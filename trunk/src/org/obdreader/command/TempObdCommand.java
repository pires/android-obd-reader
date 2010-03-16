package org.obdreader.command;

public class TempObdCommand extends IntObdCommand{

	public TempObdCommand(String cmd, String desc, String resType) {
		super(cmd, desc, resType);
	}
	public TempObdCommand(TempObdCommand other) {
		super(other);
	}
	protected int transform(int b) {
		return b-40;
	}
}
