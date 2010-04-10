package org.obdreader.activity;

import java.util.ArrayList;
import java.util.Set;

import org.obdreader.R;
import org.obdreader.command.ObdCommand;
import org.obdreader.config.ObdConfig;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class ObdReaderConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
	public static final String UPLOAD_URL_KEY = "upload_url_preference";
	public static final String UPLOAD_DATA_KEY = "upload_data_preference";
	public static final String UPDATE_PERIOD_KEY = "update_period_preference";
	public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
	public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
	public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
	public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
	public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
	public static final String ENABLE_GPS_KEY = "enable_gps_preference";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
        ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
		ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);
		String[] prefKeys = new String[]{ENGINE_DISPLACEMENT_KEY,VOLUMETRIC_EFFICIENCY_KEY,UPDATE_PERIOD_KEY}; 
		for (String prefKey:prefKeys) {
			EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen().findPreference(prefKey);
			txtPref.setOnPreferenceChangeListener(this);
		}
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	listPref.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listPref.setEntryValues(vals.toArray(new CharSequence[0]));
        	return;
        }
        final Activity thisAct = this;
        listPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (mBluetoothAdapter == null) {
					Toast.makeText(thisAct,"This device does not support bluetooth", Toast.LENGTH_LONG);
					return false;
				}
				return true;
			}
		});
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listPref.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listPref.setEntryValues(vals.toArray(new CharSequence[0]));
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen().findPreference(COMMANDS_SCREEN_KEY);
        for (int i = 0; i < cmds.size(); i++) {
        	ObdCommand cmd = cmds.get(i);
        	CheckBoxPreference cpref = new CheckBoxPreference(this);
        	cpref.setTitle(cmd.getDesc());
        	cpref.setKey(cmd.getDesc());
        	cpref.setChecked(true);
        	cmdScr.addPreference(cpref);
        }
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (UPDATE_PERIOD_KEY.equals(preference.getKey()) || 
				VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey()) ||
				ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())) {
			try {
				Double.parseDouble(newValue.toString());
				return true;
			} catch (Exception e) {
				Toast.makeText(this,"Couldn't parse '" + newValue.toString() + "' as an number.",Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}
    public static int getUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.getString(ObdReaderConfigActivity.UPDATE_PERIOD_KEY, "4");
        int period = 4000;
        try {
			period = Integer.parseInt(periodString) * 1000;
		} catch (Exception e) {
		}
    	if (period <= 0) {
    		period = 250;
    	}
    	return period;
    }
    public static double getVolumetricEfficieny(SharedPreferences prefs) {
    	String veString = prefs.getString(ObdReaderConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
    	double ve = 0.85;
    	try {
			ve = Double.parseDouble(veString);
		} catch (Exception e) {
		}
		return ve;
    }
    public static double getEngineDisplacement(SharedPreferences prefs) {
    	String edString = prefs.getString(ObdReaderConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
    	double ed = 1.6;
    	try {
			ed = Double.parseDouble(edString);
		} catch (Exception e) {
		}
		return ed;
    }
    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
    	ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
    	ArrayList<ObdCommand> ucmds = new ArrayList<ObdCommand>();
    	for (int i = 0; i < cmds.size(); i++) {
    		ObdCommand cmd = cmds.get(i);
    		boolean selected = prefs.getBoolean(cmd.getDesc(), true);
    		if (selected) {
    			ucmds.add(cmd);
    		}
    	}
    	return ucmds;
    }
}
