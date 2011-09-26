package eu.lighthouselabs.obd.commands;

public class FuelPressureObdCommand extends PressureObdCommand {

	public FuelPressureObdCommand() {
		super("010A");
	}

	public FuelPressureObdCommand(FuelPressureObdCommand other) {
		super(other);
	}

	/**
	 * TODO
	 * 
	 * put description of why we multiply by 3
	 * 
	 * @param temp
	 * @return
	 */
	protected final int preparePressureValue() {
		return tempValue * 3;
	}
}