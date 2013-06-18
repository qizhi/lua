package com.cubeia.firebase.protocol;

import java.io.IOException;

import junit.framework.TestCase;

import com.cubeia.firebase.io.ObjectFactory;
import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.ProtocolObjectVisitor;
import com.cubeia.firebase.io.StyxSerializer;

public class OverflowTest extends TestCase {

	public void testOverflow() throws IOException {
		ProtocolObject p = new ProtocolObject() {

			public int classId() {
				return 300;
			}
			public void load(PacketInputStream ps) throws IOException {
			}
			public void save(PacketOutputStream ps) throws IOException {
			}
			public void accept(ProtocolObjectVisitor visitor) {
			}
		};
		
		StyxSerializer s = new StyxSerializer(new ObjectFactory() {

			public ProtocolObject create(int classId) {
				return null;
			}
			public int version() {
				return 0;
			}
			
		});
		
		try {
			s.pack(p);
			fail("Class id is out of range.");
		} catch (IllegalArgumentException e) {
			// Expected
		}
	}
}
