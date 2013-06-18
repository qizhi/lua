/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.service.random.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.cubeia.firebase.service.random.api.RandomSeedService;

public class MarsenneTwisterTest {

	@Test
	public void testSequence() throws Exception {
		int[] check = readSequence();
		int[] local = generateNewSequence();
		assertEquals(check.length, local.length);
		assertArrayEquals(check, local);
	}
	
	@Test
	public void tetRemoteSeed() throws Exception {
		RandomSeedService seeder = mock(RandomSeedService.class);
		new MarsenneTwister(seeder);
		verify(seeder).seed(new int[624]);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullSeed() throws Exception {
		new MarsenneTwister(new int[] { });
	}

	
	// --- PRIVATE METHODS --- //
	
	private int[] generateNewSequence() {
		int[] arr = new int[10000];
		MarsenneTwister mt = new MarsenneTwister(new int[] { 0, 0 });
		for (int i = 0; i < arr.length; i++) {
			arr[i] = mt.next(32);
		}
		return arr;
	}
	
	private int[] readSequence() throws Exception {
		byte[] bytes = readZipBytes();
		List<Integer> list = extractInts(bytes);
		int[] arr = new int[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}

	private List<Integer> extractInts(byte[] bytes) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
		List<Integer> list = new ArrayList<Integer>(10000);
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.startsWith("#") && line.length() > 0) {
				list.add(Integer.parseInt(line));
			}
		}
		return list;
	}

	private byte[] readZipBytes() throws FileNotFoundException, IOException {
		InputStream in = getClass().getClassLoader().getResourceAsStream("test-sequence.zip");
		if(in == null) {
			throw new FileNotFoundException("test-sequence.zip");
		}
		ZipInputStream zip = new ZipInputStream(in);
		zip.getNextEntry();
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		byte[] buff = new byte[1028];
		int len = 0;
		while((len = zip.read(buff)) != -1) {
			ba.write(buff, 0, len);
		}
		zip.close();
		byte[] bytes = ba.toByteArray();
		return bytes;
	}
}
