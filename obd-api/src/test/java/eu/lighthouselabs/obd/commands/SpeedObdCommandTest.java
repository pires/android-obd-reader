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

/**
 * Tests for ObdSpeedCommand class.
 */
@PrepareForTest(InputStream.class)
public class SpeedObdCommandTest {

	private SpeedObdCommand command;
	private InputStream mockIn;
	
	/**
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {
		command = new SpeedObdCommand();
	}
	
	/**
	 * Test for valid InputStream read, 64km/h
	 * 
	 * @throws IOException
	 */
	@Test
	public void testValidSpeedMetric() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x40);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		command.useImperialUnits = false;
		assertEquals(command.getFormattedResult(), "64km/h");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read, 42.87mph
	 * 
	 * @throws IOException
	 */
	@Test
	public void testValidSpeedImperial() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41); // 0x41
		expectLastCall().andReturn(0x0D); // 0x0D
		expectLastCall().andReturn(0x45); // 0x45
		expectLastCall().andReturn(0x0D); // 0xD
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		command.useImperialUnits = true;
		assertEquals(command.getFormattedResult(), "42.87mph");
		
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