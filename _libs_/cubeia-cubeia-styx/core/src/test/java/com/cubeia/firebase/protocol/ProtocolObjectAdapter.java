package com.cubeia.firebase.protocol;

import java.io.IOException;

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.ProtocolObjectVisitor;

public class ProtocolObjectAdapter implements ProtocolObject {

	@Override
	public void accept(ProtocolObjectVisitor visitor) { }

	@Override
	public int classId() {
		return 0;
	}

	@Override
	public void save(PacketOutputStream ps) throws IOException { }

	@Override
	public void load(PacketInputStream ps) throws IOException { }

}
