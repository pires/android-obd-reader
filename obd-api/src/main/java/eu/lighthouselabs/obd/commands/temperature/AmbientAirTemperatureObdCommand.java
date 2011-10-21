/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

/**
 * Ambient Air Temperature. 
 */
public class AmbientAirTemperatureObdCommand extends TemperatureObdCommand {

	/**
	 * @param cmd
	 */
	public AmbientAirTemperatureObdCommand() {
		super("01 46");
	}

	/**
	 * @param other
	 */
	public AmbientAirTemperatureObdCommand(TemperatureObdCommand other) {
		super(other);
	}

	@Override
    public String getName() {
		return "Ambient Air Temperature";
    }

}