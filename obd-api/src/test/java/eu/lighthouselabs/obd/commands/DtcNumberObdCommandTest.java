/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.control.DtcNumberObdCommand;

/**
 * Tests for DtcNumberObdCommand class. 
 */
@PrepareForTest(InputStream.class)
public class DtcNumberObdCommandTest {

	private DtcNumberObdCommand command;
	private InputStream mockIn;

	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		command = new DtcNumberObdCommand();
	}

	/**
	 * Test for valid InputStream read, MIL on.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMILOn() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x01);
		expectLastCall().andReturn(0x9F);
		expectLastCall().andReturn(0x13);
		expectLastCall().andReturn(0x3E); // '>'

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		
		assertTrue(command.getMilOn());
		assertEquals(command.getTotalAvailableCodes(), 31);

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, MIL off.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMILOff() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x01);
		expectLastCall().andReturn(0x0F);
		expectLastCall().andReturn(0x13);
		expectLastCall().andReturn(0x3E); // '>'

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		
		assertFalse(command.getMilOn());
		assertEquals(command.getTotalAvailableCodes(), 15);

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