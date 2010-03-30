package org.obdreader.command;

public class FuelEconomySTFTObdCommand extends FuelEconomyObdCommand {

	public FuelEconomySTFTObdCommand() {
		super("","Fuel Economy STFT","mpg");
	}
	public FuelEconomySTFTObdCommand(FuelEconomySTFTObdCommand other) {
		super(other);
	}
	public void run() {
		try {
			super.run();
			FuelTrimObdCommand stft = new FuelTrimObdCommand("0106","Short Term Fuel Trim","%");
			runCmd(stft);
			stft.formatResult();
			double stftV = (double)stft.getInt() / 100.0;
			rawValue = (Double)rawValue * (1 + stftV);
		} catch (Exception e) {
			setError(e);
		}
	}
}
