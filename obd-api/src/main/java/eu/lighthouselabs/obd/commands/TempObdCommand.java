/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO
 * 
 * put description
 */
public class TempObdCommand extends OBDCommand implements SystemOfUnits {

	/**
	 * Default ctor.
	 * 
	 * @param cmd
	 */
	public TempObdCommand(String cmd) {
		super(cmd);
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public TempObdCommand(TempObdCommand other) {
		super(other);
	}

	/**
	 * TODO
	 * 
	 * put description of why we subtract 40
	 * 
	 * @param temp
	 * @return
	 */
	protected final int prepareTempValue(int temp) {
		return temp - 40;
	}

	/**
	 * TODO find how to determine if raw values are Metric or Imperial
	 * 
	 * @return Temperature in Celsius or Fahrenheit.
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();
		float value = 0f;

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			value = prepareTempValue((b1 << 8) / 4);
		}

		if (useImperialUnits)
			res = String.format("%.0f %s", getImperialUnit(value), "F");
		else
			res = String.format("%.1f %s", value, "C");

		return res;
	}

	/**
	 * Converts from Celsius to Fahrenheit.
	 */
	public float  getImperialUnit(float value) {
		return (value * (9 / 5)) + 32;
	}
	
}