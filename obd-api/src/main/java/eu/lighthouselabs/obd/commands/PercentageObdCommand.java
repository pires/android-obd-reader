/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * Abstract class for percentage commands.
 */
public abstract class PercentageObdCommand extends ObdCommand {

	/**
	 * @param command
	 */
	public PercentageObdCommand(String command) {
		super(command);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param other
	 */
	public PercentageObdCommand(ObdCommand other) {
		super(other);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = buff.get(2);
			float tempValue = ((b1 & 0xFF) * 100.0f) / 255.0f;
			res = String.format("%.1f%s", tempValue, "%");
		}

		return res;
	}

}