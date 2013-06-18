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

import java.util.Map;

import se.xec.commons.path.StringPath;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.resources.DeploymentFileSet;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.deployment.resources.FileSetResource;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveClassLoader;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveDeployment;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveRevision;
import com.cubeia.firebase.server.service.DefaultServiceArchiveLayout;
import com.cubeia.firebase.server.service.ServiceArchive;
import com.game.server.bootstrap.SharedClassLoader;

// TODO: FIX!!!
public class DeploymentManagerTest extends FileSetTestBase {

	private ResourceManager resources;
	private DeploymentManager manager;

	protected void setUp() throws Exception {
		// prepareClasses();
		super.setUp();
		createResources();
		createManager();
	}

	protected void tearDown() throws Exception {
		manager.stop();
		resources.stop();
		super.tearDown();
	}
	
	
	// --- TEST METHODS --- //
	
	public void testDeployments() throws Exception { 
		manager.getAllDeployments();
		/*
		 * We're expecting:
		 * 
		 *  10 (all except SAR but including UAR and no PAR)
		 * + 7 (all UAR files except SAR)
		 * + 2 (exploded uar)
		 * =17
		 */
		// TODO
		// assertEquals(17, deps.size());
	}
	
	/*public void testChangeDs() throws Exception { 
		Map<String, Deployment> deps = manager.getAllDeployments(DeploymentType.DATA_SOURCE);
		assertEquals(2, deps.size());
		
		// Get root file
		File file = new File(root, "test-ds.xml");
		assertTrue(file.exists());
		
		// Wait for FS
		Thread.sleep(1000);
		
		// Modify and write
		String content = Files.readAsString(file);
		content = content.replace("root", "root2");
		Files.writeString(content, file);
		
		// Wait for event
		Thread.sleep(1000);
		
		// Check deployment content (should be changed)
		deps = manager.getAllDeployments(DeploymentType.DATA_SOURCE);
		assertEquals(2, deps.size());
		DatasourceDeployment dep = (DatasourceDeployment)deps.get("test-ds.xml");
		assertNotNull(dep);
		assertEquals("root2", dep.getProperties().get("user"));
	}*/

	public void xtestUarSameParentClassLoader() throws Exception {
		Map<String, Deployment> deps = manager.getAllDeployments(DeploymentType.UNIFIED_ARCHIVE);
		assertEquals(4, deps.size());
		
		// Get hold of class loader
		UnifiedArchiveDeployment dep = (UnifiedArchiveDeployment)deps.get("test2.uar");
		UnifiedArchiveRevision rev = dep.getLatestRevision();
		ClassLoader original = rev.getRevisionClassLoader();
		assertNotNull(original);
		
		// Get hold of game and tournament class loaders
		GameDeployment gameDep1 = manager.getGameDeploymentForId(113);
		ClassLoader g1 = gameDep1.getLatestRevision().getRevisionClassLoader();
		GameDeployment gameDep2 = manager.getGameDeploymentForId(114);
		ClassLoader g2 = gameDep2.getLatestRevision().getRevisionClassLoader();
		TournamentDeployment tourDep = manager.getTournamentDeploymentForId(123);
		ClassLoader g3 = tourDep.getLatestRevision().getRevisionClassLoader();
		
		// Check parent class loaders references
		assertTrue(g1.getParent() == g2.getParent());
		assertTrue(g2.getParent() == g3.getParent());
		assertTrue(g1.getParent() == original);
		
		// Load test classes
		String clazz = "free.util.Pair";
		assertTrue(g1.loadClass(clazz) == g2.loadClass(clazz));
		assertTrue(g2.loadClass(clazz) == g3.loadClass(clazz));
		assertTrue(g1.loadClass(clazz) == original.loadClass(clazz));
		
		// Now, make sure this isn't correct about the ordinary deployment..
		
		// Get hold of game and tournament class loaders
		gameDep1 = manager.getGameDeploymentForId(111);
		g1 = gameDep1.getLatestRevision().getRevisionClassLoader();
		gameDep2 = manager.getGameDeploymentForId(112);
		g2 = gameDep2.getLatestRevision().getRevisionClassLoader();
		tourDep = manager.getTournamentDeploymentForId(122);
		g3 = tourDep.getLatestRevision().getRevisionClassLoader();
		
		// Load test classes, check un-matching references
		assertTrue(g1.loadClass(clazz) != g2.loadClass(clazz));
		assertTrue(g2.loadClass(clazz) != g3.loadClass(clazz));
		assertTrue(g1.loadClass(clazz) != g3.loadClass(clazz));
	}
	
