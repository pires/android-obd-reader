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
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'C');
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
		assertEquals(command.getRPM(), 16383);

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
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'C');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '2');
		expectLastCall().andReturn((byte) '8');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '3');
		expectLastCall().andReturn((byte) 'C');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		assertEquals(command.getRPM(), 2575);

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
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'C');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) 'A');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '>');

		replayAll();

		// call the method to test
		command.readResult(mockIn);
		command.getFormattedResult();
		assertEquals(command.getRPM(), 640);

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