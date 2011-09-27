/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import eu.lighthouselabs.obd.commands.protocol.ObdResetCommand;

/**
 * Tests for ObdCommand sub-classes.
 */
@PrepareForTest(InputStream.class)
public class ObdResetCommandTest {
	
	private ObdResetCommand command;
	private InputStream mockIn;
	
	/**
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {
		command = new ObdResetCommand();
	}
	
	/**
	 * Test for valid InputStream read.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadResultWithValidRead() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x4F); // O
		expectLastCall().andReturn(0x4B); // K
		expectLastCall().andReturn(0x0D); // \r
		expectLastCall().andReturn(0x3E); // '>'
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		
		assertNotEquals(command.buff.size(), 0);
		assertEquals(command.getFormattedResult(), "OK");
		
		verifyAll();
	}
	
	/**
	 * Test for empty InputStream read (meaning -1).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadResultWithEmptyRead() throws IOException {
		// mock InputStream read
		mockIn = createMock(InputStream.class);
		mockIn.read();
		expectLastCall().andReturn(0x3E); // '>'
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		
		assertEquals(command.buff.size(), 0);
		
		verifyAll();
	}
	
	/**
	 * Test for valid buffer contents.
	 */
	@Test
	public void testPrepareResultWithValidBuffer() {
		byte[] ab = "OK\rNODATA\r\r>".getBytes();
		command.buff = new ArrayList<Byte>();
		for (byte b : ab)
			command.buff.add(b);
		
		// call the method to test
		command.prepareResult();
		
		assertNotNull(command.unformattedResult);
		assertEquals(command.unformattedResult, "OK");
	}
	
	/**
	 * Test for invalid buffer contents.
	 */
	@Test
	public void testPrepareResultWithInvalidBuffer() {	
		// call the method to test
		command.prepareResult();
		
		assertNull(command.unformattedResult);
	}
	
	/**
	 * Clear resources used by test.
	 */
	@AfterClass
	public void tearDown() {
		command = null;
		mockIn = null;
	}

}