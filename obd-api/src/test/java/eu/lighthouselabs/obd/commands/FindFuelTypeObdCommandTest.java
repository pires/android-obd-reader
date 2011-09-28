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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.fuel.FindFuelTypeObdCommand;

/**
 * Tests for FindFuelTypeObdCommand class.
 */
public class FindFuelTypeObdCommandTest {

	private FindFuelTypeObdCommand command;
	private InputStream mockIn;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {
		command = new FindFuelTypeObdCommand();
	}

	/**
	 * Test for valid InputStream read, maximum value of 100%
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFindGasoline() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x51);
		expectLastCall().andReturn(0x01);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "Gasoline");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, 58.4%
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDiesel() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x51);
		expectLastCall().andReturn(0x04);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "Diesel");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, minimum value 0%
	 * 
	 * @throws IOException
	 */
	@Test
	public void testHybridEthanol() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x41);
		expectLastCall().andReturn(0x51);
		expectLastCall().andReturn(0x12);
		expectLastCall().andReturn(0x0D);
		expectLastCall().andReturn(0x3E); // '>'

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "Hybrid Ethanol");

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