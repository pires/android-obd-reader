/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 * 
 * Current speed.
 */
public class SpeedObdCommand extends OBDCommand implements SystemOfUnits {

	private int metricSpeed = -1;
	
	/**
	 * Default ctor.
	 */
	public SpeedObdCommand() {
		super("01 0D");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public SpeedObdCommand(SpeedObdCommand other) {
		super(other);
	}

	/**
	 * Convert from km/h to mph
	 */
	public float getImperialUnit(float value) {
		Double tempValue = value * 0.625;
		return Float.valueOf(tempValue.toString());
	}

	/**
	 * 
	 */
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			metricSpeed = b1 << 8;
			res = String.format("%d %s", metricSpeed, "km/h");

			if (useImperialUnits)
				res = String.format("%.1f %s", getImperialUnit(metricSpeed), "mph");
		}

		return res;
	}

	/**
	 * @return the speed in metric units.
	 */
	public int getMetricSpeed() {
		return metricSpeed;
	}
	
}