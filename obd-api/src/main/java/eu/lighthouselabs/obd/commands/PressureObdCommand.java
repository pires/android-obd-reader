/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 */
public abstract class PressureObdCommand extends ObdCommand implements
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
			tempValue = buff.get(2) & 0xFF; // unsigned short
			int value = preparePressureValue(); // this will need tempValue
			res = String.format("%d%s", value, "kPa");

			if (useImperialUnits) {
				res = String.format("%.1f%s", getImperialUnit(value), "psi");
			}
		}

		return res;
	}

	/**
	 * Convert kPa to psi
	 */
	public float getImperialUnit(float value) {
		Double d = value * 0.145037738;
		return Float.valueOf(d.toString());
	}
}