package pt.lighthouselabs.obd.reader.io;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.ObdResetCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import pt.lighthouselabs.obd.reader.R;
import pt.lighthouselabs.obd.reader.activity.ConfigActivity;
import pt.lighthouselabs.obd.reader.activity.MainActivity;
import pt.lighthouselabs.obd.reader.io.ObdCommandJob.ObdCommandJobState;
import roboguice.service.RoboService;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * <p/>
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
public class ObdGatewayService extends RoboService {

  public static final int NOTIFICATION_ID = 1;
  private static final String TAG = ObdGatewayService.class.getName();
  /*
   * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
   * #createRfcommSocketToServiceRecord(java.util.UUID)
   *
   * "Hint: If you are connecting to a Bluetooth serial board then try using the
   * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
   * are connecting to an Android peer then please generate your own unique
   * UUID."
   */
  private static final UUID MY_UUID = UUID
      .fromString("00001101-0000-1000-8000-00805F9B34FB");
  private final IBinder binder = new ObdGatewayServiceBinder();
  @Inject
  SharedPreferences prefs;
  @Inject
  private Context ctx;
  @Inject
  private NotificationManager notificationManager;
  private boolean isRunning = false;
  private boolean isQueueRunning = false;
  private BlockingQueue<ObdCommandJob> jobsQueue = new LinkedBlockingQueue<ObdCommandJob>();
  private Long queueCounter = 0L;
  private BluetoothDevice dev = null;
  private BluetoothSocket sock = null;

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "Creating service..");
    Log.d(TAG, "Service created.");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "Destroying service...");
    notificationManager.cancel(NOTIFICATION_ID);
    Log.d(TAG, "Service destroyed.");
  }

  public void startService() {
    Log.d(TAG, "Starting service..");

    // get the remote Bluetooth device
    final String remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
    if (remoteDevice == null || "".equals(remoteDevice)) {
      Toast.makeText(ctx, "No Bluetooth device selected", Toast.LENGTH_LONG).show();

      // log error
      Log.e(TAG, "No Bluetooth device has been selected.");

      // TODO kill this service gracefully
      stopService();
    }

    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    dev = btAdapter.getRemoteDevice(remoteDevice);

    /*
     * TODO clean
     *
     * Get more preferences
     */
    boolean imperialUnits = prefs.getBoolean(ConfigActivity.IMPERIAL_UNITS_KEY,
        false);
    ArrayList<ObdCommand> cmds = ConfigActivity.getObdCommands(prefs);

    /*
     * Establish Bluetooth connection
     *
     * Because discovery is a heavyweight procedure for the Bluetooth adapter,
     * this method should always be called before attempting to connect to a
     * remote device with connect(). Discovery is not managed by the Activity,
     * but is run as a system service, so an application should always call
     * cancel discovery even if it did not directly request a discovery, just to
     * be sure. If Bluetooth state is not STATE_ON, this API will return false.
     *
     * see
     * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
     * .html#cancelDiscovery()
     */
    Log.d(TAG, "Stopping Bluetooth discovery.");
    btAdapter.cancelDiscovery();

    showNotification("Tap to open OBD-Reader", "Starting OBD connection..", R.drawable.ic_launcher, true, true, false);

    try {
      startObdConnection();
    } catch (Exception e) {
      Log.e(
          TAG,
          "There was an error while establishing connection. -> "
              + e.getMessage());

      // in case of failure, stop this service.
      stopService();
    }
  }

  /**
   * Start and configure the connection to the OBD interface.
   *
   * @throws IOException
   */
  private void startObdConnection() throws IOException {
    Log.d(TAG, "Starting OBD connection..");

    // Instantiate a BluetoothSocket for the remote device and connect it.
    sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
    sock.connect();

    // Let's configure the connection.
    Log.d(TAG, "Queing jobs for connection configuration..");
    queueJob(new ObdCommandJob(new ObdResetCommand()));
    queueJob(new ObdCommandJob(new EchoOffObdCommand()));

    /*
     * Will send second-time based on tests.
     *
     * TODO this can be done w/o having to queue jobs by just issuing
     * command.run(), command.getResult() and validate the result.
     */
    queueJob(new ObdCommandJob(new EchoOffObdCommand()));
    queueJob(new ObdCommandJob(new LineFeedOffObdCommand()));
    queueJob(new ObdCommandJob(new TimeoutObdCommand(62)));

    // For now set protocol to AUTO
    queueJob(new ObdCommandJob(new SelectProtocolObdCommand(ObdProtocols.AUTO)));

    // Job for returning dummy data
    queueJob(new ObdCommandJob(new AmbientAirTemperatureObdCommand()));

    queueCounter = 0L;
    Log.d(TAG, "Initialization jobs queued.");

    isRunning = true;
  }

  /**
   * Runs the queue until the service is stopped
   */
  private void executeQueue() {
    Log.d(TAG, "Executing queue..");
    isQueueRunning = true;
    while (!jobsQueue.isEmpty()) {
      ObdCommandJob job = null;
      try {
        job = jobsQueue.take();

        // log job
        Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

        if (job.getState().equals(ObdCommandJobState.NEW)) {
          Log.d(TAG, "Job state is NEW. Run it..");
          job.setState(ObdCommandJobState.RUNNING);
          job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
        } else
          // log not new job
          Log.e(TAG,
              "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
      } catch (Exception e) {
        job.setState(ObdCommandJobState.EXECUTION_ERROR);
        Log.e(TAG, "Failed to run command. -> " + e.getMessage());
      }

      if (job != null) {
        Log.d(TAG, "Job is finished.");
        job.setState(ObdCommandJobState.FINISHED);
        ((MainActivity) ctx).stateUpdate(job);
      }
    }
    // will run next time a job is queued
    isQueueRunning = false;
  }

  /**
   * This method will add a job to the queue while setting its ID to the
   * internal queue counter.
   *
   * @param job
   * @return
   */
  public void queueJob(ObdCommandJob job) {
    queueCounter++;
    Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

    job.setId(queueCounter);
    try {
      jobsQueue.put(job);
      Log.d(TAG, "Job queued successfully.");
    } catch (InterruptedException e) {
      job.setState(ObdCommandJobState.QUEUE_ERROR);
      Log.e(TAG, "Failed to queue job.");
    }

    if (!isQueueRunning)
      executeQueue();
  }

  /**
   * Stop OBD connection and queue processing.
   */
  public void stopService() {
    Log.d(TAG, "Stopping service..");

    notificationManager.cancel(NOTIFICATION_ID);
    jobsQueue.removeAll(jobsQueue); // TODO is this safe?
    isRunning = false;

    if (sock != null)
      // close socket
      try {
        sock.close();
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }

    // kill service
    stopSelf();
  }

  /**
   * Show a notification while this service is running.
   */
  private void showNotification(String contentTitle, String contentText,
                                int icon, boolean ongoing, boolean notify, boolean vibrate) {
    final PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
        new Intent(ctx, MainActivity.class), 0);
    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
        ctx);
    notificationBuilder.setContentTitle(contentTitle)
        .setContentText(contentText).setSmallIcon(icon)
        .setContentIntent(contentIntent)
        .setWhen(System.currentTimeMillis());
    // can cancel?
    if (ongoing)
      notificationBuilder.setOngoing(true);
    else
      notificationBuilder.setAutoCancel(true);
    // vibrate?
    if (vibrate)
      notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

    if (notify)
      notificationManager.notify(NOTIFICATION_ID,
          notificationBuilder.getNotification());
  }

  public boolean isRunning() {
    return isRunning;
  }

  public class ObdGatewayServiceBinder extends Binder {
    public ObdGatewayService getService() {
      return ObdGatewayService.this;
    }
  }

}