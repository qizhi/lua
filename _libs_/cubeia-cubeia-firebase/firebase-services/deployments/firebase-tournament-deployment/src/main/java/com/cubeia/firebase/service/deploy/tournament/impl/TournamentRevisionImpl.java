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
package com.cubeia.firebase.service.deploy.tournament.impl;

import java.io.IOException;

import com.cubeia.firebase.api.mtt.MTTLogic;
import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.server.deployment.RevisionClassLoader;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeploymentLayout;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

/**
 * Holds the definition of a tournament version revision.
 * When a tournament archive is deployed runtime, the revision
 * version will be incremented and a new base folder will be created
 * that holds the exploded archive.
 * 
 * @author Fredrik
 *
 */
public class TournamentRevisionImpl implements TournamentRevision {
	
	private final int version;
	private final TournamentDefinition def;
	private final DeploymentResource resource;
	private final ClassLoader parentLoader;
	private final RevisionClassLoader tournamentLoader;
	
	TournamentRevisionImpl(int version, DeploymentResource resource, ClassLoader parentLoader) throws IOException {
		this.version = version;
		this.resource = resource;
		def = getLayout().getTournamentDefinition(this);
		this.parentLoader = parentLoader;
		tournamentLoader = setupClassLoader();
	}


	public String toString() {
		return "v:"+version+" resource:"+resource+" def:"+def;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#getVersion()
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#getResource()
	 */
	@Override
	public DeploymentResource getResource() {
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#getLayout()
	 */
	@Override
	public TournamentDeploymentLayout getLayout() {
		return DefaultRevisionLayout.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#getRevisionClassLoader()
	 */
	@Override
	public ClassLoader getRevisionClassLoader() {
		return tournamentLoader;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#getTournamentDefinition()
	 */
	@Override
	public TournamentDefinition getTournamentDefinition() {
		return def;
	}
	
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#loadTournamentClass()
	 */
	@Override
	public Class<?> loadTournamentClass() throws ClassNotFoundException {
		return tournamentLoader.loadClass(def.getClassname());
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentRevision#newTournamentInstance()
	 */
	@Override
	public MTTLogic newTournamentInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> cl = loadTournamentClass();
		return (MTTLogic)cl.newInstance();
	}


	
	// --- PRIVATE METHODS ---- //
	
	private RevisionClassLoader setupClassLoader() {
		return new RevisionClassLoaderFactory(parentLoader).createClassLoader(this);
	}
}
