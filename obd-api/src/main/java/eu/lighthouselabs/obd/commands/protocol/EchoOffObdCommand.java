/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.protocol;

/**
 * This command will turn-off echo.
 */
public class EchoOffObdCommand extends ObdProtocolCommand {

	public EchoOffObdCommand() {
		super("AT E0");
	}

	/**
	 * @param other
	 */
	public EchoOffObdCommand(EchoOffObdCommand other) {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.lighthouselabs.obd.commands.ObdBaseCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		return getResult();
	}

	@Override
	public String getName() {
		return "Echo Off";
	}

}