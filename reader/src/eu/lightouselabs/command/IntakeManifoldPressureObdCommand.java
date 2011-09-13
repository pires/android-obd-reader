package org.obdreader.command;

public class IntakeManifoldPressureObdCommand extends PressureObdCommand {

	public IntakeManifoldPressureObdCommand() {
		super("010B","Intake Manifold Press","kPa","atm");
	}
	public IntakeManifoldPressureObdCommand(IntakeManifoldPressureObdCommand other) {
		super(other);
	}
}
