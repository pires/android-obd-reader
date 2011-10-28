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

import eu.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;

/**
 * Tests for MassAirFlowObdCommand class.
 */
@PrepareForTest(InputStream.class)
public class MassAirFlowObdCommandTest {
	private MassAirFlowObdCommand command;
	private InputStream mockIn;

	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		command = new MassAirFlowObdCommand();
	}

	/**
	 * Test for valid InputStream read, maximum value of 655.35g/s
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMaxMAFValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		assertEquals(command.getFormattedResult(), "655.35g/s");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, 381.61g/s
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSomeMAFValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '9');
		expectLastCall().andReturn((byte) '5');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		assertEquals(command.getFormattedResult(), "381.61g/s");

		verifyAll();
	}

	/**
	 * Test for valid InputStream read, minimum value 0g/s
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMinMAFValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		assertEquals(command.getFormattedResult(), "0.00g/s");

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