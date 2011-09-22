package eu.lighthouselabs.obd.reader.command;

public class FuelPressureObdCommand extends PressureObdCommand{

	public FuelPressureObdCommand() {
		super("010A","Fuel Press","kPa","atm");
	}
	public FuelPressureObdCommand(FuelPressureObdCommand other) {
		super(other);
	}
	public int transform(int b) {
		return b*3;
	}
}
