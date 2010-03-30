package org.obdreader.command;

public class SpeedObdCommand extends IntObdCommand {

	public SpeedObdCommand() {
		super("010D","Vehicle Speed","km/h");
	}
	public SpeedObdCommand(SpeedObdCommand other) {
		super(other);
	}
}
