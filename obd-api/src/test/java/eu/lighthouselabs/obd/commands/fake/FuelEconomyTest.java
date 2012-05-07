/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands.fake;

import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.fuel.FuelEconomyWithMAFObdCommand;
import eu.lighthouselabs.obd.enums.FuelType;

/**
 * TODO put description
 * 
 */
// @PrepareForTest(InputStream.class)
public class FuelEconomyTest {

	private FuelEconomyWithMAFObdCommand cmdWithMAF = null;

	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		cmdWithMAF = new FuelEconomyWithMAFObdCommand(FuelType.DIESEL, 38,
				17.66d, -85.94f, false);
	}

	@Test
	public void testSomeLitersPer100Km() {
		cmdWithMAF = new FuelEconomyWithMAFObdCommand(FuelType.DIESEL, 38,
				17.66d, -85.94f, false);
		assertEquals(cmdWithMAF.getLitersPer100Km(), 15.379336121584492d);
	}
	
	@Test
	public void testSomeMPG() {
		assertEquals(cmdWithMAF.getMPG(), 15.29324791009041d);
	}

	/**
	 * Clear resources.
	 */
	@AfterClass
	public void tearDown() {
		cmdWithMAF = null;
	}

}