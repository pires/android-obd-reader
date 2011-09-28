/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.fuel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.commands.control.CommandEquivRatioObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import eu.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import eu.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;

/**
 * TODO put description
 */
public class FuelEconomyMAPObdCommand extends FuelEconomyObdCommand {

	public static final double AIR_FUEL_RATIO = 14.64;
	public static final double FUEL_DENSITY_GRAMS_PER_LITER = 720.0;

	/**
	 * Default ctor.
	 */
	public FuelEconomyMAPObdCommand() {
		super();
	}
	
	/**
	 * As it's a fake command, neither do we need to send request or read
	 * response.
	 * 
	 * TODO implement
	 */
	@Override
	public void run(InputStream in, OutputStream out) throws IOException,
			InterruptedException {
			EngineRPMObdCommand rpm = new EngineRPMObdCommand();
			AirIntakeTemperatureObdCommand temp = new AirIntakeTemperatureObdCommand();
			SpeedObdCommand speed = new SpeedObdCommand();
			CommandEquivRatioObdCommand equiv = new CommandEquivRatioObdCommand();
			IntakeManifoldPressureObdCommand press = new IntakeManifoldPressureObdCommand();
		// double mafV = (imap/120.0) * ve * ed * 28.97 / 8.314;
		// String res =
		// String.format("%.1f rpm, %.1f speed, %.1f temp, %.1f press, %.1f maf",
		// rpmV, speedV, tempV, pressV, mafV);
		// for (int i = 0; i < res.length(); i ++) {
		// buff.add((byte)res.charAt(i));
		// }
		// fuelEcon = (14.7 * equivV * 6.17 * 454.0 * speedV * 0.621371) /
		// (3600.0 * mafV);
	}

}