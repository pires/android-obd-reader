/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * TODO put description
 */
public class EngineRuntimeObdCommand extends ObdCommand {

	/**
	 * Default ctor.
	 */
	public EngineRuntimeObdCommand() {
		super("01 1F");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public EngineRuntimeObdCommand(EngineRuntimeObdCommand other) {
		super(other);
	}

	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [01 0C] of the response
			byte b1 = buff.get(2);
			byte b2 = buff.get(3);
			int value = ((b1 << 8) | b2) & 0xFFFF;
			
			// determine time
			String hh = String.format("%02d", value / 3600);
			String mm = String.format("%02d", (value % 3600) / 60);
			String ss = String.format("%02d", value % 60);
			res = String.format("%s:%s:%s", hh, mm, ss);
		}

		return res;
	}
}