package org.obdreader.command;

public class FuelTrimObdCommand extends IntObdCommand {

	public FuelTrimObdCommand(String cmd, String desc, String resType) {
		super(cmd,desc,resType,resType);
	}
	public FuelTrimObdCommand(FuelTrimObdCommand other) {
		super(other);
	}

	public FuelTrimObdCommand() {
		super("0107","Long Term Fuel Trim","%","%");
	}

	@Override
	public int transform(int b) {
		double perc = (b-128)*(100.0/128);
		return (int)perc;
	}
}
