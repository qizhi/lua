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
package com.cubeia.firebase.server.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.InternalServiceRegistry;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;

/**
 * This resource locator lazily look up the underlying 
 * archive when called.
 * 
 * @author Larsan
 */
public class DeferredResourceLocator implements ResourceLocator {

	private int gameId, revisionId;
	
	public DeferredResourceLocator(int gameId, int revisionId) {
		this.gameId = gameId;
		this.revisionId = revisionId;
	}
	
	public InputStream openResource(String path) throws IOException {
		InternalServiceRegistry registry = InternalComponentAccess.getRegistry();
		DeploymentServiceContract service = registry.getServiceInstance(DeploymentServiceContract.class);
		GameDeployment deployment = service.getDeploymentManager().getGameDeploymentForId(gameId);
		GameRevision revision = deployment.getGameRevision(revisionId);
		File file = revision.getLayout().getResource(revision, path);
		if(file != null && file.exists() && file.isFile()) return new FileInputStream(file);
		else return null;
	}
}
