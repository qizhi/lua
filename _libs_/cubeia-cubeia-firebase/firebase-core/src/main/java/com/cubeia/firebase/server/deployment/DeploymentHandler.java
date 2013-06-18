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

import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

/**
 * Handles changes for a certain type of resource.
 *  
 * 
 * @author Fredrik
 *
 */
public interface DeploymentHandler {
	
	/*
	 * If the deployment is managed in a class loader chain, this method will be called
	 * to supply the main deployment class loader. Example: any supplied class loader should be 
	 * used as "parent class loader" by game loaders to isolate the games from the rest of 
	 * the server.
	 * 
	 * <p>If used, this method is always called prior to any call to the "handle" method.
	 * 
	 * @param parent Main deployment class loader, never null
	 */
	// public void setDeploymentClassLoader(ClassLoader parent);
	
	/*
	 * Return a regex that is used a a filter for
	 * checking if the resource should be handled by
	 * this handler.
	 * 
	 * E.g.
	 * Datasource handler filter:
	 * '..*-ds.xml'
	 */
	// public String getFilter();
	
	/**
	 * Handle a change in the resource. It may be a new resource but it also may be a 
	 * change in an existing resource.
	 */
	public Deployment handle(DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException;

	/**
	 * This method is called before a new node is initialized. 
	 */
	public void registerNodeUp(NodeInfo info);
	
	/**
	 * This method is called before after a node is destroyed. 
	 */
	public void registerNodeDown(NodeInfo info);

}
