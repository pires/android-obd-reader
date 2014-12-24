package pt.lighthouselabs.obd.reader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import pt.lighthouselabs.obd.reader.ObdProgressListener;
import pt.lighthouselabs.obd.reader.R;
import pt.lighthouselabs.obd.reader.config.ObdConfig;
import pt.lighthouselabs.obd.reader.dialog.MyFragmentDialog;
import pt.lighthouselabs.obd.reader.io.AbstractGatewayService;
import pt.lighthouselabs.obd.reader.io.MockObdGatewayService;
import pt.lighthouselabs.obd.reader.io.ObdCommandJob;
import pt.lighthouselabs.obd.reader.io.ObdGatewayService;
import pt.lighthouselabs.obd.reader.net.ObdReading;
import pt.lighthouselabs.obd.reader.net.ObdService;
import retrofit.RestAdapter;
import retrofit.client.Response;

public class MainActivity extends ActionBarActivity implements ObdProgressListener
{

    // TODO make this configurable
    private static final String ENDPOINT = "http://10.0.3.2:8080/obd"; // -> /_ah/api
    private static final boolean UPLOAD = false;

    private static final String TAG = MainActivity.class.getName();
    public static final int NO_BLUETOOTH_ID = 0;
    public static final int BLUETOOTH_DISABLED = 1;
    public static final int START_LIVE_DATA = 2;
    public static final int STOP_LIVE_DATA = 3;
    public static final int SETTINGS = 4;
    public static final int GET_DTC = 5;
    public static final int TABLE_ROW_MARGIN = 7;
    public static final int NO_ORIENTATION_SENSOR = 8;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private final SensorEventListener orientListener = new SensorEventListener()
    {
        public void onSensorChanged(SensorEvent event)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mGravity = event.values.clone();
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mGeomagnetic = event.values.clone();
            }

            if (mGravity != null && mGeomagnetic != null)
            {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success)
                {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll

                    orientation[0] = (float) Math.toDegrees(orientation[0]); // Z
                    orientation[1] = (float) Math.toDegrees(orientation[1]); // X
                    orientation[2] = (float) Math.toDegrees(orientation[2]); // Y

                    String dir = getDirection(orientation[1]); // x = orientation[1]
                    updateTextView(tvCompass, dir);

                    //                    Log.d(TAG, "x= " + orientation[1]);
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
            {
                float x = event.values[0];
                String dir = getDirection(x);
                updateTextView(tvCompass, dir);

                //                Log.d(TAG, "x= " + x);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
            // do nothing
        }
    };

    private final Runnable mQueueCommands = new Runnable()
    {
        public void run()
        {
            if (service != null && service.isRunning() && service.queueEmpty())
            {
                queueCommands();
            }

            // run again in period defined in preferences
            new Handler().postDelayed(mQueueCommands, ConfigFragment.getUpdatePeriod(prefs));
        }
    };

