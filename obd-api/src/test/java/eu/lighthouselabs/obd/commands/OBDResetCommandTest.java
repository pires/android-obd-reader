/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.commands;

import static org.powermock.api.easymock.PowerMock.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for OBDCommand sub-classes.
 */
@PrepareForTest(InputStream.class)
public class OBDResetCommandTest {
	
	private OBDResetCommand command;
	private InputStream mockIn;
	private OutputStream mockOut;
	
	/**
	 * We must mock everything we need in each test, instead of 
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public void setup() throws Exception {
		command = new OBDResetCommand();
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
		expectLastCall().andReturn(1);
		expectLastCall().andReturn(2);
		expectLastCall().andReturn(-1);
		
		replayAll();
		
		// call the method  to test
		command.readResult(mockIn);
		
		assertNotEquals(command.buff.size(), 0);
		
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
		expectLastCall().andReturn(-1);
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
		mockOut = null;
	}

}