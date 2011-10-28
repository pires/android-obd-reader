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

import eu.lighthouselabs.obd.commands.engine.EngineRuntimeObdCommand;

/**
 * Runtime since engine start in seconds, with a maximum value of 65535.
 */
@PrepareForTest(InputStream.class)
public class EngineRuntimeObdCommandTest {

	private EngineRuntimeObdCommand command;
	private InputStream mockIn;
	
	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		command = new EngineRuntimeObdCommand();
	}
	
	/**
	 * Test for valid InputStream read, 65535 seconds.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMaxRuntimeValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '3');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) 'F');
		expectLastCall().andReturn((byte) '>');
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "18:12:15");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read, 67 seconds
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSomeRuntimeValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '3');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '5');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '3');
		expectLastCall().andReturn((byte) '>');
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "04:55:31");
		
		verifyAll();
	}
	
	/**
	 * Test for valid InputStream read, 0 seconds.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMinRuntimeValue() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn((byte) '4');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '3');
		expectLastCall().andReturn((byte) '1');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) ' ');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '0');
		expectLastCall().andReturn((byte) '>');
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		assertEquals(command.getFormattedResult(), "00:00:00");
		
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