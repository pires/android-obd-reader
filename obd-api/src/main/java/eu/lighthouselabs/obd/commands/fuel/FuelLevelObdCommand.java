/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands.fuel;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * Get fuel level in percentage
 */
public class FuelLevelObdCommand extends ObdCommand {

	private float fuelLevel = 0f;

	/**
	 * @param command
	 */
	public FuelLevelObdCommand() {
		super("01 2F");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.lighthouselabs.obd.commands.ObdCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			int temp = buff.get(2) & 0xFF; // unsigned short
			fuelLevel = 100.0f * temp / 255.0f;
			res = String.format("%.1f%s", fuelLevel, "%");
		}

		return res;
	}

}