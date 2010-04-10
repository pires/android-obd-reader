package org.obdreader.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.obdreader.R;
import org.obdreader.command.ObdCommand;
import org.obdreader.config.ObdConfig;
import org.obdreader.io.ObdCommandConnectThread;
import org.obdreader.io.ObdConnectThread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ObdReaderCommandActivity extends Activity implements OnItemSelectedListener {

	final private ArrayList<ObdCommand> commands = ObdConfig.getAllCommands();
	private Map<String,ObdCommand> cmdMap = null;
	protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private Handler handler = null;
	private ObdCommand selectedCmd = null;

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.command);
		Spinner cmdSpin = (Spinner) findViewById(R.id.command_spinner);
		cmdSpin.setOnItemSelectedListener(this);
		cmdMap = new HashMap<String,ObdCommand>();
		ArrayList<String> cmdStrs = new ArrayList<String>();
		for (ObdCommand cmd:commands) {
			cmdMap.put(cmd.getDesc(), cmd);
			cmdStrs.add(cmd.getDesc());
		}
		ArrayAdapter<String> adapt = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, cmdStrs);
		cmdSpin.setAdapter(adapt);
		handler = new Handler();
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Spinner cmdSpin = (Spinner) findViewById(R.id.command_spinner);
		String cmdDesc = cmdSpin.getSelectedItem().toString();
		ObdCommand cmd = cmdMap.get(cmdDesc);
		//hack to keep this from running the first time
		if (this.selectedCmd == null) {
			this.selectedCmd = cmd;
			return;
		}
		ObdReaderCommandActivityWorkerThread worker = null;
		try {
			worker = new ObdReaderCommandActivityWorkerThread(ObdConnectThread.getCopy(cmd));
		} catch (Exception e) {
			Toast.makeText(this,"Error copying command: " + e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		worker.start();
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	public void showMessage(final String message) {
		final Activity act = this;
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(act, message, Toast.LENGTH_LONG).show();
			}
		});
	}
	private void setText(final String value, final boolean clear) {
		handler.post(new Runnable() {
			public void run() {
				TextView txtView = (TextView) findViewById(R.id.command_result_text);
				String curr = (String) txtView.getText();
				if (curr == null || clear || "".equals(curr)) {
					curr = value;
				} else {
					curr = curr + "\n" + value;
				}
				txtView.setText(curr);
			}
		});
	}
	public void logMsg(final String msg) {
		setText(msg,false);
	}
	private class ObdReaderCommandActivityWorkerThread extends Thread {
		ObdCommand cmd = null;
		private ObdReaderCommandActivityWorkerThread(ObdCommand cmd) {
			this.cmd = cmd;
		}
		public void run() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ObdReaderCommandActivity.this);
			String devString = prefs.getString(ObdReaderConfigActivity.BLUETOOTH_LIST_KEY, null);
			final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    	if (mBluetoothAdapter == null) {
	    		showMessage("This device does not support bluetooth");
	    		return;
	        }
	    	if (devString == null || "".equals(devString)) {
	    		showMessage("No bluetooth device selected");
	    		return;
	    	}
			BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(devString);
			double ve = ObdReaderConfigActivity.getVolumetricEfficieny(prefs);
			double ed = ObdReaderConfigActivity.getEngineDisplacement(prefs);
			boolean imperialUnits = prefs.getBoolean(ObdReaderConfigActivity.IMPERIAL_UNITS_KEY, false);
			final ObdCommandConnectThread thread = new ObdCommandConnectThread(dev, ObdReaderCommandActivity.this, cmd, ed, ve, imperialUnits);
			thread.setVolumetricEfficiency(ve);
			thread.setEngineDisplacement(ed);
			setText("Running...", true);
			try {
				thread.start();
				thread.join();
			} catch (Exception e1) {
				showMessage("Error getting connection: " + e1.getMessage());
			}
			String res = null;
			if (!thread.getResults().containsKey(cmd.getDesc())) {
				res = "Didn't get a result.";
			} else {
				res = thread.getResults().get(cmd.getDesc());
			}
			logMsg("Formated result: " + res);
		}
	}
}
