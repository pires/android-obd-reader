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

import eu.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;

/**
 * Tests for EngineRPMObdCommand class.
 */
@PrepareForTest(InputStream.class)
public class EngineRPMObdCommandTest {

	private EngineRPMObdCommand command = null;
	private InputStream mockIn = null;
	
	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		command = new EngineRPMObdCommand();
	}
	
	/**
	 * Test for valid InputStream read, max RPM
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMaximumRPMValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0C);
		expectLastCall().andReturn(0xFF);
		expectLastCall().andReturn(0xFF);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "16383RPM");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read
	 * 
	 * @throws IOException
	 */
	@Test
	public void testHighRPM() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0C);
		expectLastCall().andReturn(0x28);
		expectLastCall().andReturn(0x3C);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "2575RPM");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLowRPM() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x0C);
		expectLastCall().andReturn(0x0A);
		expectLastCall().andReturn(0x00);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "640RPM");
		
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