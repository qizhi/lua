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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Handles the serialization of the Styx wire format.
 */
public final class PacketOutputStream {
    /** maximum string legnth in bytes */
    public static final int STRING_MAX_BYTES = 0xffff;
    
    private DataOutputStream os;

    public PacketOutputStream(DataOutputStream os) {
        this.os = os;
    }

    public void saveByte(byte val) throws IOException {
        os.writeByte(val);
    }

    public void saveUnsignedByte(int val) throws IOException {
        os.writeByte(val);
    }

    public void saveUnsignedShort(int val) throws IOException {
        os.writeShort(val);
    }

    public void saveShort(short val) throws IOException {
        os.writeShort(val);
    }

    public void saveInt(int val) throws IOException {
        os.writeInt(val);
    }
    
    public void saveUnsignedInt(long val) throws IOException {
    	 os.write((byte) (0xff & val >>> 24));
    	 os.write((byte) (0xff & val >>> 16));
    	 os.write((byte) (0xff & val >>> 8));
    	 os.write((byte) (0xff & val));
    }
    
    public void saveLong(long val) throws IOException {
        os.writeLong(val);
    }

    public void saveBoolean(boolean val) throws IOException {
        os.writeByte(val ? 1 : 0);
    }

    /**
     * Save the given string. 
     * An {@link IOException} will be thrown if the number of bytes of the string encoded in 
     * UTF-8 is greater than {@link #STRING_MAX_BYTES}.
     * @param val the string
     * @throws IOException if the number of UTF-8 bytes of the string is >= {@link #STRING_MAX_BYTES}
     */
    public void saveString(String val) throws IOException {
    	if (val == null) val = "";
        byte[] utf8 = val.getBytes("UTF-8");
        
        if (utf8.length > STRING_MAX_BYTES) {
            throw new IOException("String byte length is too long: bytes = " + utf8.length + ", max allowed = " + 0xffff);
        }
        
        os.writeShort(utf8.length);
        os.write(utf8, 0, utf8.length);
    }

	public void saveArray(byte[] gamedata) throws IOException {
		os.write(gamedata);
	}
	
	public void saveArray(int[] data) throws IOException {
		for (int val : data) {
			os.writeInt(val);
		}
	}
	
	public void saveUint32Array(long[] data) throws IOException {
		for (long val : data) {
			saveUnsignedInt(val);
		}
	}
	
	public void saveUint16Array(int[] data) throws IOException {
		for (int val : data) {
			saveUnsignedShort(val);
		}
	}

	public void saveArray(long[] data) throws IOException {
        for (long val : data) {
            os.writeLong(val);
        }
    }
	
	public void saveArray(short[] data) throws IOException {
        for (short val : data) {
            os.writeShort(val);
        }
    }

	public void saveArray(boolean[] data) throws IOException {
        for (boolean val : data) {
            os.writeByte(val ? 1 : 0);
        }
    }

	public void saveArray(String[] removedParams) throws IOException {
		for (String name : removedParams) {
			saveString(name);
		}
	}
}
