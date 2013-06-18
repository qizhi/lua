package com.cubeia.firebase.protocol;

import java.io.IOException;

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.ProtocolObjectVisitor;

public class VersionPacket implements ProtocolObject {
	
    public int classId() {
        return 0;
    }

    public int game;
    public int operatorid;
    public int protocol;

    public void save(PacketOutputStream ps) throws IOException {
        ps.saveInt(game);
        ps.saveInt(operatorid);
        ps.saveInt(protocol);
    }

    public void load(PacketInputStream ps) throws IOException {
        game = ps.loadInt();
        operatorid = ps.loadInt();
        protocol = ps.loadInt();
    }
    

    public String toString() {
        String result = "VersionPacket :";
        result += " game["+game+"]" ;
        result += " operatorid["+operatorid+"]" ;
        result += " protocol["+protocol+"]" ;
        return result;
    }

	public void accept(ProtocolObjectVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
