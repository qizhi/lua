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
package com.cubeia.firebase.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Utility methods for handling binary data.
 * Most of these were contained in the old Packet definition.
 *
 * @author Fredrik
 */
public class BinaryData {

	private static transient Logger log = Logger.getLogger(BinaryData.class);

	public static final int STRING_HEADER_BYTE_LENGTH = 1;

	public static String toString(ByteBuffer data) {
		byte[] bs = data.array();
		String text = Arrays.toString(bs);
		return text;
	}

	public static String toString(byte[] data) {
		return Arrays.toString(data);
	}

	/*
	 * Get a String length in number of bytes.
	 * 
	 */
	/*private static int getStringLengthUTF(String str) {
		int strlen = str.length();
		int utflen = 0;
		int c = 0;

		for (int i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}

		return strlen; // ERROR: RETURN UTFLEN?!
	}*/

	/*
	 * Get string field length for I/O packets
	 */
	/*public static int getStringFieldLengthUTF(String str) {
		return getStringLengthUTF(str)+STRING_HEADER_BYTE_LENGTH;
	}*/

	/**
	 * Small helper method
	 * 
	 * @param b
	 * @return
	 */
	public static int unsignedByteToInt(byte b) {
		return b & 0xff;
	}

	/**
	 * Convert n-byte-array to integer.
	 * Most significant byte first.
	 * 
	 * @param b
	 * @return
	 */
	public static int unsignedBytesToInt(byte[] b) {
		int sum = 0;
		for( int i = 0; i < b.length; i++ ){
			sum |= b[i]&0xff;
			if( i < b.length-1 ) sum <<=8;
		}
		return sum;
	}

	/**
	 * Convert 8-byte-array to long.
	 * Most significant byte first.
	 * 
	 * @param b
	 * @return
	 */
	public static long unsignedBytesToLong(byte[] b) {
		long sum = 0;
		for( int i = 0; i < b.length; i++ ){
			sum |= b[i]&0xff;
			if( i < b.length-1 ) sum <<=8;
		}
		return sum;
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
	 * Returns a byte array of size 4.
	 * 
	 * @param i
	 * @return
	 */
//	public static byte[] intToBytes(int i) {
//		byte[] dword = new byte[4];
//		dword[0] = (byte) ((i >> 24) & 0x000000FF);
//		dword[1] = (byte) ((i >> 16) & 0x000000FF);
//		dword[2] = (byte) ((i >> 8) & 0x000000FF);
//		dword[3] = (byte) (i & 0x00FF);
//		return dword;
//	}

	
	public static byte[] intToBytes (int integer) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(integer);
			dos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			log.error("Could not convert int to bytes: "+integer, e);
			return new byte[0];
		}
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
	 * @param d Date to convert, must not be null
	 * @return The date as a 32 bit signed int, as seconds since EPOCH in UTC
	 */
	public static byte[] dateToBytes(Date d) {
		int value = (int)(d.getTime() / 1000);
		byte[] bytes = intToBytes(value);
		return bytes;
	}

	/**
	 * Convert integer to 8-byte-array.
	 * Most significant byte first.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] longToBytes(long value) {
		byte[] bytes = new byte[8];
		for( int i = 0; i < 8; i++ ){
			bytes[8-1-i] = (byte)(value&0xff);
			value >>= 8;
		}
		return bytes;
	}
}
