package org.obdreader.command;

public class AverageFuelEconomyObdCommand extends ObdCommand {

	public static final String FUEL_ECONOMY_KEY = "Fuel Economy";
	public static final String AVG_FUEL_ECONOMY_KEY = "Fuel Economy Average";
	public static final String AVG_FUEL_ECONOMY_COUNT_KEY = "Average Fuel Economy Count";
	
	public AverageFuelEconomyObdCommand() {
		super("",AVG_FUEL_ECONOMY_KEY,"kmpg","mpg");
	}
	public AverageFuelEconomyObdCommand(AverageFuelEconomyObdCommand other) {
		super(other);
	}
	public String formatResult() {
		Double ampg = 0.0;
		Integer count = 0;
		Double mpg = 0.0;
		if (data.containsKey(AVG_FUEL_ECONOMY_KEY)) {
			ampg = (Double)data.get(AVG_FUEL_ECONOMY_KEY);
			count = (Integer)data.get(AVG_FUEL_ECONOMY_COUNT_KEY);
		}
		if (data.containsKey(FUEL_ECONOMY_KEY)) {
			mpg = (Double)data.get(FUEL_ECONOMY_KEY);
		}
		if (mpg > 0) {
			ampg += mpg;
			count += 1;
			data.put(AVG_FUEL_ECONOMY_KEY, ampg);
			data.put(AVG_FUEL_ECONOMY_COUNT_KEY, count);
		}
		if (count > 0) {
			ampg = ampg / (double)count;
		}
		return String.format("%.1f mpg", ampg);
	}
}
