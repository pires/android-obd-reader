/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;


/**
 * This command is intended to determine the vehicle fuel type.
 */
public class FindFuelTypeCommand extends OBDCommand {

	/**
	 * Default ctor.
	 */
	public FindFuelTypeCommand() {
		super("10 51");
	}

	/**
	 * Copy ctor
	 * 
	 * @param other
	 */
	public FindFuelTypeCommand(OBDCommand other) {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.lighthouselabs.obd.command.ObdCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [hh hh] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			res = getFuelTypeName(b1 << 8);
		}

		return res;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getFuelTypeName(int value) {
		String name = null;
		
		switch (value) {
		case 1 : name = "Gasoline"; break;
		case 2 : name = "Methanol"; break;
		case 3 : name = "Ethanol"; break;
		case 4 : name = "Diesel"; break;
		case 5 : name = "GPL/LGP"; break;
		case 6 : name = "Natural Gas (CNG)"; break;
		case 7 : name = "Propane"; break;
		case 8 : name = "Electric"; break;
		case 9 : name = "Biodiesel + Gasoline"; break;
		case 10 : name = "Biodiesel + Methanol"; break;
		case 11 : name = "Biodiesel + Ethanol"; break;
		case 12 : name = "Biodiesel + Diesel"; break;
		case 13 : name = "Biodiesel + GPL/LPG"; break;
		case 14 : name = "Biodiesel + Natural Gas"; break;
		case 15 : name = "Biodiesel + Propane"; break;
		case 16 : name = "Biodiesel + Electric"; break;
		case 17 : name = "Biodiesel + Gasoline/Electric"; break;
		case 18 : name = "Hybrid Gasoline"; break;
		case 19 : name = "Hybrid Ethanol"; break;
		case 20 : name = "Hybrid Diesel"; break;
		case 21 : name = "Hybrid Electric"; break;
		case 22 : name = "Hybrid Mixed"; break;
		case 23 : name = "Hybrid Regenerative"; break;
		default : name = "NODATA";
		}
		
		return name;
	}

}