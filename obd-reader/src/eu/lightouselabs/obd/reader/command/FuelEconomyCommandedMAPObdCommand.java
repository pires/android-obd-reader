package org.obdreader.command;


public class FuelEconomyCommandedMAPObdCommand extends FuelEconomyObdCommand {

	public static final double AIR_FUEL_RATIO = 14.64;
	public static final double FUEL_DENSITY_GRAMS_PER_LITER = 720.0;

	public FuelEconomyCommandedMAPObdCommand() {
		super("","Fuel Economy Cmd. MAP","kml","mpg");
	}
	public FuelEconomyCommandedMAPObdCommand(FuelEconomyCommandedMAPObdCommand other) {
		super(other);
	}
	public void run() {
		try {
			EngineRPMObdCommand rpm = new EngineRPMObdCommand();
			AirIntakeTempObdCommand temp = new AirIntakeTempObdCommand();
			SpeedObdCommand speed = new SpeedObdCommand();
			IntakeManifoldPressureObdCommand press = new IntakeManifoldPressureObdCommand();
			runCmd(rpm);
			rpm.formatResult();
			double rpmV = rpm.getInt();
			runCmd(speed);
			speed.formatResult();
			double speedV = (double)speed.getInt();
			runCmd(temp);
			temp.formatResult();
			double tempV = temp.getInt() + 273.15; //convert to K
			runCmd(press);
			press.formatResult();
			double pressV = press.getInt();
			double imap = rpmV * pressV / tempV;
			double ve = 1.0;
			double ed = 1.0;
			if (connectThread != null) {
				ve = connectThread.getVolumetricEfficiency();
				ed = connectThread.getEngineDisplacement();
			}
			double mafV = (imap/120.0) * ve * ed * 28.97 / 8.314;
			String res = String.format("%.1f rpm, %.1f speed, %.1f temp, %.1f press, %.1f maf", rpmV, speedV, tempV, pressV, mafV);
			for (int i = 0; i < res.length(); i ++) {
				buff.add((byte)res.charAt(i));
			}
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
}
