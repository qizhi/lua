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
package com.cubeia.firebase.server.master;

/**
 * This mbean contains information about all known participants in
 * the Firebase cluster.
 * 
 * @author Larsan
 */
public interface ClusterNodeRegistryMBean {
	
	/**
	 * Count the number of active participants in
	 * the cluster. 
	 * 
	 * @return The number of participants
	 */
	public int getRegistrySize();

	/**
	 * Get all participants in a string form. Each string in the array represents
	 * a single node. The string format is:
	 * 
	 * <blockquote>
	 * 		{id: &lt;node-id&gt;[&lt;server-id&gt;[&lt;comm-ip&gt;:&lt;comm-port&gt;]]; role: &lt;role&gt;; isCoordinator: &lt;true | false&gt;}
	 * </blockquote>
	 * 
	 * @return All participants in string for, never null
	 */
	public String[] printAllParticipants();

}
