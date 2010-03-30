package org.obdreader.command;

public class FuelEconomyObdCommand extends ObdCommand {

	public static final double AIR_FUEL_RATIO = 14.64;
	public static final double FUEL_DENSITY_GRAMS_PER_LITER = 720.0;

	public FuelEconomyObdCommand() {
		super("","Fuel Economy","mpg");
	}
	public FuelEconomyObdCommand(String cmd, String desc, String resType) {
		super(cmd, desc, resType);
	}
	public FuelEconomyObdCommand(FuelEconomyObdCommand other) {
		super(other);
	}
	public void run() {
		try {
			MassAirFlowObdCommand maf = new MassAirFlowObdCommand();
			SpeedObdCommand speed = new SpeedObdCommand();
//			CommandEquivRatioObdCommand equiv = new CommandEquivRatioObdCommand();
			runCmd(maf);
			maf.formatResult();
			double mafV = maf.getMAF();
			runCmd(speed);
			speed.formatResult();
			double speedV = (double)speed.getInt();
			/*runCmd(equiv);
			equiv.formatResult();
			double equivV = equiv.getRatio();*/
			rawValue = (14.7  * 6.17 * 454.0 * speedV * 0.621371) / (3600.0 * mafV);
			/*double airFuel = AIR_FUEL_RATIO * equivV;
			double flowGramsSec = mafV / airFuel;
			double flowLiterSec = flowGramsSec / FUEL_DENSITY_GRAMS_PER_LITER;
			double flowGallonsSec = flowLiterSec * 0.26417;
			double flowGallonsHour = flowGallonsSec * 3600.0;
			if (flowGallonsHour <= 0) {
				return;
			}
			fuelEconomy = speedV / flowGallonsHour;*/
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
		return String.format("%.1f %s", rawValue, resType);
	}
}
