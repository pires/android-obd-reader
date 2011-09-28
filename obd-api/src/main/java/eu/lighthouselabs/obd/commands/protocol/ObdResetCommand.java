/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands.protocol;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * This method will reset the OBD connection.
 * 
 * TODO complete implementation
 */
public class ObdResetCommand extends ObdCommand {

	/**
	 * @param command
	 */
	public ObdResetCommand() {
		super("AT Z");
	}

	/**
	 * @param other
	 */
	public ObdResetCommand(ObdResetCommand other) {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.lighthouselabs.obd.commands.ObdCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		return getResult();
	}

	@Override
	public String getName() {
		return "Reset OBD";
	}

}