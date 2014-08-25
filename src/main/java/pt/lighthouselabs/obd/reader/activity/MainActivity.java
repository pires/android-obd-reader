package pt.lighthouselabs.obd.reader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.apache.http.HttpResponse;

import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.control.DistanceTraveledSinceCodesClearedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineLoadObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRuntimeObdCommand;
import pt.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FindFuelTypeObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelConsumptionRateObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import pt.lighthouselabs.obd.commands.pressure.BarometricPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.FuelPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
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

@ContentView(R.layout.main_alt)
public class MainActivity extends RoboActivity implements ObdProgressListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static boolean UPLOAD = false;

    private static final String TAG = MainActivity.class.getName();
    private static final int NO_BLUETOOTH_ID = 0;
    private static final int BLUETOOTH_DISABLED = 1;
    private static final int START_LIVE_DATA = 2;
    private static final int STOP_LIVE_DATA = 3;
    private static final int SETTINGS = 4;
    private static final int GET_DTC = 5;
    private static final int TABLE_ROW_MARGIN = 7;
    private static final int NO_ORIENTATION_SENSOR = 8;

    // added FC
    private LocationReceiver locationListener;
    public static PendingIntent locationIntent;
    String vehicleId;
    private Database database;
    private String lastCommand;
    private boolean serviceBusy=false;
    private Map<String, String> unsentReadings;
    public static boolean trace = false;
    private boolean GPSready = false;
    /**
     * set this to true if you want to test the system without having an OBD REader at hand
     * (or simply to test the whole system without being in the car ;)
     */
    public static boolean excludeOBDReaderForDebugging=false;


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
            updateTextView(row1Col2TextView, dir);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };

    private final Runnable mQueueCommands = new Runnable() {
        public void run() {
            if ((excludeOBDReaderForDebugging) || (service != null && service.isRunning())) {
                queueCommands();
            }
            // run again in a
            // no of seconds given by the update interval
            new Handler().postDelayed(mQueueCommands, ConfigActivity.getUpdatePeriod(prefs));

        }
    };
    @InjectView(R.id.row1_col1)
    private TextView row1Col1TextView;
    @InjectView(R.id.row1_col2)
    private TextView row1Col2TextView;
    @InjectView(R.id.row2_col1)
    private TextView row2Col1TextView;
    @InjectView(R.id.row2_col2)
    private TextView row2Col2TextView;
    @InjectView(R.id.row2_col3)
    private TextView row2Col3TextView;

    @InjectView(R.id.data_table)
    private TableLayout tl;
    @InjectView(R.id.data_text)
    private TextView data_text_view;

    @Inject
    private SensorManager sensorManager;
    @Inject
    private PowerManager powerManager;
    @Inject
    private SharedPreferences prefs;
    private boolean isServiceBound;

    private AbstractGatewayService service;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (trace) Log.d(TAG, className.toString() + " service is bound");
            isServiceBound = true;
            service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(MainActivity.this);
            if (trace) Log.d(TAG, "Starting the live data");
            if (!excludeOBDReaderForDebugging) service.startService();

        }

        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (trace) Log.d(TAG, className.toString() + " service is unbound");
            isServiceBound = false;
        }
    };

    private Sensor orientSensor = null;
    private PowerManager.WakeLock wakeLock = null;
    private boolean preRequisites = true;
    private boolean useImperialUnits;


    public void updateTextView(final TextView view, final String txt) {
        new Handler().post(new Runnable() {
            public void run() {
                view.setText(txt);
            }
        });
    }

    public void stateUpdate(final ObdCommandJob job) {
        String cmdName = job.getCommand().getName();
        if (trace) Log.d(TAG, cmdName);
        unsentReadings.put(cmdName, job.getCommand().getFormattedResult());
            // TODO get coords from GPS, if enabled, and set VIN properly
            // FC: DONE GPS: I do not know what VIN is so I cannot help here
            // it does not make sense to send log and lat every time. you just do it for every block of data
            // also it is more efficient to update the interface once everything is already rather than doing it
            // in bits and in pieces
            if (cmdName.equals(lastCommand)) {
                serviceBusy=false;
                // write on screen
                data_text_view.setText("");
                for (Map.Entry<String, String> entry : unsentReadings.entrySet()) {
                    final String cmdResult = entry.getValue();
                    cmdName= entry.getKey();
                    // TODO the code should be flexible so to be able to decide what to display and where directly from the interface
                    if (AvailableCommandNames.SPEED.getValue().equals(cmdName))
                        row1Col1TextView.setText(cmdResult);
                        // row1 col2 is compass
                    else if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName))
                        row2Col1TextView.setText("RPM: "+cmdResult);
                        // row2 col2 is time
                    else if (AvailableCommandNames.FUEL_CONSUMPTION.getValue().equals(
                            cmdName))
                        row2Col3TextView.setText("Eco: "+cmdResult);
                    else
                        data_text_view.append(cmdName + ": " + cmdResult+"\n");
                }
                if (UPLOAD) {
                    double lat = ((LocationReceiver.currentLocation == null) ? 0d : LocationReceiver.currentLocation.getLatitude());
                    double lon = ((LocationReceiver.currentLocation == null) ? 0d : LocationReceiver.currentLocation.getLongitude());
                    ObdReading blockOfReadings = new ObdReading(lat, lon, System.currentTimeMillis(), "UNDEFINED_VIN", unsentReadings, vehicleId);
                    new UploadAsyncTask(blockOfReadings).execute();
                }
                // clear unsent readings
                unsentReadings.clear();
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
            Toast.makeText(this, "Bluetooth ok", Toast.LENGTH_SHORT).show();
        }


        // get Orientation sensor
        orientSensor = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
        if (orientSensor == null)
            showDialog(NO_ORIENTATION_SENSOR);

        //Added FC
        // We add the location service in any case. but we do not start it until the
        // preference on GPS is read in startCommand
        startLocationService();
        vehicleId = ConfigActivity.getVehicle_id(prefs);
        database = new Database(this);
    }


    /**
     * it starts the location services
     */
    void startLocationService() {
        LocationReceiver.locationClient = new LocationClient(this, this, this);
        // Create the LocationRequest object
        LocationReceiver.locationRequest = LocationRequest.create();
        // Set request for high accuracy
        LocationReceiver.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set update interval
        long updateInterval = ConfigActivity.getUpdatePeriod(prefs);
        LocationReceiver.locationRequest.setInterval(updateInterval);
        // Set fastest update interval that we can accept
        LocationReceiver.locationRequest.setFastestInterval(updateInterval);
        LocationReceiver.locationClient.connect();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (trace) Log.d(TAG, "Entered onStart...");

    }

    @Override
    protected void onStop() {
        super.onStop();
        // If the client is connected, remove location updates and disconnect
        if (LocationReceiver.locationClient.isConnected()) {
            LocationReceiver.locationClient.removeLocationUpdates(locationIntent);
        }
        LocationReceiver.locationClient.disconnect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseWakeLockIfHeld();
        if (isServiceBound) {
            doUnbindService();
        }
        stopLocationService();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (trace) Log.d(TAG, "Pausing..");
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
        if (trace) Log.d(TAG, "Resuming..");
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
        if (trace) Log.d(TAG, "Starting live data..");
        UPLOAD = ConfigActivity.getUploadDataPreference(prefs);
        useImperialUnits= ConfigActivity.getImperialUnitPreference(prefs);
        if (ConfigActivity.getGPSPreference(prefs))
            startLocationUpdates();
        else stopLocationUpdates();

        doBindService();
        unsentReadings = new HashMap<String, String>();

        // start command execution
        new Handler().post(mQueueCommands);

        // screen won't turn off until wakeLock.release()
        wakeLock.acquire();
        // todo remove it when ready
        database.deleteAllEntries();
        sendAllUnsentData();

    }

    /**
     * it gets all the data stored in teh database (i.e. the one where teh server could not be reached)
     * and sends it again to the server
     */
    private void sendAllUnsentData() {
        if (!UPLOAD) return;
        List<String> dataList = database.getAllData();
        Gson gson = new GsonBuilder().create();
        for (String reading : dataList) {
            Type listType = new TypeToken<ObdReading>() {
            }.getType();
            ObdReading data = gson.fromJson(reading, listType);
            new UploadAsyncTask(data).execute();
        }
    }

    public void stopLiveData() {
        stopLiveData("");
    }

    public void stopLiveData(String error) {
        if (trace) Log.d(TAG, "Stopping live data..");
        doUnbindService();
        stopLocationUpdates();
        releaseWakeLockIfHeld();
        if (!error.equals(""))
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
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

        if (service != null && service.isRunning()) {
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
        // in case we lost track: it may happen in case of exception that prevents the lastCommand
        // to enter here
        if ((service!=null)&&(service.queueEmpty())){
            serviceBusy=false;
        }
        if (serviceBusy){
            if (trace) Log.d(TAG, "Service busy");
        }
        else if ((isServiceBound)&& (service.isRunning())) {
            //TODO take the ones enabled from the preference screen and enable only those who are checked
            AmbientAirTemperatureObdCommand ambaircomm = new AmbientAirTemperatureObdCommand();
            ambaircomm.useImperialUnits(useImperialUnits);
            final ObdCommandJob airTemp = new ObdCommandJob(ambaircomm);
            EngineCoolantTemperatureObdCommand ecoolcomm = new EngineCoolantTemperatureObdCommand();
            ecoolcomm.useImperialUnits(useImperialUnits);
            final ObdCommandJob ecool = new ObdCommandJob(ecoolcomm);
            final ObdCommandJob baro = new ObdCommandJob(new BarometricPressureObdCommand());
            final ObdCommandJob eload = new ObdCommandJob(new EngineLoadObdCommand());
            // todo reinstate when error in obd library is fixed this returns an error when formatted
            //            final ObdCommandJob ftype = new ObdCommandJob(new FindFuelTypeObdCommand());
            final ObdCommandJob mflow = new ObdCommandJob(new MassAirFlowObdCommand());
            final FuelConsumptionRateObdCommand fuelConCommand = new FuelConsumptionRateObdCommand();
            fuelConCommand.useImperialUnits(useImperialUnits);
            final ObdCommandJob fcons = new ObdCommandJob(fuelConCommand);
            // todo reinstate when error in obd library is fixed this returns an error when formatted
            //            final ObdCommandJob disttrav = new ObdCommandJob(new DistanceTraveledSinceCodesClearedObdCommand());
            final ObdCommandJob ert = new ObdCommandJob(new EngineRuntimeObdCommand());
            final ObdCommandJob fpress = new ObdCommandJob(new FuelPressureObdCommand());
            final ObdCommandJob impress = new ObdCommandJob(new IntakeManifoldPressureObdCommand());
            final ObdCommandJob fuelLevel = new ObdCommandJob(new FuelLevelObdCommand());
            SpeedObdCommand obdSpeed = new SpeedObdCommand();
            obdSpeed.useImperialUnits(useImperialUnits);
            final ObdCommandJob speed = new ObdCommandJob(obdSpeed);
            FuelEconomyObdCommand obdFuel = new FuelEconomyObdCommand();
            obdFuel.useImperialUnits(useImperialUnits);
            final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());

            // write here what the last command is: used to pack a number of results before sending to server
            lastCommand = new FuelLevelObdCommand().getName();

            // todo for now just comment the ones that you do not want
            service.queueJob(eload);
            // todo reinstate when error corrected
            //            service.queueJob(disttrav);
            service.queueJob(ert);
            // todo reinstate when error corrected
            //            service.queueJob(ftype);
            service.queueJob(fpress);
            service.queueJob(mflow);
            service.queueJob(impress);
            service.queueJob(fcons);
            service.queueJob(speed);
            service.queueJob(rpm);
            service.queueJob(airTemp);
            service.queueJob(baro);
            service.queueJob(ecool);

            // NOTE: the last command must be the same as the one declared in lastCommand!!
            // NOTE: the last command must be the same as the one declared in lastCommand!!
            // NOTE: the last command must be the same as the one declared in lastCommand!!
            service.queueJob(fuelLevel);
            // now stop asking for update until we have finished the current assignment
            serviceBusy=true;
        }

    }

    private void doBindService() {
        if (!isServiceBound) {
            if (trace) Log.d(TAG, "Binding OBD service..");
            if ((preRequisites)&& (!excludeOBDReaderForDebugging)) {
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
            if (trace) Log.d(TAG, "Unbinding OBD service..");
            unbindService(serviceConn);
            isServiceBound = false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // probably not necessary
        if (!LocationReceiver.locationClient.isConnected())
            return;
        GPSready = true;
        Intent intent2 = new Intent(this, LocationReceiver.class);
        locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 14872, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        startLocationUpdates();
    }

    private void stopLocationService() {
        if ((ConfigActivity.getGPSPreference(prefs)) && (LocationReceiver.locationClient != null)) {
            stopLocationUpdates();
            LocationReceiver.locationClient.disconnect();
        }
    }


    int howManyTimesWaitedForGps=0;
    private void startLocationUpdates() {
        // if the location service is not ready, wait
        while (!GPSready)
            try {
                Thread.sleep(200);
                if (trace) Log.d(TAG, "waiting for GPS..." + howManyTimesWaitedForGps);
                if (howManyTimesWaitedForGps++ > 20)
                    throw new InterruptedException();
            } catch (InterruptedException e) {
                Log.e(TAG, "GPS taking too long. stopping searching!");
                Toast.makeText(this, "GPS taking too long. stopping searching!", Toast.LENGTH_LONG).show();
                return;
            }
        LocationReceiver.activeLocationUpdates = true;
        try {
            locationListener.startLocationUpdates();
            LocationReceiver.locationClient.requestLocationUpdates(LocationReceiver.locationRequest, locationIntent);
            LocationReceiver.currentLocation = (LocationReceiver.locationClient != null) ? LocationReceiver.locationClient.getLastLocation() : null;
            // here we should register the sensor
        } catch (Exception e) {
            if (trace) Log.e("onConnected", "There is no location");
        }
    }

    private void stopLocationUpdates() {
        if (LocationReceiver.locationClient.isConnected())
            LocationReceiver.locationClient.removeLocationUpdates(locationIntent);
    }

    @Override
    public void onDisconnected() {
        stopLocationService();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void ELMNotAvailable() {
        Toast.makeText(this, "ObdPort Unreachable. Is your Obd Interface connected to the OBD Port?", Toast.LENGTH_LONG).show();
    }

    /**
     * Uploading asynchronous task
     */
    private class UploadAsyncTask extends AsyncTask<Integer, Integer, Long> {
        HttpResponse response;
        String readings;

        private UploadAsyncTask(ObdReading readings) {
            // it may be unnecessary but we GSon the readings
            Gson gson = new Gson();
            this.readings = gson.toJson(readings);
        }

        @Override
        protected Long doInBackground(Integer... integers) {
            if (trace) Log.d(TAG, "Uploading readings..");
            //ConfigActivity.getUploadURL(prefs)
            // instantiate reading service client
            // upload readings
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(ConfigActivity.getUploadURL(prefs));
            try {
                httppost.setEntity(new ByteArrayEntity(readings.getBytes("UTF8")));
                response = httpclient.execute(httppost);
            } catch (UnsupportedEncodingException e) {
                // will be taken care of in onPostExecute
            } catch (IOException e) {
                // will be taken care of in onPostExecute
            }
            if (trace) Log.d(TAG, "Done");
            return (response!=null)? (long) response.getStatusLine().getStatusCode():200L;
        }

        /**
         * This is called when doInBackground() is finished
         *
         * @param result
         */
        protected void onPostExecute(Long result) {
            if ((response == null)) {
                // store into database
                database.addData(readings);
                if (trace) Log.d("TAG", "Server unreachable");
            } else if ((response.getStatusLine().getStatusCode() != 200)) {
//                    outPane.append("\nError: " + response.getStatusLine().getReasonPhrase() + "\n" + foundWhat + "\n\n");
                if (trace) Log.d("TAG", "\nError: " + response.getStatusLine().getStatusCode() + "\n\n");
                // store into database
                database.addData(readings);

            } else {

//                    outPane.append("\nData Sent to Server");
                if (trace) Log.d("TAG", "Sent to server");

            }
        }
    }


}
