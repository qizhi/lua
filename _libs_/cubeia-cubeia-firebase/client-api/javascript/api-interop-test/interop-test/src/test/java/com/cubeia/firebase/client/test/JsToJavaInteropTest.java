package com.cubeia.firebase.client.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.firebase.client.test.protocol.ActionDeal;
import com.cubeia.firebase.client.test.protocol.BetRequest;
import com.cubeia.firebase.client.test.protocol.Enums.EnumType;
import com.cubeia.firebase.client.test.protocol.ListOfBool;
import com.cubeia.firebase.client.test.protocol.ListOfEnums;
import com.cubeia.firebase.client.test.protocol.ListOfInt16;
import com.cubeia.firebase.client.test.protocol.ListOfInt8;
import com.cubeia.firebase.client.test.protocol.ListOfInts;
import com.cubeia.firebase.client.test.protocol.ListOfInts64;
import com.cubeia.firebase.client.test.protocol.ListOfString;
import com.cubeia.firebase.client.test.protocol.ListOfUint16;
import com.cubeia.firebase.client.test.protocol.ListOfUint32;
import com.cubeia.firebase.client.test.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.client.test.protocol.ValueInt16;
import com.cubeia.firebase.client.test.protocol.ValueInt32;
import com.cubeia.firebase.client.test.protocol.ValueInt64;
import com.cubeia.firebase.client.test.protocol.ValueInt8;
import com.cubeia.firebase.client.test.protocol.ValueUint16;
import com.cubeia.firebase.client.test.protocol.ValueUint32;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxJsonSerializer;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.GameTransportPacket;

