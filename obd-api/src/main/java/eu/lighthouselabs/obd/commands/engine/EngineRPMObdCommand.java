/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * Displays the current engine revolutions per minute (RPM).
 */
public class EngineRPMObdCommand extends ObdCommand {

	/**
	 * Default ctor.
	 */
	public EngineRPMObdCommand() {
		super("01 0C");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public EngineRPMObdCommand(EngineRPMObdCommand other) {
		super(other);
	}

	/**
	 * @return the engine RPM per minute
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();
		int value = 0;

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [01 0C] of the response
			byte b1 = buff.get(2);
			byte b2 = buff.get(3);
			value = (((b1 << 8) | b2) & 0xFFFF) / 4;
		}

		return String.format("%d%s", value, "RPM");
	}

	@Override
	public String getName() {
		return "Engine RPM";
	}
}