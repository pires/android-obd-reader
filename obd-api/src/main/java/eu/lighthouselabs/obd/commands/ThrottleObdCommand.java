/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 * 
 * Throttle Position
 */
public class ThrottleObdCommand extends OBDCommand {

	/**
	 * Default ctor.
	 */
	public ThrottleObdCommand() {
		super("01 11");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public ThrottleObdCommand(ThrottleObdCommand other) {
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
			byte b1 = Byte.parseByte(res.substring(4, 6));
			byte b2 = Byte.parseByte(res.substring(6, 8));
			int tempValue = (b1 << 8) | b2;
			res = String.format("%.1f %s", (tempValue * 100) / 255, "%");
		}

		return res;
	}
}