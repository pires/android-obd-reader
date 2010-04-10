package org.obdreader.io;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;

import org.obdreader.R;
import org.obdreader.activity.ObdReaderConfigActivity;
import org.obdreader.command.ObdCommand;

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

public class ObdReaderService extends Service {

	private ObdConnectThread connectThread = null;
	private final IBinder binder = new ObdReaderServiceBinder();
	private NotificationManager notifyMan = null;
	private Context context = null;
	private Intent notificationIntent = null;
	private PendingIntent contentIntent = null;
	public static final int COMMAND_ERROR_NOTIFY = 2;
	public static final int CONNECT_ERROR_NOTIFY = 3;
	public static final int OBD_SERVICE_RUNNING_NOTIFY = 4;
	public static final int OBD_SERVICE_ERROR_NOTIFY = 5;

	public void onCreate() {
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
        int period = ObdReaderConfigActivity.getUpdatePeriod(prefs);
        double ve = ObdReaderConfigActivity.getVolumetricEfficieny(prefs);
        double ed = ObdReaderConfigActivity.getEngineDisplacement(prefs);
        boolean imperialUnits = prefs.getBoolean(ObdReaderConfigActivity.IMPERIAL_UNITS_KEY, false);
        boolean gps = prefs.getBoolean(ObdReaderConfigActivity.ENABLE_GPS_KEY, false);
        ArrayList<ObdCommand> cmds = ObdReaderConfigActivity.getObdCommands(prefs);
		BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(devString);
		connectThread = new ObdConnectThread(dev,locationManager,this,uploadUrl,period,ed,ve,imperialUnits,gps,cmds);
		connectThread.setEngineDisplacement(ed);
		connectThread.setVolumetricEfficiency(ve);
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
				notifyMessage(e.getMessage(),strw.toString(), OBD_SERVICE_ERROR_NOTIFY);
			}
		}
		connectThread.close();
		stopSelf();
		return true;
	}
	public void notifyMessage(String msg, String longMsg, int notifyId) {
		long when = System.currentTimeMillis();
		Notification notification = new Notification(android.R.drawable.stat_notify_error, msg, when);
		notification.setLatestEventInfo(context, msg, longMsg, contentIntent);
		notifyMan.notify(notifyId, notification);
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
			try {
				t.start();
				long when = System.currentTimeMillis();
				Notification notification = new Notification(R.drawable.car, "OBD Service Running", when);
				notification.setLatestEventInfo(context, "OBD Service Running", "", contentIntent);
				notification.flags |= Notification.FLAG_NO_CLEAR;
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				notifyMan.notify(OBD_SERVICE_RUNNING_NOTIFY, notification);
				t.join();
			} catch (Exception e) {
			} finally {
				notifyMan.cancel(OBD_SERVICE_RUNNING_NOTIFY);
				stopSelf();
			}
		}
	}
}
