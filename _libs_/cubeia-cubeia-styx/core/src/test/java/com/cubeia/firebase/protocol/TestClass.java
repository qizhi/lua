package com.cubeia.firebase.protocol;

import java.io.IOException;

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.ProtocolObjectVisitor;

public class TestClass implements ProtocolObject {

    public int classId() {
        return 42;
    }

    public Integer intVal;
    public String  strVal;

    public void save(PacketOutputStream ps) throws IOException {
        ps.saveInt(intVal);
        ps.saveString(strVal);
    }

    public void load(PacketInputStream ps) throws IOException {
        intVal = ps.loadInt();
        strVal = ps.loadString();
    }

	public void accept(ProtocolObjectVisitor visitor) {
		
	}


	
}
