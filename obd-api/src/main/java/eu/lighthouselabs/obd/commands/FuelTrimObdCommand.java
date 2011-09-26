/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 * 
 */
public class FuelTrimObdCommand extends OBDCommand {

	/**
	 * Default ctor.
	 */
	public FuelTrimObdCommand() {
		super("01 07");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public FuelTrimObdCommand(FuelTrimObdCommand other) {
		super(other);
	}

	/**
	 * TODO is this needed?
	 * 
	 * @param value
	 * @return
	 */
	private int prepareTempValue(int value) {
		Double perc = (value - 128) * (100.0 / 128);
		return Integer.parseInt(perc.toString());
	}

	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			res = String.format("%.2f %s", prepareTempValue(b1 << 8), "%");
		}

		return res;
	}
}