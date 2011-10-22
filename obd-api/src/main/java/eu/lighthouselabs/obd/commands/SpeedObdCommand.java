/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 * 
 * Current speed.
 */
public class SpeedObdCommand extends ObdCommand implements SystemOfUnits {

	private int metricSpeed = 0;

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
		Double tempValue = value * 0.621371192;
		return Float.valueOf(tempValue.toString());
	}

	/**
	 * 
	 */
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			/*
			 * Ignore first two bytes [hh hh] of the response.
			 * 
			 * If the car is stopped, then we must not &0xFF or else metricSpeed
			 * would equal to 32.
			 */
			byte raw = buff.get(2);
			if (0x00 != raw)
				metricSpeed = raw & 0xFF; // unsigned short
			res = String.format("%d%s", metricSpeed, "km/h");

			if (useImperialUnits)
				res = String.format("%.2f%s", getImperialUnit(metricSpeed),
				        "mph");
		}

		return res;
	}

	/**
	 * @return the speed in metric units.
	 */
	public int getMetricSpeed() {
		return metricSpeed;
	}

	/**
	 * @return the speed in imperial units.
	 */
	public float getImperialSpeed() {
		return getImperialUnit(metricSpeed);
	}

	@Override
	public String getName() {
		return "Vehicle Speed";
	}

}