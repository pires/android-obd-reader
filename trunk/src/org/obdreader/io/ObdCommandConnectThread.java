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
			activity.logMsg("Starting device...");
			startDevice();
			activity.logMsg("Device started, running " + cmd.getCmd() + "...");
			String res = runCommand(cmd);
			String rawRes = cmd.getResult().replace("\r","\\r").replace("\n","\\n");
			activity.logMsg("Raw result is '" + rawRes + "'");
			results.put(cmd.getDesc(), res);
		} catch (Exception e) {
			activity.logMsg("Error running command: " + e.getMessage() + ", result was: '" + cmd.getResult() + "'");
			activity.logMsg("Error was: " + getStackTrace(e));
		} finally {
			close();
		}
	}
}
