package org.obdreader.command;

public class ThrottleObdCommand extends IntObdCommand {

	public ThrottleObdCommand(String cmd, String desc, String resType) {
		super(cmd, desc, resType);
	}
	public ThrottleObdCommand(ThrottleObdCommand other) {
		super(other);
	}
	protected int transform(int b) {
		return (int)((double)(b*100)/255.0);
	}
}
