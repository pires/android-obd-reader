/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.enums;

/**
 * MODE 1 PID 0x51 will return one of the following values to identify the fuel
 * type of the vehicle.
 */
public enum FuelType {
	GASOLINE(0x01),
	METHANOL(0x02),
	ETHANOL(0x03),
	DIESEL(0x04),
	LPG(0x05),
	CNG(0x06),
	PROPANE(0x07),
	ELECTRIC(0x08),
	BIFUEL_GASOLINE(0x09),
	BIFUEL_METHANOL(0x0A),
	BIFUEL_ETHANOL(0x0B),
	BIFUEL_LPG(0x0C),
	BIFUEL_CNG(0x0D),
	BIFUEL_PROPANE(0x0E),
	BIFUEL_ELECTRIC(0x0F),
	BIFUEL_GASOLINE_ELECTRIC(0x10),
	HYBRID_GASOLINE(0x11),
	HYBRID_ETHANOL(0x12),
	HYBRID_DIESEL(0x13),
	HYBRID_ELECTRIC(0x14),
	HYBRID_MIXED(15),
	HYBRID_REGENERATIVE(16);

	private final int value;

	private FuelType(int value) {
		this.value = value;
	}

	public final int getValue() {
		return value;
	}
	
//	/**
//	 * 
//	 * @param type
//	 * @return
//	 */
//	public static String getName(FuelType type) {
//		String name = null;
//		
//		switch (type.getValue()) {
//		case 1 : name = "Gasoline"; break;
//		case 2 : name = "Methanol"; break;
//		case 3 : name = "Ethanol"; break;
//		case 4 : name = "Diesel"; break;
//		case 5 : name = "GPL/LGP"; break;
//		case 6 : name = "Natural Gas (CNG)"; break;
//		case 7 : name = "Propane"; break;
//		case 8 : name = "Electric"; break;
//		case 9 : name = "Biodiesel + Gasoline"; break;
//		case 10 : name = "Biodiesel + Methanol"; break;
//		case 11 : name = "Biodiesel + Ethanol"; break;
//		case 12 : name = "Biodiesel + Diesel"; break;
//		case 13 : name = "Biodiesel + GPL/LPG"; break;
//		case 14 : name = "Biodiesel + Natural Gas"; break;
//		case 15 : name = "Biodiesel + Propane"; break;
//		case 16 : name = "Biodiesel + Electric"; break;
//		case 17 : name = "Biodiesel + Gasoline/Electric"; break;
//		case 18 : name = "Hybrid Gasoline"; break;
//		case 19 : name = "Hybrid Ethanol"; break;
//		case 20 : name = "Hybrid Diesel"; break;
//		case 21 : name = "Hybrid Electric"; break;
//		case 22 : name = "Hybrid Mixed"; break;
//		case 23 : name = "Hybrid Regenerative"; break;
//		default : name = "NODATA";
//		}
//		
//		return name;
//	}
}