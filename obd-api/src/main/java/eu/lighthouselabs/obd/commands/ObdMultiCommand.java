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
public class ObdMultiCommand {

	private ArrayList<OBDCommand> commands;
	
	/**
	 * Default ctor.
	 */
	public ObdMultiCommand() {
		this.commands = new ArrayList<OBDCommand>();
	}
	
	/**
	 * Add OBDCommand to list of ObdCommands.
	 * 
	 * @param command
	 */
	public void add(OBDCommand command) {
		this.commands.add(command);
	}
	
	/**
	 * Removes OBDCommand from the list of ObdCommands.
	 * @param command
	 */
	public void remove(OBDCommand command) {
		this.commands.remove(command);
	}
	
	/**
	 * Iterate all commands and call:
	 * 	- sendCommand()
	 *  - readResult()
	 */
	public void sendCommands(InputStream in, OutputStream out) throws IOException,
			InterruptedException {
		for (OBDCommand command : commands) {
			/*
			 * Send command and read response.
			 */
			command.run(in, out);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getFormattedResult() {
		StringBuilder res = new StringBuilder();
		
		for (OBDCommand command : commands) {
			res.append(command.getFormattedResult()).append(",");
		}
		
		return res.toString();
	}
}