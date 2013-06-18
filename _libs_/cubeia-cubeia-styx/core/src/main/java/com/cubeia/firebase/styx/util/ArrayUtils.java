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
package com.cubeia.firebase.styx.util;

/**
 * This class contains utility methods for handling arrays.
 */
public class ArrayUtils {

	/**
	 * Gets a {@link String} representation of a byte array.
	 * 
	 * The byte {@link String} will only contain the first <code>maxPrintSize</code> bytes.
	 * If the array is longer than maxPrintSize, the size of the array will be appended at
	 * the end of the string.
	 * 
	 * If the array is null, "null" will be returned.
	 * 
	 * @param array
	 * @param maxPrintSize
	 * @return a {@link String} representation of the byte array
	 */
	public static String toString(final byte[] array, final int maxPrintSize) {
		if (array == null) {
			return "null";
		}
		if (array.length == 0) {
			return "{}";
		}
		
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append(array[0]);
		
		for (int i = 1; i < Math.min(array.length, maxPrintSize); i++) {
			s.append(", ").append(array[i]);
		}
		
		if (array.length > maxPrintSize) {
			s.append("... (" + array.length).append(")");
		}
		s.append("}");
		
		return s.toString();
	}	
}
