package com.cubeia.styx.test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.styx.test.protocol.EnumTestPacket;

public class ProtocolTest {

	@Test
	public void nullEnumOnSave() throws Exception {
		EnumTestPacket e = new EnumTestPacket();
		e.type = null;
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		PacketOutputStream out = new PacketOutputStream(new DataOutputStream(ba));
		e.save(out);
	}
}
