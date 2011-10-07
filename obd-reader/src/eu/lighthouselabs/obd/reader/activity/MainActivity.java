/*
 * TODO put header
 */
package eu.lighthouselabs.obd.reader.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.R;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;
import eu.lighthouselabs.obd.reader.io.ObdGatewayService;
import eu.lighthouselabs.obd.reader.io.ObdGatewayServiceConnection;

/**
 * The main activity.
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	/*
	 * TODO put description
	 */
	static final int NO_BLUETOOTH_ID = 0;
	static final int BLUETOOTH_DISABLED = 1;
	static final int NO_GPS_ID = 2;
	static final int START_LIVE_DATA = 3;
	static final int STOP_LIVE_DATA = 4;
	static final int SETTINGS = 5;
	static final int COMMAND_ACTIVITY = 6;
	static final int TABLE_ROW_MARGIN = 7;
	static final int NO_ORIENTATION_SENSOR = 8;

	/**
	 * Callback for ObdGatewayService to update UI.
	 */
	private IPostListener _listener = null;
	private Intent _serviceIntent = null;
	private ObdGatewayServiceConnection _serviceConnection = null;

	private SensorManager sensorManager = null;
	private Sensor orientSensor = null;
	private double fuelEconAvg = 0;
	private int fuelEconi = 0;
	private double speedAvg = 0;
	private int speedi = 0;
	private SharedPreferences prefs = null;
	private double maxFuelEcon = 70.0;

	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakeLock = null;

	private boolean preRequisites = true;

	private final SensorEventListener orientListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			float x = event.values[0];
			String dir = "";
			if (x >= 337.5 || x < 22.5) {
				dir = "N";
			} else if (x >= 22.5 && x < 67.5) {
				dir = "NE";
			} else if (x >= 67.5 && x < 112.5) {
				dir = "E";
			} else if (x >= 112.5 && x < 157.5) {
				dir = "SE";
			} else if (x >= 157.5 && x < 202.5) {
				dir = "S";
			} else if (x >= 202.5 && x < 247.5) {
				dir = "SW";
			} else if (x >= 247.5 && x < 292.5) {
				dir = "W";
			} else if (x >= 292.5 && x < 337.5) {
				dir = "NW";
			}
			TextView compass = (TextView) findViewById(R.id.compass_text);
			updateTextView(compass, dir);
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
	};

	public void updateTextView(final TextView view, final String txt) {
		new Handler().post(new Runnable() {
			public void run() {
				view.setText(txt);
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * TODO clean-up this upload thing
		 * 
		 * ExceptionHandler.register(this,
		 * "http://www.whidbeycleaning.com/droid/server.php");
		 */
		setContentView(R.layout.main);

		_listener = new IPostListener() {
			public void stateUpdate(ObdCommandJob job) {
				addTableRow(job.getCommand().getName(), job.getCommand()
						.getFormattedResult());
				Log.d(TAG, "stateUpdate callback");
			}
		};
		/*
		 * Prepare service and its connection
		 */
		_serviceIntent = new Intent(this, ObdGatewayService.class);
		_serviceConnection = new ObdGatewayServiceConnection();
		_serviceConnection.setServiceListener(_listener);

		/*
		 * Validate GPS service.
		 */
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager == null) {
			/*
			 * TODO for testing purposes we'll not make GPS a pre-requisite.
			 */
			// preRequisites = false;
			showDialog(NO_GPS_ID);
		}

		/*
		 * Validate Bluetooth service.
		 */
		// Bluetooth device exists?
		final BluetoothAdapter mBtAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBtAdapter == null) {
			preRequisites = false;
			showDialog(NO_BLUETOOTH_ID);
		} else {
			// Bluetooth device is enabled?
			if (!mBtAdapter.isEnabled()) {
				preRequisites = false;
				showDialog(BLUETOOTH_DISABLED);
			}
		}

		/*
		 * Get Orientation sensor.
		 */
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sens = sensorManager
				.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sens.size() <= 0) {
			showDialog(NO_ORIENTATION_SENSOR);
		} else {
			orientSensor = sens.get(0);
		}

		// validate app pre-requisites
		if (!preRequisites)
			unbindService(_serviceConnection);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseWakeLockIfHeld();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Pausing..");
		releaseWakeLockIfHeld();
	}

	private void releaseWakeLockIfHeld() {
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}
	
	protected void onResume() {
		super.onResume();
		
		Log.d(TAG, "Resuming..");
		
		sensorManager.registerListener(orientListener, orientSensor,
				SensorManager.SENSOR_DELAY_UI);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		maxFuelEcon = ConfigActivity.getMaxFuelEconomy(prefs);
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"ObdReader");
	}

	private void updateConfig() {
		Intent configIntent = new Intent(this, ConfigActivity.class);
		startActivity(configIntent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
		menu.add(0, COMMAND_ACTIVITY, 0, "Run Command");
		menu.add(0, STOP_LIVE_DATA, 0, "Stop");
		menu.add(0, SETTINGS, 0, "Settings");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case START_LIVE_DATA:
			startLiveData();
			return true;
		case STOP_LIVE_DATA:
			stopLiveData();
			return true;
		case SETTINGS:
			updateConfig();
			return true;
			// case COMMAND_ACTIVITY:
			// staticCommand();
			// return true;
		}
		return false;
	}

	// private void staticCommand() {
	// Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
	// startActivity(commandIntent);
	// }

	private void startLiveData() {
		Log.d(TAG, "Starting live data..");
		
		/*
		 * Bind service
		 */
		Log.d(TAG, "Binding service..");
		bindService(_serviceIntent, _serviceConnection,
				Context.BIND_AUTO_CREATE);
		
		if (!_serviceConnection.isRunning()) {
			Log.d(TAG, "Service is not running. Going to start it..");
			
			startService(_serviceIntent);
		}

		// TODO start running commands
		// we can do this through binder in serviceconnection
		
		// screen won't turn off until wakeLock.release()
		wakeLock.acquire();
	}

	private void stopLiveData() {
		Log.d(TAG, "Stopping live data..");
		
		if (_serviceConnection.isRunning())
			stopService(_serviceIntent);

		// TODO stop running commands
		// we can do this through binder in serviceconnection

		releaseWakeLockIfHeld();
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		switch (id) {
		case NO_BLUETOOTH_ID:
			build.setMessage("Sorry, your device doesn't support Bluetooth.");
			return build.create();
		case BLUETOOTH_DISABLED:
			build.setMessage("You have Bluetooth disabled. Please enable it!");
			return build.create();
		case NO_GPS_ID:
			build.setMessage("Sorry, your device doesn't support GPS.");
			return build.create();
		case NO_ORIENTATION_SENSOR:
			build.setMessage("Orientation sensor missing?");
			return build.create();
		}
		return null;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem startItem = menu.findItem(START_LIVE_DATA);
		MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
		MenuItem settingsItem = menu.findItem(SETTINGS);
		MenuItem commandItem = menu.findItem(COMMAND_ACTIVITY);

		// validate if preRequisites are satisfied.
		if (preRequisites) {
			if (_serviceConnection.isRunning()) {
				startItem.setEnabled(false);
				stopItem.setEnabled(true);
				settingsItem.setEnabled(false);
				commandItem.setEnabled(false);
			} else {
				stopItem.setEnabled(false);
				startItem.setEnabled(true);
				settingsItem.setEnabled(true);
				commandItem.setEnabled(false);
			}
		} else {
			startItem.setEnabled(false);
			stopItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandItem.setEnabled(false);
		}

		return true;
	}

	private void addTableRow(String key, String val) {
		TableLayout tl = (TableLayout) findViewById(R.id.data_table);
		TableRow tr = new TableRow(this);
		MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
				TABLE_ROW_MARGIN);
		tr.setLayoutParams(params);
		tr.setBackgroundColor(Color.BLACK);
		TextView name = new TextView(this);
		name.setGravity(Gravity.RIGHT);
		name.setText(key + ": ");
		TextView value = new TextView(this);
		value.setGravity(Gravity.LEFT);
		value.setText(val);
		tr.addView(name);
		tr.addView(value);
		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		/*
		 * TODO remove this hack
		 * 
		 * let's define a limit number of rows
		 */
		if (tl.getChildCount() > 10)
			tl.removeViewAt(0);
	}

	// private class UpdateThread extends Thread {
	// boolean stop = false;
	//
	// public void run() {
	// String vehicleId = prefs.getString(
	// ConfigActivity.VEHICLE_ID_KEY, "");
	// while (!stop && _serviceConnection.isRunning()) {
	// ObdReaderService svc = _serviceConnection.getService();
	// Map<String, String> dataMap = null;
	// if (svc == null || svc.getDataMap() == null) {
	// dataMap = new HashMap<String, String>();
	// for (ObdCommand cmd : ObdConfig.getCommands()) {
	// // TODO why a Map?
	// dataMap.put(cmd.getName(), "--");
	// }
	// } else {
	// dataMap = svc.getDataMap();
	// }
	// if (vehicleId != null && !"".equals(vehicleId.trim())) {
	// dataMap.put("Vehicle ID", vehicleId);
	// }
	// updateDataTable(dataMap);
	// try {
	// Thread.sleep(ConfigActivity.getUpdatePeriod(prefs));
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

}