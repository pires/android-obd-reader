/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

/**
 * This method will reset the OBD connection.
 * 
 * TODO complete implementation
 */
public class OBDResetCommand extends OBDCommand {

	/**
	 * @param command
	 */
	public OBDResetCommand() {
		super("AT Z");
	}

	/**
	 * @param other
	 */
	public OBDResetCommand(OBDResetCommand other) {
		super(other);
	}

	/* (non-Javadoc)
	 * @see eu.lighthouselabs.obd.commands.OBDCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		// TODO implement this
		return null;
	}

}