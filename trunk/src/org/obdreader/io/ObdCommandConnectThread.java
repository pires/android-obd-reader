package org.obdreader.io;

import org.obdreader.activity.ObdReaderCommandActivity;
import org.obdreader.command.ObdCommand;

import android.bluetooth.BluetoothDevice;

public class ObdCommandConnectThread extends ObdConnectThread {

	private ObdCommand cmd = null;
	private ObdReaderCommandActivity activity = null;

	public ObdCommandConnectThread(BluetoothDevice dev, ObdReaderCommandActivity activity, ObdCommand cmd) {
		super(dev, null, null, null, 0);
		this.cmd = cmd;
		this.activity = activity;
	}

	public void run() {
		try {
			startDevice();
			String res = runCommand(cmd);
			results.put(cmd.getDesc(), res);
		} catch (Exception e) {
			activity.showMessage("Error running command: " + e.getMessage());
		} finally {
			close();
		}
	}
}
