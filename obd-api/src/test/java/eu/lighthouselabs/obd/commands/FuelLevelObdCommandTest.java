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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;

/**
 * Tests for FuelLevelObdCommand class.
 */
public class FuelLevelObdCommandTest {
	private FuelLevelObdCommand command = null;
	private InputStream mockIn = null;

	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		command = new FuelLevelObdCommand();
	}

	/**
	 * Test for valid InputStream read, full tank
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFullTank() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '2');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "100.0%");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read. 78.4%
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSomeValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '2');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'C');
		expectLastCall().andReturn((byte) '8');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "78.4%");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, empty tank
	 * 
	 * @throws IOException
	 */
	@Test
	public void testEmptyTank() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '2');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "0.0%");

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