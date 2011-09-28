/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * TODO put description
 * 
 * Mass Air Flow
 */
public class MassAirFlowObdCommand extends ObdCommand {

	private double maf = -9999.0;

	/**
	 * Default ctor.
	 */
	public MassAirFlowObdCommand() {
		super("01 10");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public MassAirFlowObdCommand(MassAirFlowObdCommand other) {
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
			byte b2 = buff.get(3);
			maf = (((b1 << 8) | b2) & 0xFFFF) / 100.0f;
			res = String.format("%.2f%s", maf, "g/s");
		}

		return res;
	}

	/**
	 * @return MAF value for further calculus.
	 */
	public double getMAF() {
		return maf;
	}

	@Override
	public String getName() {
		return "Mass Air Flow";
	}
}