package com.github.pires.obd.reader.io;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.pires.obd.reader.activity.MainActivity;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import roboguice.service.RoboService;


public abstract class AbstractGatewayService extends RoboService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = AbstractGatewayService.class.getName();
    private final IBinder binder = new AbstractGatewayServiceBinder();
    @Inject
    protected NotificationManager notificationManager;
    protected Context ctx;
    protected boolean isRunning = false;
    protected Long queueCounter = 0L;
    protected BlockingQueue<ObdCommandJob> jobsQueue = new LinkedBlockingQueue<>();
    // Run the executeQueue in a different thread to lighten the UI thread
    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                executeQueue();
            } catch (InterruptedException e) {
                t.interrupt();
            }
        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating service..");
        t.start();
        Log.d(TAG, "Service created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying service...");
        notificationManager.cancel(NOTIFICATION_ID);
        t.interrupt();
        Log.d(TAG, "Service destroyed.");
    }

    public boolean isRunning() {
        return isRunning;
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
        Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

        job.setId(queueCounter);
        try {
            jobsQueue.put(job);
            Log.d(TAG, "Job queued successfully.");
        } catch (InterruptedException e) {
            job.setState(ObdCommandJob.ObdCommandJobState.QUEUE_ERROR);
            Log.e(TAG, "Failed to queue job.");
        }
    }

    /**
     * Show a notification while this service is running.
     */
    protected void showNotification(String contentTitle, String contentText, int icon, boolean ongoing, boolean notify, boolean vibrate) {
        final PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);
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

    abstract protected void executeQueue() throws InterruptedException;

    abstract public void startService() throws IOException;

    abstract public void stopService();

    public class AbstractGatewayServiceBinder extends Binder {
        public AbstractGatewayService getService() {
            return AbstractGatewayService.this;
        }
    }
}
