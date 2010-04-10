package org.obdreader.command;

public class TempObdCommand extends IntObdCommand{

	public TempObdCommand(String cmd, String desc, String resType, String impType) {
		super(cmd, desc, resType, impType);
	}
	public TempObdCommand(TempObdCommand other) {
		super(other);
	}
	protected int transform(int b) {
		return b-40;
	}
	@Override
	public int getImperialInt() {
		return (intValue*9/5) + 32;
	}
}
