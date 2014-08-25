package pt.lighthouselabs.obd.reader.io;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.inject.Inject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pt.lighthouselabs.obd.reader.activity.MainActivity;
import roboguice.service.RoboService;


public abstract class AbstractGatewayService extends RoboService {
  private static final String TAG = AbstractGatewayService.class.getName();
  public static final int NOTIFICATION_ID = 1;
  @Inject
  protected NotificationManager notificationManager;

  protected Context ctx;
  protected boolean isRunning = false;
  private final IBinder binder = new AbstractGatewayServiceBinder();
  protected boolean isQueueRunning = false;
  protected Long queueCounter = 0L;
  protected BlockingQueue<ObdCommandJob> jobsQueue = new LinkedBlockingQueue<ObdCommandJob>();

  @Override
  public IBinder onBind(Intent intent) {
      return binder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (MainActivity.trace) Log.d(TAG, "Creating service..");
    if (MainActivity.trace) Log.d(TAG, "Service created.");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (MainActivity.trace) Log.d(TAG, "Destroying service...");
    notificationManager.cancel(NOTIFICATION_ID);
    if (MainActivity.trace) Log.d(TAG, "Service destroyed.");
  }

  public boolean isRunning() {
    return isRunning;
  }



  public class AbstractGatewayServiceBinder extends Binder {
    public AbstractGatewayService getService() {
      return AbstractGatewayService.this;
    }
  }

    public boolean queueEmpty() {
        return jobsQueue.isEmpty();
    }

   /**
   * This method will add a job to the queue while setting its ID to the
   * internal queue counter.
   *
   * @param job the job to queue.
   */
  public void queueJob(ObdCommandJob job) {
      queueCounter++;
      if (MainActivity.trace) Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

      job.setId(queueCounter);
      try {
        jobsQueue.put(job);
        if (MainActivity.trace) Log.d(TAG, "Job queued successfully.");
      } catch (InterruptedException e) {
        job.setState(ObdCommandJob.ObdCommandJobState.QUEUE_ERROR);
        if (MainActivity.trace) Log.e(TAG, "Failed to queue job.");
      }

    if (!isQueueRunning) {
      // Run the executeQueue in a different thread to lighten the UI thread
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          executeQueue();
        }
      });

      t.start();
    }
  }

    /**
     * Show a notification while this service is running.
     */
  protected void showNotification(String contentTitle, String contentText, int icon, boolean ongoing, boolean notify, boolean vibrate) {
    final PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( ctx);
    notificationBuilder.setContentTitle(contentTitle)
              .setContentText(contentText).setSmallIcon(icon)
              .setContentIntent(contentIntent)
              .setWhen(System.currentTimeMillis());
    // can cancel?
    if (ongoing) {
      notificationBuilder.setOngoing(true);
    } else {
      notificationBuilder.setAutoCancel(true);
    }
    if (vibrate) {
      notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
    }
    if (notify) {
      notificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
    }
  }

  public void setContext(Context c) {
    ctx = c;
  }

  abstract protected void executeQueue();
  abstract public void startService();
  abstract public void stopService();
}
