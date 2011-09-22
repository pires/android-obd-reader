/*
 * TODO put header
 */

package eu.lighthouselabs.obd.reader.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

//import eu.lighthouselabs.obd.reader.io.ObdConnectThread;

/**
 * Every time a command is created, a thread is instantiated in order to deal
 * with the execution of the same command. Also, the response is dealt with
 * here.
 * 
 * TODO Separate Command API from Thread management. This should be pooled
 * somewhere?
 */
public class ObdCommand extends Thread {

	private static final String TAG = "ObdCommand";

	protected InputStream in = null;
	protected OutputStream out = null;
	protected ArrayList<Byte> buff = null;
	protected String cmd = null;
	protected String desc = null;
	protected String resType = null;
	protected Exception error;
	protected Object rawValue = null;
	protected HashMap<String, Object> data = null;

	// protected ObdConnectThread connectThread = null;
	// protected String impType = null;

	public ObdCommand(String cmd, String desc, String resType/* , String impType */) {
		this.cmd = cmd;
		this.desc = desc;
		this.resType = resType;
		this.buff = new ArrayList<Byte>();
		this.impType = impType;
	}

	// public void setConnectThread(ObdConnectThread thread) {
	// this.connectThread = thread;
	// }

	// public boolean isImperial() {
	// if (connectThread != null && connectThread.getImperialUnits()) {
	// return true;
	// }
	// return false;
	// }

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 *            The ObdCommand to copy.
	 */
	public ObdCommand(ObdCommand other) {
		this(other.cmd, other.desc, other.resType, /*other.impType*/);
	}

	/**
	 * Set the input for this ObdCommand response.
	 * 
	 * @param in
	 */
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	/**
	 * Set the output for this ObdCommand request.
	 * 
	 * @param out
	 */
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	/**
	 * TODO put description
	 * 
	 * @param data
	 */
	public void setDataMap(HashMap<String, Object> data) {
		this.data = data;
	}

	/**
	 * Sends the OBD-II request and deals with the response.
	 */
	public void run() {
		sendCmd(cmd);
		readResult();
	}

	/**
	 * Sends the OBD-II request.
	 * 
	 * This method may be overriden in subclasses, such as
	 * ObMultiCommand or TroubleCodesObdCommand.
	 * 
	 * @param cmd
	 *            The command to send.
	 */
	protected void sendCmd(String cmd) {
		try {
			// add the carriage return char
			cmd += "\r";

			// write to OutputStream, or in this case a BluetoothSocket
			out.write(cmd.getBytes());
			out.flush();

			/*
			 * GOLDEN HAMMER ahead!!
			 * 
			 * Due to the time that some systems may take to respond, let's give
			 * it 500ms.
			 */
			Thread.sleep(500);
		} catch (IOException e) {
			Log.d(TAG, "Socket write error: " + e.getMessage());
			return;
		} catch (InterruptedException e) {
			Log.d(TAG, "Couldn't sleep on socket write: " + e.getMessage());
		}
	}

	/**
	 * Reads the OBD-II response.
	 * 
	 * This method may be overriden in subclasses, such as ObdMultiCommand.
	 */
	protected void readResult() {
		byte c = 0;
		this.buff.clear();
		try {
			while ((c = (byte) in.read()) != -1) {
				// exit loop if read char == prompt char
				if ((char) c == '>')
					break;
				
				this.buff.add(c);
			}
		} catch (IOException e) {
			Log.d(TAG, "Socket read error: " + e.getMessage());
			return;
		}
	}

	public String getResult() {
		StringBuilder sb = new StringBuilder();
		for (byte b : this.buff)
			sb.append((char) b);
		return sb.toString();
	}

	public String formatResult() {
		String[] res = getResult().split("\r");
		if ("SEARCHING...".equals(res[0]))
			return res[1];
		return res[0].replace(" ", "");
	}

	public InputStream getIn() {
		return in;
	}

	public OutputStream getOut() {
		return out;
	}

	public ArrayList<Byte> getBuff() {
		return buff;
	}

	public String getCmd() {
		return cmd;
	}

	public String getDesc() {
		return desc;
	}

	public String getResType() {
		return resType;
	}

	public void setError(Exception e) {
		error = e;
	}

	public Exception getError() {
		return error;
	}

	public Object getRawValue() {
		return rawValue;
	}
}