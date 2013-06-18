package com.cubeia.firebase.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class MyTest extends TestCase {
	
	/**
	 * This is test I wrote to make sure that the new ByteBuffer reading of
	 * unsigned shorts matches the existing DataInputStream's.
	 * 
	 * @throws Exception
	 */
	public void testUnsigned() throws Exception {
		byte[] data = new byte[]{ (byte)0xF0, (byte)0xFF };
		
		// Read through Datainputstream
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		short sRead = stream.readShort();
		stream.reset();
		int usRead = stream.readUnsignedShort();
		
		System.out.println("Stream signed:\t\t"+sRead);
		System.out.println("Stream unsigned:\t"+usRead);
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.mark();
		
		short bRead = buffer.getShort();
		buffer.reset();
		int ubRead = buffer.getShort() & 0xFFFF;
		
		assertEquals(bRead, sRead);
		assertEquals(ubRead, usRead);
		
	}
	
}
