/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.fuel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;

/**
 * TODO put description
 */
public class FuelEconomyObdCommand extends ObdCommand {

	public static final double AIR_FUEL_RATIO = 14.64;
	public static final double FUEL_DENSITY_GRAMS_PER_LITER = 720.0;
	protected double fuelEcon = -9999.0;

	/**
	 * Default ctor.
	 */
	public FuelEconomyObdCommand() {
		super("");
	}

	/**
	 * As it's a fake command, neither do we need to send request or read
	 * response.
	 */
	@Override
	public void run(InputStream in, OutputStream out) throws IOException,
			InterruptedException {
		MassAirFlowObdCommand mafCommand = new MassAirFlowObdCommand();
		mafCommand.run(in, out);

		// call in order to calculate mafCommand.getMAF()
		mafCommand.getFormattedResult();
		double maf = mafCommand.getMAF();

		SpeedObdCommand speedCommand = new SpeedObdCommand();
		speedCommand.run(in, out);

		// call in order to calculate speedCommand.getMetricSpeed()
		speedCommand.getFormattedResult();
		double speed = speedCommand.getMetricSpeed();

		/*
		 * TODO calculate fuelEcon for metric units
		 */
		fuelEcon = calculateFuelEconomy(maf, speed);
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getFormattedResult() {
		if (fuelEcon < 0) {
			return "NODATA";
		}
		if (useImperialUnits) {
			double kml = fuelEcon * 0.354013;
			return String.format("%.1f %s", kml, "l/100km");
		}
		return String.format("%.1f %s", fuelEcon, "mpg");
	}

	/**
	 * TODO implement
	 * 
	 * This method will calculate
	 * 
	 * @param value
	 * @return
	 */
	private float calculateFuelEconomy(double maf, double speed) {
		Double tempValue = 0d;

		tempValue = (14.7 * 6.17 * 454.0 * speed * 0.621371) / (3600.0 * maf);
		if (useImperialUnits) {
		} else {

		}

		return Float.valueOf(tempValue.toString());
	}

	@Override
	public String getName() {
		return "Fuel Consumption";
	}

}