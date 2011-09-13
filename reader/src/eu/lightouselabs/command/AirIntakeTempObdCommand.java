package org.obdreader.command;

public class AirIntakeTempObdCommand extends TempObdCommand{

	public AirIntakeTempObdCommand() {
		super("010F","Air Intake Temp","C","F");
	}
	public AirIntakeTempObdCommand(AirIntakeTempObdCommand other) {
		super(other);
	}
}
