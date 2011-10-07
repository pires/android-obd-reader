/*
 * TODO put header
 */

package eu.lighthouselabs.obd.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * TODO put description
 */
public abstract class ObdCommand {

	protected ArrayList<Byte> buff = null;
	protected String cmd = null;
	protected boolean useImperialUnits = false;
	protected String unformattedResult = null;

	/**
	 * Default ctor to use
	 * 
	 * @param command
	 *            the command to send
	 */
	public ObdCommand(String command) {
		this.cmd = command;
		this.buff = new ArrayList<Byte>();
	}

	/**
	 * Prevent empty instantiation
	 */
	private ObdCommand() {
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 *            the ObdCommand to copy.
	 */
	public ObdCommand(ObdCommand other) {
		this(other.cmd);
	}

	/**
	 * Sends the OBD-II request and deals with the response.
	 * 
	 * This method CAN be overriden in fake commands.
	 */
	public void run(InputStream in, OutputStream out) throws IOException,
			InterruptedException {
		sendCommand(out);
		readResult(in);
	}

	/**
	 * Sends the OBD-II request.
	 * 
	 * This method may be overriden in subclasses, such as ObMultiCommand or
	 * TroubleCodesObdCommand.
	 * 
	 * @param cmd
	 *            The command to send.
	 */
	protected void sendCommand(OutputStream out) throws IOException,
			InterruptedException {
		// add the carriage return char
		cmd += "\r";

		// write to OutputStream, or in this case a BluetoothSocket
		out.write(cmd.getBytes());
		out.flush();

		/*
		 * HACK GOLDEN HAMMER ahead!!
		 * 
		 * TODO clean
		 * 
		 * Due to the time that some systems may take to respond, let's give it
		 * 500ms.
		 */
		// Thread.sleep(250);
	}

	/**
	 * Resends this command.
	 * 
	 * 
	 */
	protected void resendCommand(OutputStream out) throws IOException,
			InterruptedException {
		out.write("\r".getBytes());
		out.flush();
		/*
		 * HACK GOLDEN HAMMER ahead!!
		 * 
		 * TODO clean this
		 * 
		 * Due to the time that some systems may take to respond, let's give it
		 * 500ms.
		 */
		// Thread.sleep(250);
	}

	/**
	 * Reads the OBD-II response.
	 * 
	 * This method may be overriden in subclasses, such as ObdMultiCommand.
	 */
	protected void readResult(InputStream in) throws IOException {
		byte b = 0;
		this.buff.clear();

		// read until '>' arrives
		while ((char) (b = (byte) in.read()) != '>')
			this.buff.add(b);
	}

	/**
	 * If an unformatted result is not available, it will be prepared before
	 * returning.
	 * 
	 * @return the raw command response in string representation.
	 */
	protected String getResult() {
		if (unformattedResult == null)
			prepareResult();

		return unformattedResult;
	}

	/**
	 * Prepare raw result.
	 */
	protected void prepareResult() {
		/*
		 * Parse buffer to string
		 */
		StringBuilder sb = new StringBuilder();
		for (byte b : this.buff)
			sb.append((char) b);

		/*
		 * Process result
		 */

		// validate empty string
		String temp = sb.toString();
		if (!"".equals(temp)) {
			// split response lines
			String[] resultArray = temp.split("\r");

			// validate lines and determine result
			if (resultArray.length > 0) {
				/*
				 * The following will happen when first command after
				 * auto-protocol search is set.
				 */
				if ("SEARCHING...".equals(resultArray[0])) {
					unformattedResult = resultArray[1].replace(" ", "");
				} else {
					unformattedResult = resultArray[0].replace(" ", "");
				}
			}
		}
		// TODO what happens when sb is empty?
	}

	/**
	 * @return a formatted command response in string representation.
	 */
	public abstract String getFormattedResult();

	/******************************************************************
	 * Getters & Setters
	 */

	/**
	 * Returns this command response in Byte format.
	 * 
	 * @return a list of Byte
	 */
	public ArrayList<Byte> getBuff() {
		return buff;
	}

	/**
	 * Returns this command in string representation.
	 * 
	 * @return the command
	 */
	public String getCommand() {
		return cmd;
	}

	/**
	 * @return true if imperial units are used, or false otherwise
	 */
	public boolean useImperialUnits() {
		return useImperialUnits;
	}

	/**
	 * Set to 'true' if you want to use imperial units, false otherwise. By
	 * default this value is set to 'false'.
	 * 
	 * @param isImperial
	 */
	public void useImperialUnits(boolean isImperial) {
		this.useImperialUnits = isImperial;
	}

	/**
	 * @return the OBD command name.
	 */
	public abstract String getName();

}