package eu.lighthouselabs.obd.commands;

public abstract class PressureObdCommand extends OBDCommand implements
		SystemOfUnits {

	protected int tempValue = 0;

	/**
	 * Default ctor
	 * 
	 * @param cmd
	 */
	public PressureObdCommand(String cmd) {
		super(cmd);
	}

	/**
	 * Copy ctor.
	 * 
	 * @param cmd
	 */
	public PressureObdCommand(PressureObdCommand other) {
		super(other);
	}

	/**
	 * Some PressureObdCommand subclasses will need to implement this method in
	 * order to determine the final kPa value.
	 * 
	 * *NEED* to read tempValue
	 * 
	 * @return
	 */
	protected abstract int preparePressureValue();

	/**
	 * 
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			tempValue = b1 << 8;
			int value = preparePressureValue(); // this will need tempValue
			res = String.format("%d %s", value, "kPa");

			if (useImperialUnits) {
				res = String.format("%.1f %s", getImperialUnit(value), "psi");
			}
		}

		return res;
	}

	/**
	 * Convert kPa to psi
	 */
	public float getImperialUnit(float value) {
		//value * 0.000145037;
		Double d = value / 101.3;
		return Float.valueOf(d.toString());
	}
}
