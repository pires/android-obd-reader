/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * Read the throttle position in percentage.
 */
public class ThrottlePositionObdCommand extends ObdCommand {

	/**
	 * Default ctor.
	 */
	public ThrottlePositionObdCommand() {
		super("01 11");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public ThrottlePositionObdCommand(ThrottlePositionObdCommand other) {
		super(other);
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

	@Override
	public String getName() {
		return "Throttle Position";
	}
}