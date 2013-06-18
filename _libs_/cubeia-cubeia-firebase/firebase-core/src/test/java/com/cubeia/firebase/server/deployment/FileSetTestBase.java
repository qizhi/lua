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
package com.cubeia.firebase.server.deployment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.cubeia.firebase.server.deployment.resources.DeploymentFileSet;
import com.cubeia.firebase.util.Files;

public abstract class FileSetTestBase extends TestCase {
	
	private final static AtomicInteger DIR_COUNTER = new AtomicInteger();

	protected File root;
	protected File tmp;
	
	protected DeploymentFileSet set;
	
	protected void setUp() throws Exception {
		root = createNewTempDir("_org");
		tmp = createNewTempDir("_tmp");
		copyTestFilesToRoot();
		set = new DeploymentFileSet(root, tmp);
		set.start();
	}

	protected void tearDown() throws Exception { 
		Files.recursiveDelete(root);
		Files.recursiveDelete(tmp);
		set.stop();
	}
	
	private void copyTestFilesToRoot() throws IOException {
		File[] arr = listTestFiles();
		for (File f : arr) {
			File next = new File(root, f.getName());
			if(f.isFile()) {
				Files.copyFile(f, next);
			} else {
				next.mkdir();
				Files.copyDirectory(f, next, false);
			}
		}
	}
	
	private File[] listTestFiles() {
		File dir = new File("src/test/resources/com/cubeia/firebase/server/deployment");	
		return dir.listFiles();
	}

	private File createNewTempDir(String postFix) {
		String tmp = System.getProperty("java.io.tmpdir");
		File file = new File(new File(tmp), getClass().getName() + postFix + "/" + System.currentTimeMillis() + "/" + DIR_COUNTER.incrementAndGet());
		System.out.println(file);
		file.mkdirs();
		return file;
	}
}
