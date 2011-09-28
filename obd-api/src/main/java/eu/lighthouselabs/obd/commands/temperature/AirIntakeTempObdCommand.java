/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

/**
 * TODO
 * 
 * put description
 */
public class AirIntakeTempObdCommand extends TempObdCommand {

	public AirIntakeTempObdCommand() {
		super("01 0F");
	}

	public AirIntakeTempObdCommand(AirIntakeTempObdCommand other) {
		super(other);
	}

	@Override
	public String getName() {
		return "Air Intake Temperature";
	}
}