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
package com.cubeia.firebase.util.file;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.util.Files;

public class FileFinderTest extends TestCase {

	private File FOLDER = new File("src/test/resources/com/cubeia/firebase/util/file");
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testFindXML() throws Exception {
		List<File> files = Files.getResources(FOLDER, ".*.xml", false);
		assertEquals(4, files.size());
		List<File> files2 = Files.getResources(FOLDER, ".*-ds.xml", false);
		assertEquals(2, files2.size());
	}
	
}
