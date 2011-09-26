/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 * 
 * Timing Advance
 */
public class TimingAdvanceObdCommand extends OBDCommand {

	public TimingAdvanceObdCommand() {
		super("01 0E");
	}

	public TimingAdvanceObdCommand(TimingAdvanceObdCommand other) {
		super(other);
	}

	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			res = String.format("%.1f %s", ((b1 << 8) / 2) - 64, "%");
		}

		return res;
	}
}