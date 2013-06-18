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
package com.cubeia.firebase.server.deployment.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.mockito.Mockito;

import se.xec.commons.fileset.FileSetEvent;
import se.xec.commons.fileset.FileSetListener;
import se.xec.commons.path.Path;
import se.xec.commons.path.StringPath;

import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.FileSetTestBase;
import com.cubeia.firebase.util.Files;

public class DeploymentFileSetTest extends FileSetTestBase {

	public void testIOError() throws Exception {
		File root = Mockito.mock(File.class);
		Mockito.when(root.exists()).thenReturn(true);
		/*
		 * Trac issues #606: when the file set is created with a moch, "listFiles()" returns
		 * null, just as it would on IO errors, do not fail with a NPE
		 */
		try {
			new DeploymentFileSet(root, tmp); 
		} catch(NullPointerException e) {
			Assert.fail("NPE after File.listFiles(); trac issue #606");
		}
	}
	
	public void testListResources() throws Exception {
		Path[] paths = set.getResourcePaths();
		
		/*
		 * This should be 12 as "dummy.txt" should not be listed
		 */
		assertEquals(12, paths.length);
		
		/*
		 * We should have two gar files
		 */
		assertEquals(2, set.getResourcesByType(DeploymentType.GAME_ARCHIVE).length);
		
		/*
		 * And 4 uar files (3 files + 1 directory)
		 */
		assertEquals(4, set.getResourcesByType(DeploymentType.UNIFIED_ARCHIVE).length);
	}
	
	public void testListen() throws Exception {
		Listener l = new Listener();
		set.addFileSetListener(l);
		String name = "test2.uar";
		
		/*
		 * At this point we must wait before touching the
		 * file as we might miss it otherwise.
		 */
		Thread.sleep(2000);
		touchFile(new File(root, name));
		Thread.sleep(600);
		
		/*
		 * Make sure we have had a change event
		 */
		assertNotNull(l.event);
		
		/*
		 * Check resource name
		 */
		Path path = l.event.getResource().getPath();
		assertEquals(name, path.getName());
	}
	
	public void testCopy() throws Exception {
		Path p = new StringPath("test2.uar");
		DeploymentResource r = (DeploymentResource)set.getResource(p);
		
		/*
		 * Check that we have the resource
		 */
		assertNotNull(r);
	
		/*
		 * Check that we have the resource COPY
		 */
		DeploymentResource r2 = (DeploymentResource)set.getResourceCopy(r, 1, false);
		assertNotNull(r2);
	}
	

	
	// --- PRIVATE METHODS --- //
	
	private void touchFile(File file) throws IOException {
		Files.writeBytes(new ByteArrayInputStream(Files.getBytes(file)), file);
	}
	
	
	// --- INNER CLASSES --- //
	
	private static class Listener implements FileSetListener {
		
		private FileSetEvent event;

		public void receiveFileSetEvent(FileSetEvent event) {
			this.event = event;
		}
	}
}