    private AbstractGatewayService service;
    private ServiceConnection serviceConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            Log.d(TAG, className.toString() + " service is bound");
            isServiceBound = true;
            service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(MainActivity.this);
            Log.d(TAG, "Starting the live data");
            service.startService();
        }

        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            Log.d(TAG, className.toString() + " service is unbound");
            isServiceBound = false;
        }
    };

    private Sensor orientSensor = null;
    private boolean preRequisites = true;

    private TextView tvCompass;
    private LinearLayout llContainer;
    private TableLayout tlContents;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private PowerManager powerManager;
    private SharedPreferences prefs;
    private boolean isServiceBound;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link cml to the class
        tvCompass = (TextView) findViewById(R.id.compass_text);
        llContainer = (LinearLayout) findViewById(R.id.vehicle_view);
        tlContents = (TableLayout) findViewById(R.id.data_table);

        // Init SP
        prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // get Bluetooth device
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        preRequisites = btAdapter != null;
        if (preRequisites)
        {
            preRequisites = btAdapter.isEnabled();
        }

        if (!preRequisites)
        {
            displayDialog(BLUETOOTH_DISABLED);
        }

        // get required sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (orientSensor == null)
        {
            displayDialog(NO_ORIENTATION_SENSOR);
            Toast.makeText(this, "NO_ORIENTATION_SENSOR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "Resuming..");

        // Register listener
        sensorManager.registerListener(orientListener, orientSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(orientListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(orientListener, magnetometer, SensorManager.SENSOR_DELAY_UI);

        // Prevent screen from going off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "Pausing..");

        // Unregister listener
        sensorManager.unregisterListener(orientListener);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Clear flag
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (isServiceBound)
        {
            doUnbindService();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem startItem = menu.findItem(START_LIVE_DATA);
        MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
        MenuItem settingsItem = menu.findItem(SETTINGS);
        MenuItem getDTCItem = menu.findItem(GET_DTC);

        if (service != null && service.isRunning())
        {
            getDTCItem.setEnabled(false);
            startItem.setEnabled(false);
            stopItem.setEnabled(true);
            settingsItem.setEnabled(false);
        }
        else
        {
            getDTCItem.setEnabled(true);
            stopItem.setEnabled(false);
            startItem.setEnabled(true);
            settingsItem.setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
        menu.add(0, STOP_LIVE_DATA, 0, "Stop Live Data");
        menu.add(0, GET_DTC, 0, "Get DTC");
        menu.add(0, SETTINGS, 0, "Settings");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
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

    private void updateConfig()
    {
        startActivity(new Intent(this, ConfigActivity.class));
    }

    public void updateTextView(final TextView view, final String txt)
    {
        new Handler().post(new Runnable()
        {
            public void run()
            {
                view.setText(txt);
            }
        });
    }

    public static String LookUpCommand(String txt)
    {
        for (AvailableCommandNames item : AvailableCommandNames.values())
        {
            if (item.getValue().equals(txt))
            {
                return item.name();
            }
        }
        return txt;
    }

    public void stateUpdate(final ObdCommandJob job)
    {
        final String cmdName = job.getCommand().getName();
        String cmdResult = "";
        final String cmdID = LookUpCommand(cmdName);

        if (job.getState().equals(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR))
        {
            cmdResult = job.getCommand().getResult();
        }
        else
        {
            cmdResult = job.getCommand().getFormattedResult();
        }

        if (llContainer.findViewWithTag(cmdID) != null)
        {
            TextView existingTV = (TextView) llContainer.findViewWithTag(cmdID);
            existingTV.setText(cmdResult);
        }
        else
        {
            addTableRow(cmdID, cmdName, cmdResult);
        }

        if (UPLOAD)
        {
            Map<String, String> commandResult = new HashMap<String, String>();
            commandResult.put(cmdID, cmdResult);
            // TODO get coords from GPS, if enabled, and set VIN properly
            ObdReading reading = new ObdReading(0d, 0d, System.currentTimeMillis(), "UNDEFINED_VIN", commandResult);
            new UploadAsyncTask().execute(reading);
        }
    }


    // private void staticCommand() {
    // Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
    // startActivity(commandIntent);
    // }

    private void getTroubleCodes()
    {
        startActivity(new Intent(this, TroubleCodesActivity.class));
    }

    private void startLiveData()
    {
        Log.d(TAG, "Starting live data..");

        tlContents.removeAllViews(); //start fresh
        doBindService();

        // start command execution
        new Handler().post(mQueueCommands);
    }

    private void stopLiveData()
    {
        Log.d(TAG, "Stopping live data..");

        doUnbindService();
    }

    private void addTableRow(String id, String key, String val)
    {

        TableRow tr = new TableRow(this);
        MarginLayoutParams params = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN);
        tr.setLayoutParams(params);

        TextView name = new TextView(this);
        name.setGravity(Gravity.RIGHT);
        name.setText(key + ": ");
        TextView value = new TextView(this);
        value.setGravity(Gravity.LEFT);
        value.setText(val);
        value.setTag(id);
        tr.addView(name);
        tr.addView(value);
        tlContents.addView(tr, params);
    }


    /**
     *
     */
    private void queueCommands()
    {
        if (isServiceBound)
        {
            for (ObdCommand Command : ObdConfig.getCommands())
            {
                if (prefs.getBoolean(Command.getName(), true))
                {
                    service.queueJob(new ObdCommandJob(Command));
                }
            }
        }
    }

    private void doBindService()
    {
        if (!isServiceBound)
        {
            Log.d(TAG, "Binding OBD service..");

            if (preRequisites)
            {
                Intent serviceIntent = new Intent(this, ObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            }
            else
            {
                Intent serviceIntent = new Intent(this, MockObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private void doUnbindService()
    {
        if (isServiceBound)
        {
            if (service.isRunning())
            {
                service.stopService();
            }

            Log.d(TAG, "Unbinding OBD service..");
            unbindService(serviceConn);
            isServiceBound = false;
        }
    }

    /**
     * Uploading asynchronous task
     */
    private class UploadAsyncTask extends AsyncTask<ObdReading, Void, Void>
    {

        @Override
        protected Void doInBackground(ObdReading... readings)
        {
            Log.d(TAG, "Uploading " + readings.length + " readings..");
            // instantiate reading service client
            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ENDPOINT).build();
            ObdService service = restAdapter.create(ObdService.class);
            // upload readings
            for (ObdReading reading : readings)
            {
                Response response = service.uploadReading(reading);
                assert response.getStatus() == 200;
            }
            Log.d(TAG, "Done");
            return null;
        }

    }


    private String getDirection(float x)
    {
        String dir = "";

        if (x >= 337.5 || x < 22.5)
        {
            dir = "N";
        }
        else if (x >= 22.5 && x < 67.5)
        {
            dir = "NE";
        }
        else if (x >= 67.5 && x < 112.5)
        {
            dir = "E";
        }
        else if (x >= 112.5 && x < 157.5)
        {
            dir = "SE";
        }
        else if (x >= 157.5 && x < 202.5)
        {
            dir = "S";
        }
        else if (x >= 202.5 && x < 247.5)
        {
            dir = "SW";
        }
        else if (x >= 247.5 && x < 292.5)
        {
            dir = "W";
        }
        else if (x >= 292.5 && x < 337.5)
        {
            dir = "NW";
        }

        return dir;
    }

    private void displayDialog(int mode)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null)
        {
            ft.remove(fragment);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = MyFragmentDialog.newInstance(mode);
        newFragment.show(ft, "dialog");
    }
}