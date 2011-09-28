/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.control;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * TODO put description
 * 
 * Timing Advance
 */
public class TimingAdvanceObdCommand extends ObdCommand {

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
			float tempValue = (buff.get(2) & 0xFF) / 2.0f - 64.0f;
			res = String.format("%.1f %s", tempValue, "%");
		}

		return res;
	}
}