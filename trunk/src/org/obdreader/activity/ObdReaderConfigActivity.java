package org.obdreader.activity;

import java.util.ArrayList;
import java.util.Set;

import org.obdreader.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class ObdReaderConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
	public static final String UPLOAD_URL_KEY = "upload_url_preference";
	public static final String UPLOAD_DATA_KEY = "upload_data_preference";
	public static final String UPDATE_PERIOD_KEY = "update_period_preference";
	public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
        ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
		ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);
		EditTextPreference periodPref = (EditTextPreference) getPreferenceScreen().findPreference(UPDATE_PERIOD_KEY);
		periodPref.setOnPreferenceChangeListener(this);
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
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (UPDATE_PERIOD_KEY.equals(preference.getKey())) {
			try {
				Integer.parseInt(newValue.toString());
				return true;
			} catch (Exception e) {
				Toast.makeText(this,"Couldn't parse '" + newValue.toString() + "' as an integer.",Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}
}
