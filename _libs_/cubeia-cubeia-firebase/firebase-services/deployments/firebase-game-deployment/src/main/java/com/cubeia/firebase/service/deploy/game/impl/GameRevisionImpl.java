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
package com.cubeia.firebase.service.deploy.game.impl;

import java.io.IOException;

import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.server.deployment.RevisionClassLoader;
import com.cubeia.firebase.server.deployment.game.GameDeploymentLayout;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

/**
 * Holds the definition of a game version revision.
 * When a game archive is deployed runtime, the revision
 * version will be incremented and a new base folder will be created
 * that holds the exploded archive.
 * 
 * @author Fredrik
 *
 */
public class GameRevisionImpl implements GameRevision {
	
	private final int version;
	private final GameDefinition def;
	private final DeploymentResource resources;
	private final ClassLoader parentLoader;
	private final RevisionClassLoader gameLoader;
	
	//private final AtomicBoolean isGameSupport = new AtomicBoolean(false);
	//private final AtomicBoolean isGameSupportSet = new AtomicBoolean(false);
	
	GameRevisionImpl(int version, DeploymentResource resources, ClassLoader parentLoader) throws IOException {
		this.version = version;
		this.resources = resources;
		def = getLayout().getGameDefinition(this);
		this.parentLoader = parentLoader;
		gameLoader = setupClassLoader();
	}


	public String toString() {
		return "v:"+version+" resource:"+resources + " def:"+def;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#getVersion()
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#getResource()
	 */
	@Override
	public DeploymentResource getResource() {
		return resources;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#getLayout()
	 */
	@Override
	public GameDeploymentLayout getLayout() {
		return DefaultRevisionLayout.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#getRevisionClassLoader()
	 */
	@Override
	public ClassLoader getRevisionClassLoader() {
		return gameLoader;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#getGameDefinition()
	 */
	@Override
	public GameDefinition getGameDefinition() {
		return def;
	}
	
	
	/*
	 * This method returns true if the game for this revision is
	 * of the game support type. Please note that this method may throw
	 * a class not found exception if it is called before a game instance is
	 * created or the class is first referenced.
	 * 
	 * @return True if the game is of type game support, false otherwise
	 * @throws ClassNotFoundException If the class is needed, but cannot be found
	 */
	/*public boolean isGameSupport() throws ClassNotFoundException {
		if(isGameSupportSet.get()) return isGameSupport.get();
		else return checkGameSupport(loadGameClass());
	}*/

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#loadGameClass()
	 */
	@Override
	public Class<?> loadGameClass() throws ClassNotFoundException {
		return gameLoader.loadClass(def.getClassname());
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameRevision#newGameInstance()
	 */
	@Override
	public Game newGameInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> cl = loadGameClass();
		return (Game)cl.newInstance();
	}


	
	// --- PRIVATE METHODS ---- //
	
	/*private boolean checkGameSupport(Class cl) {
		boolean b = GameSupport.class.isAssignableFrom(cl);
		isGameSupportSet.set(true);
		isGameSupport.set(b);
		return b;
	}*/

	private RevisionClassLoader setupClassLoader() {
		return new RevisionClassLoaderFactory(parentLoader).createClassLoader(this);
	}
}
