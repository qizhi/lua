package com.cubeia.firebase.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.StyxSerializer;

public class StyxTest extends TestCase {

    public void testBasicSerialization() throws Exception {
        Byte in_int8 = -128;
        Integer in_uint8 = 150;
        Short in_int16 = -32768;
        Integer in_uint16 = 65535;
        Integer in_int32 = -1000000;
        Integer in_uint32 = 1000000;
        Long in_uint_big_32 = 3294967296L;
        Long in_int64 = 1234567891011121314L;
        String in_string = "LOL, IMHO :)";
        boolean in_bool = false, in_bool2 = true;
        
        StringBuffer longStringBuffer = new StringBuffer();
        for (int i = 0; i < 50000; i++) {
            longStringBuffer.append('x');
        }
        String in_string_long = longStringBuffer.toString();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PacketOutputStream out = new PacketOutputStream(new DataOutputStream(bytes));
        out.saveByte(in_int8);
        out.saveUnsignedByte(in_uint8);
        out.saveShort(in_int16);
        out.saveUnsignedShort(in_uint16);
        out.saveInt(in_int32);
        out.saveInt(in_uint32);
        out.saveUnsignedInt(in_uint_big_32);
        out.saveLong(in_int64);
        out.saveString(in_string);
        out.saveString(in_string_long);
        out.saveBoolean(in_bool);
        out.saveBoolean(in_bool2);

        ByteBuffer buffer = ByteBuffer.wrap(bytes.toByteArray());
        PacketInputStream in = new PacketInputStream(buffer);

        assertEquals(in.loadByte(), in_int8.intValue());
        assertEquals(in.loadUnsignedByte(), in_uint8.intValue());
        assertEquals(in.loadShort(), in_int16.intValue());
        assertEquals(in.loadUnsignedShort(), in_uint16.intValue());
        assertEquals(in.loadInt(), in_int32.intValue());
        assertEquals(in.loadInt(), in_uint32.intValue());
        assertEquals(in.loadUnsignedInt(), in_uint_big_32.longValue());
        assertEquals(in.loadLong(), in_int64.longValue());
        assertEquals(in.loadString(), in_string);
        assertEquals(in.loadString(), in_string_long);
        assertEquals(in.loadBoolean(), in_bool);
        assertEquals(in.loadBoolean(), in_bool2);
    }
    
    public void testBasicArraysSerialization() throws Exception {
        byte[] in_int8_array = new byte[] {1, 2, -100};
        int[] in_int32_array = new int[] {1000, 2000000, -10000000};
        long[] in_int64_array = new long[] {100000L, 200000000000L, -1000000000000L};
        String[] in_string_array = new String[] {"one", "two", "three"};
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PacketOutputStream out = new PacketOutputStream(new DataOutputStream(bytes));
        out.saveArray(in_int8_array);
        out.saveArray(in_int32_array);
        out.saveArray(in_int64_array);
        out.saveArray(in_string_array);
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes.toByteArray());
        PacketInputStream in = new PacketInputStream(buffer);

        byte[] out_int8_array = new byte[3];
        in.loadByteArray(out_int8_array);
        assertThat(out_int8_array, is(in_int8_array));
        
        int[] out_int32_array = new int[3];
        in.loadIntArray(out_int32_array);
        assertThat(out_int32_array, is(in_int32_array));
        
        long[] out_int64_array = new long[3];
        in.loadLongArray(out_int64_array);
        assertThat(out_int64_array, is(in_int64_array));
        
        String[] out_string_array = new String[3];
        in.loadStringArray(out_string_array);
        assertThat(out_string_array, is(in_string_array));
    }
    

    public void testSaveStringSizeOverflow() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PacketOutputStream packetOut = new PacketOutputStream(new DataOutputStream(byteOut));
        
        String longString = randomString(PacketOutputStream.STRING_MAX_BYTES + 1);
        
        // write a string that is too long and check that we get an exception
        try {
            packetOut.saveString(longString);
            fail("we should have got an IOException, the string was too long");
        } catch (IOException e) {
            // this should happen
        }
        
        // test exact length
        longString = randomString(PacketOutputStream.STRING_MAX_BYTES);
        packetOut.saveString(longString);
        
        ByteBuffer buffer = ByteBuffer.wrap(byteOut.toByteArray());
        PacketInputStream in = new PacketInputStream(buffer);
        
        assertEquals(longString, in.loadString());
    }
    
    public void testStyxSerializer() throws Exception {
        StyxSerializer styx = new StyxSerializer(new TestFactory());

        TestClass obj = new TestClass();
        obj.intVal = 761220;
        obj.strVal = "OMG! åäÖåÄö";

        ByteBuffer packed = styx.pack(obj);

        TestClass unpacked = (TestClass)styx.unpack(packed);
        assertEquals(obj.intVal, unpacked.intVal);
        assertEquals(obj.strVal, unpacked.strVal);
    }

    
    public void testVersionPacket() throws Exception {
    	StyxSerializer styx = new StyxSerializer(new TestFactory());
    	
    	VersionPacket packet = new VersionPacket();
    	packet.game = 99;
    	packet.operatorid = 1;
    	packet.protocol = 2;
    	
    	ByteBuffer packed = styx.pack(packet);
    	
    	VersionPacket unpacked = (VersionPacket)styx.unpack(packed);
    	assertEquals(99, unpacked.game);
    }
    
    public void testReadWriteUnsignedInt64() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        PacketOutputStream pos = new PacketOutputStream(dos);
        long value1 = 1234L;
        long value2 = 0xffffffffL;
        long value3 = 0xff00ff00L;
        pos.saveUnsignedInt(value1);
        pos.saveUnsignedInt(value2);
        pos.saveUnsignedInt(value3);

        byte[] byteBuffer = os.toByteArray();
        InputStream is = new ByteArrayInputStream(byteBuffer);
        DataInputStream dis = new DataInputStream(is);
        byte[] ba = new byte[200];
        dis.read(ba);
        PacketInputStream pis = new PacketInputStream(ByteBuffer.wrap(ba));
        
        long result1 = pis.loadUnsignedInt();
        long result2 = pis.loadUnsignedInt();
        long result3 = pis.loadUnsignedInt();
        assertThat(result1, is(value1));
        assertThat(result2, is(value2));
        assertThat(result3, is(value3));
    }    
    
    private String randomString(int length) {
        char[] chars = new char[] {'a', 'b', 'c', 'd'};
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            stringBuffer.append(chars[(int) Math.random() * chars.length]);
        }
        return stringBuffer.toString();
    }
    
    public static void main(String[] args) {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(StyxTest.class);
        junit.textui.TestRunner.run(suite);
    }

}
