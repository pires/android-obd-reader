package org.obdreader.command;

public class FuelEconomyLTFTObdCommand extends FuelEconomyObdCommand {

	public FuelEconomyLTFTObdCommand() {
		super("","Fuel Economy LTFT","mpg");
	}
	public FuelEconomyLTFTObdCommand(FuelEconomyLTFTObdCommand other) {
		super(other);
	}
	public void run() {
		try {
			super.run();
			FuelTrimObdCommand ltft = new FuelTrimObdCommand();
			runCmd(ltft);
			ltft.formatResult();
			double stftV = (double)ltft.getInt() / 100.0;
			rawValue = (Double)rawValue * (1 + stftV);
		} catch (Exception e) {
			setError(e);
		}
	}
}
