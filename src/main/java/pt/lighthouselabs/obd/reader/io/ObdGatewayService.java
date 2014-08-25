package pt.lighthouselabs.obd.reader.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.inject.Inject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelConsumptionRateObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import pt.lighthouselabs.obd.commands.pressure.BarometricPressureObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.ObdResetCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import pt.lighthouselabs.obd.exceptions.NoDataException;
import pt.lighthouselabs.obd.reader.R;
import pt.lighthouselabs.obd.reader.activity.ConfigActivity;
import pt.lighthouselabs.obd.reader.activity.MainActivity;
import pt.lighthouselabs.obd.reader.io.ObdCommandJob.ObdCommandJobState;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * <p/>
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
public class ObdGatewayService extends AbstractGatewayService {

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
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final IBinder binder = new ObdGatewayServiceBinder();
    @Inject
    SharedPreferences prefs;

    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private BluetoothSocket sockFallback = null;

    public void startService() {
        if (MainActivity.trace) Log.d(TAG, "Starting service..");

        // get the remote Bluetooth device
        final String remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice)) {
            Toast.makeText(ctx, "No Bluetooth device selected", Toast.LENGTH_LONG).show();

            // log error
            if (MainActivity.trace) Log.e(TAG, "No Bluetooth device has been selected.");

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
        if (MainActivity.trace) Log.d(TAG, "Stopping Bluetooth discovery.");
        btAdapter.cancelDiscovery();

        showNotification("Tap to open OBD-Reader", "Starting OBD connection..", R.drawable.ic_launcher, true, true, false);

        try {
            startObdConnection();
        } catch (Exception e) {
            if (MainActivity.trace) Log.e(
                    TAG,
                    "There was an error while establishing connection. -> "
                            + e.getMessage()
            );

            // in case of failure, stop this service.
            stopService();
        }
    }

    /**
     * Start and configure the connection to the OBD interface.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @throws IOException
     */
    private void startObdConnection() throws IOException {
        if (MainActivity.trace) Log.d(TAG, "Starting OBD connection..");

        try {
            // Instantiate a BluetoothSocket for the remote device and connect it.
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
        } catch (Exception e1) {
            if (MainActivity.trace)
                Log.e(TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
            } catch (Exception e2) {
                if (MainActivity.trace)
                    Log.e(TAG, "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                stopService();
                ((MainActivity) ctx).ELMNotAvailable();
                return;
            }
        }

        // Let's configure the connection.
        if (MainActivity.trace) Log.d(TAG, "Queing jobs for connection configuration..");
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
        if (MainActivity.trace) Log.d(TAG, "Initialization jobs queued.");

        isRunning = true;
    }

    /**
     * Runs the queue until the service is stopped
     */
    protected void executeQueue() {
        if (MainActivity.trace) Log.d(TAG, "Executing queue..");
        isQueueRunning = true;
        // this is in case i want to test the code without actually connecting to the
        while (!jobsQueue.isEmpty()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();

                // log job
                if (MainActivity.trace) Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                if (job.getState().equals(ObdCommandJobState.NEW)) {
                    if (MainActivity.trace) Log.d(TAG, "Job state is NEW. Run it..");
                    job.setState(ObdCommandJobState.RUNNING);
                    job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
                } else
                    // log not new job
                    if (MainActivity.trace) Log.e(TAG,
                            "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
            } catch (IOException e) {
                job.setState(ObdCommandJobState.EXECUTION_ERROR);
                if (MainActivity.trace) Log.e(TAG, "Failed to run command. -> " + e.getMessage());
                stopService();
                ((MainActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) ctx).stopLiveData("Error Connecting to the OBD Reader");

                    }
                });

            } catch (Exception e) {
                job.setState(ObdCommandJobState.EXECUTION_ERROR);
                if (MainActivity.trace) Log.e(TAG, "Failed to run command. -> " + e.getMessage());
            }

            if (job != null) {
                final ObdCommandJob job2 = job;
                ((MainActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) ctx).stateUpdate(job2);
                    }
                });
            }
        }

        // will run next time a job is queued
        isQueueRunning = false;
    }

    /**
     * Stop OBD connection and queue processing.
     */
    public void stopService() {
        if (MainActivity.trace) Log.d(TAG, "Stopping service..");

        notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.removeAll(jobsQueue); // TODO is this safe?
        isRunning = false;

        if (sock != null)
            // close socket
            try {
                sock.close();
            } catch (IOException e) {
                if (MainActivity.trace) Log.e(TAG, e.getMessage());
            }

        // kill service
        stopSelf();
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