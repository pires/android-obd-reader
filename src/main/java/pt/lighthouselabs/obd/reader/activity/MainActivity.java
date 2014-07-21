package pt.lighthouselabs.obd.reader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import android.widget.Toast;

import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import pt.lighthouselabs.obd.reader.ObdProgressListener;
import pt.lighthouselabs.obd.reader.R;
import pt.lighthouselabs.obd.reader.io.AbstractGatewayService;
import pt.lighthouselabs.obd.reader.io.MockObdGatewayService;
import pt.lighthouselabs.obd.reader.io.ObdCommandJob;
import pt.lighthouselabs.obd.reader.io.ObdGatewayService;
import pt.lighthouselabs.obd.reader.net.ObdReading;
import pt.lighthouselabs.obd.reader.net.ObdService;
import retrofit.RestAdapter;
import retrofit.client.Response;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.main)
public class MainActivity extends RoboActivity implements ObdProgressListener {

  // TODO make this configurable
  private static final boolean UPLOAD = false;

  private static final String TAG = MainActivity.class.getName();
  private static final int NO_BLUETOOTH_ID = 0;
  private static final int BLUETOOTH_DISABLED = 1;
  private static final int START_LIVE_DATA = 2;
  private static final int STOP_LIVE_DATA = 3;
  private static final int SETTINGS = 4;
  private static final int GET_DTC = 5;
  private static final int TABLE_ROW_MARGIN = 7;
  private static final int NO_ORIENTATION_SENSOR = 8;
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
      updateTextView(compass, dir);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // do nothing
    }
  };
  private final Runnable mQueueCommands = new Runnable() {
    public void run() {
      if (service.isRunning())
        queueCommands();
      // run again in 2s
      new Handler().postDelayed(mQueueCommands, 2000);
    }
  };
  @InjectView(R.id.compass_text)
  private TextView compass;
  @InjectView(R.id.rpm_text)
  private TextView tvRpm;
  @InjectView(R.id.spd_text)
  private TextView tvSpeed;
  @InjectView(R.id.data_table)
  private TableLayout tl;
  @Inject
  private SensorManager sensorManager;
  @Inject
  private PowerManager powerManager;
  @Inject
  private SharedPreferences prefs;
  private boolean isServiceBound;


  private AbstractGatewayService service;
  private ServiceConnection serviceConn = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder binder) {
      Log.d(TAG, className.toString()  + " service is bound");
      isServiceBound = true;
      service = ((AbstractGatewayService.AbstractGatewayServiceBinder)binder).getService();
      service.setContext(MainActivity.this);
      Log.d(TAG, "Starting the live data");

    }

    public void onServiceDisconnected(ComponentName className) {
      Log.d(TAG, className.toString()  + " service is unbound");
      isServiceBound = false;
    }
  };

  private Sensor orientSensor = null;
  private PowerManager.WakeLock wakeLock = null;
  private boolean preRequisites = true;

  public void updateTextView(final TextView view, final String txt) {
    new Handler().post(new Runnable() {
      public void run() {
        view.setText(txt);
      }
    });
  }

  public void stateUpdate(final ObdCommandJob job) {
    final String cmdName = job.getCommand().getName();
    final String cmdResult = job.getCommand().getFormattedResult();
    if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName))
      tvRpm.setText(cmdResult);
    else if (AvailableCommandNames.SPEED.getValue().equals(
        cmdName))
      tvSpeed.setText(cmdResult);
    else if (AvailableCommandNames.MAF.getValue().equals(cmdName))
      addTableRow(cmdName, cmdResult);
    else if (AvailableCommandNames.EQUIV_RATIO.getValue().equals(cmdName))
      addTableRow(cmdName, cmdResult);
    else
      addTableRow(cmdName, cmdResult);

    if (UPLOAD) {
      Map<String, String> commandResult = new HashMap<String, String>();
      commandResult.put(cmdName, cmdResult);
      // TODO get coords from GPS, if enabled, and set VIN properly
      ObdReading reading = new ObdReading(0d, 0d, System.currentTimeMillis(), "UNDEFINED_VIN", commandResult);
      new UploadAsyncTask().execute(reading);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // get Bluetooth device
    final BluetoothAdapter btAdapter = BluetoothAdapter
        .getDefaultAdapter();

    preRequisites = btAdapter == null ? false : true;
    if (preRequisites)
      preRequisites = btAdapter.isEnabled();

    if (!preRequisites) {
      showDialog(BLUETOOTH_DISABLED);
      Toast.makeText(this, "BT is disabled, will use Mock service instead", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, "Blutooth ok", Toast.LENGTH_SHORT).show();
    }


    // get Orientation sensor
    orientSensor = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
    if (orientSensor == null)
      showDialog(NO_ORIENTATION_SENSOR);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "Entered onStart...");
    // bind service
    if (!isServiceBound) {
      doBindService();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    releaseWakeLockIfHeld();
    if (isServiceBound)
      doUnbindService();
    ;
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "Pausing..");
    releaseWakeLockIfHeld();
  }

  /**
   * If lock is held, release. Lock will be held when the service is running.
   */
  private void releaseWakeLockIfHeld() {
    if (wakeLock.isHeld())
      wakeLock.release();
  }

  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Resuming..");
    sensorManager.registerListener(orientListener, orientSensor,
        SensorManager.SENSOR_DELAY_UI);
    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
        "ObdReader");
    
  }

  private void updateConfig() {
    startActivity(new Intent(this, ConfigActivity.class));
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
    menu.add(0, STOP_LIVE_DATA, 0, "Stop Live Data");
    menu.add(0, GET_DTC, 0, "Get DTC");
    menu.add(0, SETTINGS, 0, "Settings");
    return true;
  }

  // private void staticCommand() {
  // Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
  // startActivity(commandIntent);
  // }

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
      case GET_DTC:
        getTroubleCodes();
        return true;
      // case COMMAND_ACTIVITY:
      // staticCommand();
      // return true;
    }
    return false;
  }

  private void getTroubleCodes() {
    startActivity(new Intent(this, TroubleCodesActivity.class));
  }

  private void startLiveData() {
    Log.d(TAG, "Starting live data..");
    Log.d(TAG, "Service is bound? " + isServiceBound + ", and running? " + service.isRunning());
    if (isServiceBound && !service.isRunning()) {
      Log.d(TAG, "Service is not running. Going to start it..");
      service.startService();
    }

    // start command execution
    new Handler().post(mQueueCommands);

    // screen won't turn off until wakeLock.release()
    wakeLock.acquire();
  }

  private void stopLiveData() {
    Log.d(TAG, "Stopping live data..");
    if (isServiceBound)
      doUnbindService();
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
    MenuItem getDTCItem = menu.findItem(GET_DTC);

    if (service!=null && service.isRunning()) {
      getDTCItem.setEnabled(false);
      startItem.setEnabled(false);
      stopItem.setEnabled(true);
      settingsItem.setEnabled(false);
    } else {
      getDTCItem.setEnabled(true);
      stopItem.setEnabled(false);
      startItem.setEnabled(true);
      settingsItem.setEnabled(true);
    }

    return true;
  }

  private void addTableRow(String key, String val) {
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

  /**
   *
   */
  private void queueCommands() {
    if (isServiceBound) {
      final ObdCommandJob airTemp = new ObdCommandJob(
          new AmbientAirTemperatureObdCommand());
      final ObdCommandJob speed = new ObdCommandJob(new SpeedObdCommand());
      final ObdCommandJob fuelEcon = new ObdCommandJob(
          new FuelEconomyObdCommand());
      final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());
      final ObdCommandJob fuelLevel = new ObdCommandJob(
          new FuelLevelObdCommand());

      service.queueJob(airTemp);
      service.queueJob(speed);
      service.queueJob(fuelEcon);
      service.queueJob(rpm);
      service.queueJob(fuelLevel);
    }
  }

  private void doBindService() {
    if (!isServiceBound) {
      Log.d(TAG, "Binding OBD service..");
      if(preRequisites) {
        Intent serviceIntent = new Intent(this, ObdGatewayService.class);
        bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
      } else {
        Intent serviceIntent = new Intent(this, MockObdGatewayService.class);
        bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
      }
    }
  }

  private void doUnbindService() {
    if (isServiceBound) {
      if (service.isRunning()) {
        service.stopService();
      }
      Log.d(TAG, "Unbinding OBD service..");
      unbindService(serviceConn);
    }
  }

  /**
   * Uploading asynchronous task
   */
  private class UploadAsyncTask extends AsyncTask<ObdReading, Void, Void> {

    @Override
    protected Void doInBackground(ObdReading... readings) {
      Log.d(TAG, "Uploading " + readings.length + " readings..");
      // instantiate reading service client
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("http://server_ip:8080/obd")
          .build();
      ObdService service = restAdapter.create(ObdService.class);
      // upload readings
      for (ObdReading reading : readings) {
        Response response = service.uploadReading(reading);
        assert response.getStatus() == 200;
      }
      Log.d(TAG, "Done");
      return null;
    }

  }

}