package org.obdreader.activity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obdreader.R;
import org.obdreader.command.ObdCommand;
import org.obdreader.config.ObdConfig;
import org.obdreader.io.ObdReaderService;
import org.obdreader.io.ObdReaderServiceConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nullwire.trace.ExceptionHandler;

public class ObdReaderMainActivity extends Activity {
	static final int NO_BLUETOOTH_ID = 0;
	static final int NO_GPS_ID = 1;
	static final int START_LIVE_DATA = 1;
	static final int STOP_LIVE_DATA = 2;
	static final int SETTINGS = 3;
	static final int COMMAND_ACTIVITY = 5;
	private Handler handler = null;
	private Intent serviceIntent = null;
	private ObdReaderServiceConnection serviceConn = null;
	private UpdateThread updater = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExceptionHandler.register(this,"http://www.whidbeycleaning.com/droid/server.php");
        setContentView(R.layout.main);
        handler = new Handler();
        serviceIntent = new Intent(this, ObdReaderService.class);
        serviceConn = new ObdReaderServiceConnection();
        bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
        	showDialog(NO_GPS_ID);
        	return;
        }
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	showDialog(NO_BLUETOOTH_ID);
        	return;
        }
    }
    private void updateConfig() {
    	Intent configIntent = new Intent(this,ObdReaderConfigActivity.class);
    	startActivity(configIntent);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
        menu.add(0, SETTINGS, 0, "Settings");
        menu.add(0, COMMAND_ACTIVITY, 0, "Run Command");
        menu.add(0, STOP_LIVE_DATA, 0, "Stop");
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case START_LIVE_DATA:
        	liveData();
            return true;
        case STOP_LIVE_DATA:
        	cancel();
        	return true;
        case SETTINGS:
        	updateConfig();
        	return true;
        case COMMAND_ACTIVITY:
        	staticCommand();
        	return true;
        }
        return false;
    }
    private void staticCommand() {
    	Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
    	startActivity(commandIntent);
    }
    private void liveData() {
    	if (!serviceConn.isRunning()) {
    		serviceConn.getService().startService();
    	}
    	updater = new UpdateThread();
    	updater.start();
    }
    private void cancel() {
    	stopService(serviceIntent);
    	if (serviceConn.isRunning()) {
    		serviceConn.getService().stopService();
    	}
    	if (updater != null) {
    		updater.stop = true;
    	}
    }
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder build = new AlertDialog.Builder(this);
    	switch(id) {
    	case NO_BLUETOOTH_ID:
            build.setMessage("Sorry, your device doesn't support bluetooth");
            return build.create();
    	case NO_GPS_ID:
            build.setMessage("Sorry, your device doesn't support gps");
            return build.create();
    	}
    	return null;
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem startItem = menu.findItem(START_LIVE_DATA);
    	MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
    	MenuItem settingsItem = menu.findItem(SETTINGS);
    	MenuItem commandItem = menu.findItem(COMMAND_ACTIVITY);
    	if (serviceConn.isRunning()) {
    		startItem.setEnabled(false);
    		stopItem.setEnabled(true);
    		settingsItem.setEnabled(false);
    		commandItem.setEnabled(false);
    	} else {
    		stopItem.setEnabled(false);
    		startItem.setEnabled(true);
    		settingsItem.setEnabled(true);
    		commandItem.setEnabled(true);
    	}
    	return true;
    }
    public void updateDataTable(final Map<String,String> dataMap) {
    	handler.post(new Runnable() {
			public void run() {
				setDataTableText(dataMap);
			}
		});
    }
    public void setDataTableText(Map<String,String> dataMap) {
    	TableLayout tl = (TableLayout) findViewById(R.id.data_table);
    	tl.removeAllViews();
    	Set<String> keySet = dataMap.keySet();
    	String[] keys = keySet.toArray(new String[0]);
    	Arrays.sort(keys);
    	for (String k:keys) {
    		addTableRow(tl,k,dataMap.get(k));
    	}
    	
    }
    private void addTableRow(TableLayout tl, String key, String val) {
    	TableRow tr = new TableRow(this);
		tr.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
		TextView name = new TextView(this);
		name.setGravity(Gravity.RIGHT);
		name.setText(key + ": ");
		TextView value = new TextView(this);
		value.setGravity(Gravity.LEFT);
		value.setText(val);
		tr.addView(name);
		tr.addView(value);
		tl.addView(tr,new TableLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
    }
    private class UpdateThread extends Thread {
    	boolean stop = false;
    	public void run() {
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ObdReaderMainActivity.this);
    		String vehicleId = prefs.getString(ObdReaderConfigActivity.VEHICLE_ID_KEY,"");
    		while (!stop && serviceConn.isRunning()) {
    			ObdReaderService svc = serviceConn.getService();
    			Map<String,String> dataMap = null;
    			if (svc == null || svc.getDataMap() == null) {
    				dataMap = new HashMap<String, String>();
    				for (ObdCommand cmd:ObdConfig.getCommands()) {
    					dataMap.put(cmd.getDesc(),"--");
    				}
    			} else {
    				dataMap = svc.getDataMap();
    			}
    			if (vehicleId != null && !"".equals(vehicleId.trim())) {
        			dataMap.put("Vehicle ID", vehicleId);
        		}
    			updateDataTable(dataMap);
    			try {
					Thread.sleep(ObdReaderConfigActivity.getUpdatePeriod(prefs));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
}