public class JsToJavaInteropTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

    private ScriptEngine engine;

    private StyxJsonSerializer firebaseStyx = new StyxJsonSerializer(new com.cubeia.firebase.io.protocol.ProtocolObjectFactory());

    @Before
    public void setUp() throws FileNotFoundException, ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
        
        File fbProtocolScript = new File("target/js/firebase-js-protocol.js");
        assertThat(fbProtocolScript.exists(), is(true));
        engine.eval(new java.io.FileReader(fbProtocolScript));

        File apiScript = new File("target/js/firebase-js-api.js");
        assertThat(apiScript.exists(), is(true));
        engine.eval(new java.io.FileReader(apiScript));
        
        File protocolScript = new File("target/js/test-protocol.js");
        assertThat(protocolScript.exists(), is(true));
        engine.eval(new java.io.FileReader(protocolScript));
        
        File testScript = new File("src/test/js/js-to-java-interop-test.js");
        assertThat(testScript.exists(), is(true));
        engine.eval(new java.io.FileReader(testScript));
    }

    @Test
    public void testListOfInt8() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfInt8", 1, 2, -127, 127);
        ListOfInt8 packet = (ListOfInt8) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is((byte) 1));
        assertThat(packet.l[1], is((byte) 2));
        assertThat(packet.l[2], is((byte) -127));
        assertThat(packet.l[3], is((byte) 127));
    }
    
    @Test
    public void testListOfInt16() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfInt16", 1, 2, -32767, 32767);
        ListOfInt16 packet = (ListOfInt16) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is((short) 1));
        assertThat(packet.l[1], is((short) 2));
        assertThat(packet.l[2], is((short) -32767));
        assertThat(packet.l[3], is((short) 32767));
    }
    
    @Test
    public void testListOfUInt16() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfUInt16", 1, 2, 0, 65535);
        ListOfUint16 packet = (ListOfUint16) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is(1));
        assertThat(packet.l[1], is(2));
        assertThat(packet.l[2], is(0));
        assertThat(packet.l[3], is(65535));
    }
    
    @Test
    public void testListOfInt32() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfInt32", 1, 2, -2147483647, 2147483647);
        ListOfInts packet = (ListOfInts) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is(1));
        assertThat(packet.l[1], is(2));
        assertThat(packet.l[2], is(-2147483647));
        assertThat(packet.l[3], is(2147483647));
    }
    
    @Test
    public void testListOfUint32() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfUInt32", 1, 2, 0, 4294967295L);
        ListOfUint32 packet = (ListOfUint32) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is(1L));
        assertThat(packet.l[1], is(2L));
        assertThat(packet.l[2], is(0L));
        assertThat(packet.l[3], is(4294967295L));
    }
    
    @Test
    public void testListOfInt64() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfInt64", 1, 2, -4503599627370496L, 4503599627370496L);
        ListOfInts64 packet = (ListOfInts64) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is(1L));
        assertThat(packet.l[1], is(2L));
        assertThat(packet.l[2], is(-4503599627370496L));
        assertThat(packet.l[3], is(4503599627370496L));
    }
    
    @Test
    public void testListOfStrings() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfString", "abba", "bork", "curp", "durp");
        ListOfString packet = (ListOfString) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is("abba"));
        assertThat(packet.l[1], is("bork"));
        assertThat(packet.l[2], is("curp"));
        assertThat(packet.l[3], is("durp"));
    }
    
    @Test
    public void testListOfBooleans() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfBool", true, false, true, true);
        ListOfBool packet = (ListOfBool) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.length, is(4));
        assertThat(packet.l[0], is(true));
        assertThat(packet.l[1], is(false));
        assertThat(packet.l[2], is(true));
        assertThat(packet.l[3], is(true));
    }
    
    @Test
    public void testListOfEnums() {
        GameTransportPacket gameTransportPacket = evalTestScript("createListOfEnum", EnumType.INT, EnumType.DATE, EnumType.STRING, EnumType.DATE);
        ListOfEnums packet = (ListOfEnums) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.l.size(), is(4));
        assertThat(packet.l.get(0), is(EnumType.INT));
        assertThat(packet.l.get(1), is(EnumType.DATE));
        assertThat(packet.l.get(2), is(EnumType.STRING));
        assertThat(packet.l.get(3), is(EnumType.DATE));
    }
    
    @Test
    public void testInt8() {
        byte value = 127;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt8", value);
        ValueInt8 packet = (ValueInt8) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
        
        value = -127;
        gameTransportPacket = evalTestScript("createValueInt8", value);
        packet = (ValueInt8) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testUInt16() {
        int value = 64000;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueUInt16", value);
        ValueUint16 packet = (ValueUint16) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testInt16() {
        short value = 32000;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt16", value);
        ValueInt16 packet = (ValueInt16) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
        
        value = -32000;
        gameTransportPacket = evalTestScript("createValueInt16", value);
        packet = (ValueInt16) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testUInt32() {
        long value = 2547483647L;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueUInt32", value);
        ValueUint32 packet = (ValueUint32) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testInt32() {
        int value = 2147483647;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt32", value);
        ValueInt32 packet = (ValueInt32) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
        
        value = -2147483647;
        gameTransportPacket = evalTestScript("createValueInt32", value);
        packet = (ValueInt32) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testInt64Small() {
        long value = 12345L;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt64", value);
        ValueInt64 packet = (ValueInt64) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testInt64Big() {
        long value = 6007199254740992L;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt64", value);
        ValueInt64 packet = (ValueInt64) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testInt64Negative() {
        long value = -6007199254740992L;
        GameTransportPacket gameTransportPacket = evalTestScript("createValueInt64", value);
        ValueInt64 packet = (ValueInt64) unpackGameTransportPacket(gameTransportPacket);
        assertThat(packet.value, is(value));
    }
    
    @Test
    public void testBetRequest() {
        int handId = 10;
        long amount = 20;
        long tieAmount = 30;
        
        GameTransportPacket gameTransportPacket = evalTestScript("createBetRequest", handId, amount, tieAmount);
        BetRequest bet = (BetRequest) unpackGameTransportPacket(gameTransportPacket);
        assertThat(bet.handId, is(handId));
        assertThat(bet.amount, is(amount));
        assertThat(bet.tieAmount, is(tieAmount));
    }
    
    @Test
    public void testDealAction() {
        int handId = 10;
        int handId2 = 11;
        long amount = 20;
        long amount2 = 21;
        long tieAmount = 30;
        long tieAmount2 = 31;
        
        GameTransportPacket gameTransportPacket = evalTestScript("createDealAction", handId, amount, tieAmount, handId2, amount2, tieAmount2);
        ActionDeal dealAction = (ActionDeal) unpackGameTransportPacket(gameTransportPacket);
        assertThat(dealAction.bets.size(), is(2));
        BetRequest bet1 = dealAction.bets.get(0);
        assertThat(bet1.handId, is(handId));
        assertThat(bet1.amount, is(amount));
        assertThat(bet1.tieAmount, is(tieAmount));
        
        BetRequest bet2 = dealAction.bets.get(1);
        assertThat(bet2.handId, is(handId2));
        assertThat(bet2.amount, is(amount2));
        assertThat(bet2.tieAmount, is(tieAmount2));
    }
    
    private ProtocolObject unpackGameTransportPacket(GameTransportPacket gameTransportPacket) {
        return styx.unpack(ByteBuffer.wrap(gameTransportPacket.gamedata));
    }

    private GameTransportPacket evalTestScript(String method, Object... args) {
        Invocable inv = (Invocable) engine;
        try {
            String result = (String) inv.invokeFunction(method, args);
            GameTransportPacket gtp = (GameTransportPacket) firebaseStyx.fromJson(result);
            log.debug("json result: {}, game transport packet: {}", result, gtp);
            return gtp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
