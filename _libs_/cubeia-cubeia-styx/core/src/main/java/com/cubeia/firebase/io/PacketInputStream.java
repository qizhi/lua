/**
 * Copyright 2009 Cubeia Ltd  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cubeia.firebase.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Handles the deserialization of the Styx wire format.
 */
public final class PacketInputStream {
	
	public static final long MAX_UNSIGNED_INTEGER = (long) Math.pow(2,32);
	public static final long MAX_UNSIGNED_SHORT = (long) Math.pow(2,16);
	
	private final ByteBuffer inBuffer;

    public PacketInputStream(ByteBuffer inBuffer) {
        this.inBuffer = inBuffer;
    }
    
    public int peek() throws IOException {
    	int pos = inBuffer.position();
    	return (pos + 1 > inBuffer.limit() ? -1 : inBuffer.get(pos));
    }

    public byte loadByte() throws IOException {
        return inBuffer.get();
    }

    public int loadUnsignedByte() throws IOException {
        return BinaryData.asUnsigned(inBuffer.get());
    }

    public int loadUnsignedShort() throws IOException {
        int uint = 0;
        uint = uint | (inBuffer.get() & 0xff) << 8;
        uint = uint | (inBuffer.get() & 0xff);
        if ( uint < 0 ) {
        	uint += MAX_UNSIGNED_SHORT;
        }
        return uint;
    }

    public short loadShort() throws IOException {
        return inBuffer.getShort();
    }

    public int loadInt() throws IOException {
        return inBuffer.getInt();
    }

    public long loadUnsignedInt() {
        long uint = 0;
        uint = uint | (inBuffer.get() & 0xff) << 24;
        uint = uint | (inBuffer.get() & 0xff) << 16;
        uint = uint | (inBuffer.get() & 0xff) << 8;
        uint = uint | (inBuffer.get() & 0xff);
        if ( uint < 0 ) {
        	uint += MAX_UNSIGNED_INTEGER;
        }
        return uint;
    }
    
    public long loadLong() throws IOException {
        return inBuffer.getLong();
    }

    public boolean loadBoolean() throws IOException {
        return (inBuffer.get() != 0);
    }

    public String loadString() throws IOException {
        int length = 0xffff & inBuffer.getShort();
        byte[] utf8 = new byte[length];
        inBuffer.get(utf8);
        return new String(utf8, "UTF-8");
    }

	public void loadByteArray(byte[] arg0) throws IOException {
		inBuffer.get(arg0);
	}
	
	public void loadIntArray(int[] data) throws IOException {
		for (int i = 0; i < data.length; i++) {
			data[i] = inBuffer.getInt();
		}
	}
	
	public void loadUint32Array(long[] data) throws IOException {
		for (int i = 0; i < data.length; i++) {
			data[i] = loadUnsignedInt();
		}
	}
	
	public void loadUint16Array(int[] data) throws IOException {
		for (int i = 0; i < data.length; i++) {
			data[i] = loadUnsignedShort();
		}
	}

	public void loadShortArray(short[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            data[i] = inBuffer.getShort();
        }
    }

	public void loadLongArray(long[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            data[i] = inBuffer.getLong();
        }
    }
	
	public void loadBooleanArray(boolean[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            data[i] = inBuffer.get() != 0;
        }
    }
	
	public void loadStringArray(String[] removedParams) throws IOException {
		for (int i = 0; i < removedParams.length; i++) {
			removedParams[i] = loadString();
		}
	}
}
