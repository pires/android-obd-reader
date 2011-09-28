/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;

/**
 * Tests for TemperatureObdCommand sub-classes.
 */
@PrepareForTest(InputStream.class)
public class AirIntakeTempObdCommandTest {

	private AirIntakeTemperatureObdCommand command = null;
	private InputStream mockIn = null;
	
	/**
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {
		command = new AirIntakeTemperatureObdCommand();
	}
	
	/**
	 * Test for valid InputStream read, 24ºC
	 * 
	 * @throws IOException
	 */
	@Test
	public void testValidTemperatureCelsius() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0F);
		expectLastCall().andReturn(0x40);
		expectLastCall().andReturn(0x13);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "24C");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read, 75.2F
	 * 
	 * @throws IOException
	 */
	@Test
	public void testValidTemperatureFahrenheit() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0F);
		expectLastCall().andReturn(0x45);
		expectLastCall().andReturn(0x13);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		command.useImperialUnits = true;
		assertEquals(command.getFormattedResult(), "84.2F");
		
		verifyAll();
	}
	
	/**
	 * Clear resources.
	 */
	@AfterClass
	public void tearDown() {
		command = null;
		mockIn = null;
	}
	
}