	public void xtestUarSameParentClassLoader_ExplodedUar() throws Exception {
		Map<String, Deployment> deps = manager.getAllDeployments(DeploymentType.UNIFIED_ARCHIVE);
		assertEquals(4, deps.size());
		
		// Get hold of class loader
		UnifiedArchiveDeployment dep = (UnifiedArchiveDeployment)deps.get("test5.uar");
		UnifiedArchiveRevision rev = dep.getLatestRevision();
		ClassLoader original = rev.getRevisionClassLoader();
		assertNotNull(original);
		
		// Get hold of game class loader
		GameDeployment gameDep1 = manager.getGameDeploymentForId(173);
		ClassLoader g1 = gameDep1.getLatestRevision().getRevisionClassLoader();
		
		// Check parent class loaders references
		assertTrue(g1.getParent() == original);
		
		// Load test classes
		String clazz = "free.util.Pair";
		assertTrue(g1.loadClass(clazz) == original.loadClass(clazz));
	}
	
	public void xtestUarUtilityClasses() throws Exception {
		doTestUtilityClasses("test3.uar");
		doTestUtilityClasses("test4.uar");
	}
	
	public void xtestSarUarPickup() throws Exception {
		/*
		 * Here we'll test the way the SAR files are treated
		 * on startup and see if we can load files that way (please 
		 * refer to the ArchiveDirector/ServiceClassLoader classes)
		 */
		DeploymentFileSet files = resources.getDeploymentFileSet();
		DeploymentResource resource = (DeploymentResource)files.getResource(new StringPath("test3.sar"));
		assertNotNull(resource);
		
		// create classes
		FileSetResource clone = resource.clone(0, true);
		ClassLoader myParent = getClass().getClassLoader();
		UnifiedArchiveClassLoader ua = new UnifiedArchiveClassLoader(myParent, clone);
		ServiceArchive sa = new ServiceArchive("test3", clone.getRoot(), DefaultServiceArchiveLayout.getInstance(), ua, new SharedClassLoader(myParent), false, false);
		ClassLoader loader = sa.getServiceClassLoader();
		
		// try load
		String clazz = "free.util.Pair";
		assertNotNull(loader.loadClass(clazz));
	}
	
	

	// --- PRIVATE METHODS --- //
	
	private void doTestUtilityClasses(String uarName) throws Exception {
		Map<String, Deployment> deps = manager.getAllDeployments(DeploymentType.UNIFIED_ARCHIVE);
		assertEquals(4, deps.size());
		
		// Get hold of class loader
		UnifiedArchiveDeployment dep = (UnifiedArchiveDeployment)deps.get(uarName);
		UnifiedArchiveRevision rev = dep.getLatestRevision();
		ClassLoader original = rev.getRevisionClassLoader();
		assertNotNull(original);
		
		// Load test classes
		String clazz = "free.util.Pair";
		assertNotNull(original.loadClass(clazz));
	}
	
	/*private void prepareClasses() {
		PersistenceDeploymentHandlerControl.setAsTest(true);
	}*/
	
	private void createManager() throws SystemException {
		manager = new DeploymentManager(resources, new ServiceContextMock());
		manager.start();
	}

	private void createResources() throws SystemException {
		resources = new ResourceManager(super.root, super.tmp, null);
		resources.start();
	}
}
