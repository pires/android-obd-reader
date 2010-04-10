package org.obdreader.command;

public class FuelEconomyObdCommand extends ObdCommand {

	public static final double AIR_FUEL_RATIO = 14.64;
	public static final double FUEL_DENSITY_GRAMS_PER_LITER = 720.0;
	protected double fuelEcon = 0.0;

	public FuelEconomyObdCommand(String cmd, String desc, String resType, String impType) {
		super(cmd,desc,resType,impType);
	}
	public FuelEconomyObdCommand() {
		super("","Fuel Economy","kml","mpg");
	}
	public FuelEconomyObdCommand(FuelEconomyObdCommand other) {
		super(other);
	}
	public void run() {
		try {
			MassAirFlowObdCommand maf = new MassAirFlowObdCommand();
			SpeedObdCommand speed = new SpeedObdCommand();
			runCmd(maf);
			maf.formatResult();
			double mafV = maf.getMAF();
			runCmd(speed);
			speed.formatResult();
			double speedV = (double)speed.getInt();
			fuelEcon = (14.7  * 6.17 * 454.0 * speedV * 0.621371) / (3600.0 * mafV);
		} catch (Exception e) {
			setError(e);
		}
	}
	public void runCmd(ObdCommand cmd) {
		cmd.setInputStream(in);
		cmd.setOutputStream(out);
		cmd.start();
		try {
			cmd.join();
		} catch (InterruptedException e) {
			setError(e);
		}
	}
	public String formatResult() {
		if (!isImperial()) {
			double kml = fuelEcon * 0.354013;
			return String.format("%.1f %s", kml, resType);
		}
		return String.format("%.1f %s", fuelEcon, impType);
	}
}
