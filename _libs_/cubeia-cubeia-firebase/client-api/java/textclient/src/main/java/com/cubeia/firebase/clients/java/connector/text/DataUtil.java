/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.clients.java.connector.text;

public class DataUtil {
	
	public static int asUnsigned(byte value) {
		return value & 0xFF;
	}
	
	public static int asUnsigned(short value) {
		return value & 0xFFFF;
	}
	
	public static long asUnsigned(int value) {
		return value & 0xFFFFFFFF;
	}
	
	/**
	 * Returns a byte array of size 4.
	 * 
	 * @param i
	 * @return
	 */
	public static byte[] intToBytes(int i) {
		byte[] dword = new byte[4];
		dword[0] = (byte) ((i >> 24) & 0x000000FF);
		dword[1] = (byte) ((i >> 16) & 0x000000FF);
		dword[2] = (byte) ((i >> 8) & 0x000000FF);
		dword[3] = (byte) (i & 0x00FF);
		return dword;
	}
	
	 /**
     * Convert integer to n-byte-array.
     * Most significant byte first.
     * 
     * @param b
     * @return
     */
    public static byte[] intToBytes(int value, int b) {
        byte[] bytes = new byte[b];
        for( int i = 0; i < b; i++ ){
           bytes[b-1-i] = (byte)(value&0xff);
           value >>= 8;
        }
        return bytes;
    }
    
    /**
     * Convert integer to a single byte.
     * The least significant byte will be returned.
     * I.e. only supports values up to 255.
     * 
     * @param b
     * @return
     */
    public static byte intToByte(int value) {
        byte[] bytes = intToBytes(value, 4);
        return bytes[3];
    }
	
	/**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}
