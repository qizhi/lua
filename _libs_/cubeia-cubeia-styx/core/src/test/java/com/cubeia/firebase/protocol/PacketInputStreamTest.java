package com.cubeia.firebase.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;

public class PacketInputStreamTest extends TestCase {

	public void testPeek() throws Exception {
		ByteBuffer b = ByteBuffer.allocate(4);
		PacketInputStream in = new PacketInputStream(b);
		b.put(0, (byte) 1);
		b.put(1, (byte) 2);
		b.put(2, (byte) 3);
		b.put(3, (byte) 4);
		assertEquals(1, in.loadByte()); // read first
		assertEquals(2, in.peek()); // peek second
		assertEquals(1, b.position()); // check position isn't advanced
		assertEquals(2, in.loadByte()); // read second
		assertEquals(2, b.position()); // check position
		assertEquals(3, in.loadByte());
		assertEquals(4, in.loadByte());
		assertEquals(-1, in.peek()); // end of stream	
	}
	
	@Ignore
	@Test
	public void uint32Test() throws Exception {
		for ( long value = 0; value < PacketInputStream.MAX_UNSIGNED_INTEGER; value ++ ) {
		   ByteArrayOutputStream os = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(os);
	        PacketOutputStream pos = new PacketOutputStream(dos);
	        
	        pos.saveUnsignedInt(value);

	        byte[] byteBuffer = os.toByteArray();
	        InputStream is = new ByteArrayInputStream(byteBuffer);
	        DataInputStream dis = new DataInputStream(is);
	        byte[] ba = new byte[200];
	        dis.read(ba);
	        PacketInputStream pis = new PacketInputStream(ByteBuffer.wrap(ba));
	        
	        long result = pis.loadUnsignedInt();
	        assertThat(result, is(value));
	        
		}
	}
	
	@Ignore
	@Test
	public void uint16Test() throws Exception {
		for ( int value = 0; value < PacketInputStream.MAX_UNSIGNED_SHORT; value ++ ) {
		   ByteArrayOutputStream os = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(os);
	        PacketOutputStream pos = new PacketOutputStream(dos);
	        
	        pos.saveUnsignedShort(value);

	        byte[] byteBuffer = os.toByteArray();
	        InputStream is = new ByteArrayInputStream(byteBuffer);
	        DataInputStream dis = new DataInputStream(is);
	        byte[] ba = new byte[200];
	        dis.read(ba);
	        PacketInputStream pis = new PacketInputStream(ByteBuffer.wrap(ba));
	        
	        int result = pis.loadUnsignedShort();
	        assertThat(result, is(value));
	        
		}
	}

}

