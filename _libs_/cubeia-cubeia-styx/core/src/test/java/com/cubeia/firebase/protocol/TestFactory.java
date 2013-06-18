package com.cubeia.firebase.protocol;

import com.cubeia.firebase.io.ObjectFactory;
import com.cubeia.firebase.io.ProtocolObject;


public class TestFactory implements ObjectFactory {
    public int version() {
        return 666;
    }

    public ProtocolObject create(int classId) {
        switch(classId) {
            case 42:
                return new TestClass();
            case 0:
                return new VersionPacket();
            default:
                throw new IllegalArgumentException("Unknown classId: " + classId);
        }
    }

}
