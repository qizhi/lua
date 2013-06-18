package com.cubeia.firebase.client.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.firebase.client.test.protocol.Enums.EnumType;
import com.cubeia.firebase.client.test.protocol.ListOfInts;
import com.cubeia.firebase.client.test.protocol.ListOfUint16;
import com.cubeia.firebase.client.test.protocol.ListOfUint32;
import com.cubeia.firebase.client.test.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.client.test.protocol.TypeTestPacket;
import com.cubeia.firebase.client.test.protocol.ValueBool;
import com.cubeia.firebase.client.test.protocol.ValueEnum;
import com.cubeia.firebase.client.test.protocol.ValueInt16;
import com.cubeia.firebase.client.test.protocol.ValueInt32;
import com.cubeia.firebase.client.test.protocol.ValueInt64;
import com.cubeia.firebase.client.test.protocol.ValueInt8;
import com.cubeia.firebase.client.test.protocol.ValueString;
import com.cubeia.firebase.client.test.protocol.ValueUint16;
import com.cubeia.firebase.client.test.protocol.ValueUint32;
import com.cubeia.firebase.client.test.protocol.ValueUint8;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JavaToJsInteropTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private StyxSerializer styx = new StyxSerializer(
        new ProtocolObjectFactory());

    private ScriptEngine engine;

    @Before
    public void setUp() throws FileNotFoundException, ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");

        File apiScript = new File("target/js/firebase-js-api.js");
        assertThat(apiScript.exists(), is(true));
        engine.eval(new java.io.FileReader(apiScript));

        File protocolScript = new File("target/js/test-protocol.js");
        assertThat(protocolScript.exists(), is(true));
        engine.eval(new java.io.FileReader(protocolScript));
    }

    
    @Test
    public void testInt8() {
        ValueInt8 vp = new ValueInt8((byte) -100);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(-100));
    }
    
    @Test
    public void testUInt8() {
        ValueUint8 vp = new ValueUint8(200);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(200));
    }
    
    @Test
    public void testInt16() {
        ValueInt16 vp = new ValueInt16((short) 31000);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(31000));
    }
    
    @Test
    public void testUInt16() {
        ValueUint16 vp = new ValueUint16(64000);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(64000));
    }
    
    @Test
    public void testInt32() {
        ValueInt32 vp = new ValueInt32(-2000000000);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(-2000000000));
    }
    
    @Test
    public void testUInt32() {
        ValueUint32 vp = new ValueUint32(4294967295L);
        Map result = evalTestScript(vp);
        assertThat((Long) result.get("value"), is(4294967295L));
    }
    
    @Test
    public void testInt64() {
        ValueInt64 vp = new ValueInt64(-4503599627370496L);
        Map result = evalTestScript(vp);
        assertThat((Long) result.get("value"), is(-4503599627370496L));
    }
    
    @Test
    public void testBool() {
        ValueBool vp = new ValueBool(true);
        Map result = evalTestScript(vp);
        assertThat((Boolean) result.get("value"), is(true));
    }
    
    @Test
    public void testString() {
        ValueString vp = new ValueString("test string");
        Map result = evalTestScript(vp);
        assertThat((String) result.get("value"), is("test string"));
    }
    
    @Test
    public void testEnum() {
        ValueEnum vp = new ValueEnum(EnumType.INT);
        Map result = evalTestScript(vp);
        assertThat((Integer) result.get("value"), is(EnumType.INT.ordinal()));
    }
    
    @Test
    public void testListOfUInts() throws ScriptException, JsonParseException,
        JsonMappingException, IOException {
        ListOfUint32 listOfUInts = new ListOfUint32();
        listOfUInts.l = new long[] { 1, 2, 3, 4, 64000 };

        Map result = evalTestScript(listOfUInts);
        ArrayList<Integer >intsListResult = (ArrayList<Integer>) result.get("l");
        assertThat(intsListResult.size(), is (5));
        assertThat(intsListResult.get(0), is((int) listOfUInts.l[0]));
        assertThat(intsListResult.get(1), is((int) listOfUInts.l[1]));
        assertThat(intsListResult.get(2), is((int) listOfUInts.l[2]));
        assertThat(intsListResult.get(3), is((int) listOfUInts.l[3]));
        assertThat(intsListResult.get(4), is((int) listOfUInts.l[4]));
    }

    @Test
    public void testListOfInts() throws ScriptException, JsonParseException,
        JsonMappingException, IOException {
        ListOfInts listOfInts = new ListOfInts();
        listOfInts.l = new int[] { -11, 2, 3, 4, 64000 };

        Map result = evalTestScript(listOfInts);
        ArrayList<Integer >intsListResult = (ArrayList<Integer>) result.get("l");
        assertThat(intsListResult.size(), is (5));
        assertThat(intsListResult.get(0), is((int) listOfInts.l[0]));
        assertThat(intsListResult.get(1), is((int) listOfInts.l[1]));
        assertThat(intsListResult.get(2), is((int) listOfInts.l[2]));
        assertThat(intsListResult.get(3), is((int) listOfInts.l[3]));
        assertThat(intsListResult.get(4), is((int) listOfInts.l[4]));
    }

    @Test
    public void testListOfUShorts() throws ScriptException, JsonParseException,
        JsonMappingException, IOException {
        ListOfUint16 listOfShorts = new ListOfUint16();
        listOfShorts.l = new int[] { 11, 2, 256, 4, 65535 };

        Map result = evalTestScript(listOfShorts);
        ArrayList<Integer >intsListResult = (ArrayList<Integer>) result.get("l");
        assertThat(intsListResult.size(), is (5));
        assertThat(intsListResult.get(0), is((int) listOfShorts.l[0]));
        assertThat(intsListResult.get(1), is((int) listOfShorts.l[1]));
        assertThat(intsListResult.get(2), is((int) listOfShorts.l[2]));
        assertThat(intsListResult.get(3), is((int) listOfShorts.l[3]));
        assertThat(intsListResult.get(4), is((int) listOfShorts.l[4]));
    }
    
    @Test
    public void testTypeTestPacket() throws ScriptException,
        JsonParseException, JsonMappingException, IOException {
        TypeTestPacket p = new TypeTestPacket(
            240,            // ui8
            1234567890L,    // ui64
            (byte) 100,     // i8
            (short) 64000,  // i16
            12345678,       // i32
            1234567890L,    // i64
            true,           // boolean
            "string");      // string

        Map result = evalTestScript(p);
        assertThat(result.size(), is(8));
        assertThat((Boolean) result.get("b"), is(p.b));
        assertThat((String) result.get("s"), is(p.s));
        assertThat((Integer) result.get("i8"), is((int) p.i8));
        assertThat((Integer) result.get("i16"), is((int) p.i16));
        assertThat((Integer) result.get("ui8"), is(p.ui8));
        assertThat((Integer) result.get("i32"), is(p.i32));
        assertThat((Integer) result.get("i64"), is((int)p.i64));
        assertThat((Integer) result.get("ui64"), is((int)p.ui64));
    }

    private Map jsonToMap(String jsonResult) throws IOException,
        JsonParseException, JsonMappingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = objectMapper.readValue(jsonResult, Map.class);
        return map;
    }

    private Map evalTestScript(ProtocolObject protocolObject) {
        File testScript = new File("src/test/js/java-to-js-interop-test.js");
        assertThat(testScript.exists(), is(true));
        ByteBuffer pack = styx.pack(protocolObject);
        engine.put("packet", pack.array());
        String json;
        try {
            json = (String) engine.eval(new java.io.FileReader(testScript));
            log.debug("json from javascript: {}", json);
            return jsonToMap(json);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
