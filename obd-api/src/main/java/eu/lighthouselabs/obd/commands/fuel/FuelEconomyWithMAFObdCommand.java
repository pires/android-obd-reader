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
import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.enums.FuelTrim;

/**
 * TODO put description
 */
public class FuelEconomyWithMAFObdCommand extends ObdCommand {

	double mpg;

	/**
	 * @param command
	 */
	public FuelEconomyWithMAFObdCommand() {
		super("");
	}

	/**
	 * As it's a fake command, neither do we need to send request or read
	 * response.
	 */
	@Override
	public void run(InputStream in, OutputStream out) throws IOException,
			InterruptedException {
		SpeedObdCommand speedCmd = new SpeedObdCommand();
		speedCmd.run(in, out);
		speedCmd.getFormattedResult();

		MassAirFlowObdCommand mafCmd = new MassAirFlowObdCommand();
		mafCmd.run(in, out);
		mafCmd.getFormattedResult();

		FuelTrimObdCommand ltftCmd = new FuelTrimObdCommand(
				FuelTrim.LONG_TERM_BANK_1);
		ltftCmd.run(in, out);
		ltftCmd.getFormattedResult();

		int speed = speedCmd.getMetricSpeed();
		double maf = mafCmd.getMAF();
		float ltft = ltftCmd.getValue();

		if (speed == 0)
			speed = 1;

		if (maf == 0)
			maf = 1;

		// TODO 14.7 shall be multiplied by ltft
		mpg = (14.7 * (1 + ltft / 100) * 6.17 * 454 * speed * 0.621371)
				/ (3600 * maf / 100);
	}

	public String getFormattedResult() {
		String res = "NODATA";

		res = String.format("%.2f%s", getLitersPer100Km(), "l/100km");

		if (useImperialUnits)
			res = String.format("%.1f%s", mpg, "mpg");

		return res;
	}

	/**
	 * @return the fuel consumption in l/100km
	 */
	public double getLitersPer100Km() {
		return 235.2f / mpg;
	}

	@Override
	public String getName() {
		return AvailableCommandNames.FUEL_ECONOMY_WITH_MAF.getValue();
	}

}