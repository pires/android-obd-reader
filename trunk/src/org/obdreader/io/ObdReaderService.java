package org.obdreader.io;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.obdreader.activity.ObdReaderConfigActivity;

import org.obdreader.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nullwire.trace.ExceptionHandler;

public class ObdReaderService extends Service {

	private ObdConnectThread connectThread = null;
	private final IBinder binder = new ObdReaderServiceBinder();
	private int notifyId = 1;
	private NotificationManager notifyMan = null;
	private Context context = null;
	private Intent notificationIntent = null;
	private PendingIntent contentIntent = null;

	public void onCreate() {
		ExceptionHandler.register(this,"http://www.whidbeycleaning.com/droid/server.php");
		super.onCreate();
		notifyMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		context = getApplicationContext();
		notificationIntent = new Intent(this, ObdReaderService.class);
		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	}
	public void onDestroy() {
		super.onDestroy();
		stopService();
		stopSelf();
	}
	public boolean isRunning() {
		if (connectThread == null) {
			return false;
		}
		return connectThread.isAlive();
	}
	public IBinder onBind(Intent intent) {
		return binder;
	}
	public boolean startService() {
		if (connectThread != null && connectThread.isAlive()) {
			return true;
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String devString = prefs.getString(ObdReaderConfigActivity.BLUETOOTH_LIST_KEY, null);
		boolean uploadEnabled = prefs.getBoolean(ObdReaderConfigActivity.UPLOAD_DATA_KEY,false);
		String uploadUrl = null;
		if (uploadEnabled) {
			uploadUrl = prefs.getString(ObdReaderConfigActivity.UPLOAD_URL_KEY, null);
		}
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if (mBluetoothAdapter == null) {
    		Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_LONG).show();
    		stopSelf();
        	return false;
        }
    	if (devString == null || "".equals(devString)) {
    		Toast.makeText(this, "No bluetooth device selected", Toast.LENGTH_LONG).show();
			stopSelf();
			return false;
		}
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
        	Toast.makeText(this, "This device does not support GPS", Toast.LENGTH_LONG).show();
        	stopSelf();
        	return false;
        }
        String periodString = prefs.getString(ObdReaderConfigActivity.UPDATE_PERIOD_KEY, "4");
        int period = 4000;
        try {
			period = Integer.parseInt(periodString);
		} catch (Exception e) {
		}
		BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(devString);
		connectThread = new ObdConnectThread(dev,locationManager,this,uploadUrl,period);
		new ObdReaderServiceWorkerThread(connectThread).start();
		return true;
	}
	public boolean stopService() {
		if (connectThread == null) {
			return true;
		}
		while (connectThread.isAlive()) {
			try {
				connectThread.cancel();
				connectThread.join(300);
			} catch (InterruptedException e) {
		    	StringWriter strw = new StringWriter();
		    	PrintWriter ptrw = new PrintWriter(strw);
		    	e.printStackTrace(ptrw);
				notifyMessage(e.getMessage(),strw.toString());
			}
		}
		connectThread.close();
		stopSelf();
		return true;
	}
	public void notifyMessage(String msg, String longMsg) {
		long when = System.currentTimeMillis();
		Notification notification = new Notification(android.R.drawable.stat_notify_error, msg, when);
		notification.setLatestEventInfo(context, msg, longMsg, contentIntent);
		notifyMan.notify(notifyId, notification);
		notifyId ++;
	}
	public Map<String,String> getDataMap() {
		if (connectThread != null) {
			return connectThread.getResults();
		}
		return null;
	}
	public class ObdReaderServiceBinder extends Binder {
		ObdReaderService getService() {
			return ObdReaderService.this;
		}
	}
	private class ObdReaderServiceWorkerThread extends Thread {

		ObdConnectThread t = null;
		public ObdReaderServiceWorkerThread(ObdConnectThread t) {
			this.t = t;
		}
		public void run() {
			t.start();
			long when = System.currentTimeMillis();
			Notification notification = new Notification(R.drawable.car, "OBD Service Running", when);
			notification.setLatestEventInfo(context, "OBD Service Running", "", contentIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notifyMan.notify(3, notification);
			try {
				t.join();
			} catch (InterruptedException e) {
			}
			stopSelf();
			notifyMan.cancel(3);
		}
	}
}
