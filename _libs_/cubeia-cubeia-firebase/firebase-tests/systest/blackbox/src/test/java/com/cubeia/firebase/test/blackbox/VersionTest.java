package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.assertEquals;

import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.io.protocol.VersionPacket;

public class VersionTest extends LoginTest {

	// @Test
	public void testVersionPacket() throws Exception {
		client.sendFirebasePacket(new VersionPacket(0, 0, new ProtocolObjectFactory().version()));
		VersionPacket versionPacket = client.expectFirebasePacket(VersionPacket.class);
		assertEquals(new ProtocolObjectFactory().version(), versionPacket.protocol);
	}
}