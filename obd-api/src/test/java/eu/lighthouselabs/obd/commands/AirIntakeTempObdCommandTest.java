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
import org.testng.annotations.BeforeMethod;
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
	@BeforeMethod
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
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '>');
		
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
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '5');
		expectLastCall().andReturn((byte) '>');
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		command.useImperialUnits = true;
		assertEquals(command.getFormattedResult(), "84.2F");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read, 0ºC
	 * 
	 * @throws IOException
	 */
	@Test
	public void testValidTemperatureZeroCelsius() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '2');
		expectLastCall().andReturn((byte) '8');
		expectLastCall().andReturn((byte) '>');
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "0C");
		
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