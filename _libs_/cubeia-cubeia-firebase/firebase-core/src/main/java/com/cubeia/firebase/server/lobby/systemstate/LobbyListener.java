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
package com.cubeia.firebase.server.lobby.systemstate;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;

/**
 * Listens
 *
 * @author Fredrik
 */
public interface LobbyListener {
	
	public void nodeCreated(LobbyPath path);
	
	/**
	 * 
	 * @param path
	 * @param changed, changed attributes. If size > 1, then this si a bulk insert.
	 * @param allData
	 */
	public void nodeAttributeChanged(NodeChangeDTO change);

	/**
	 * Report table removed from lobby.
	 * 
	 * @param path, full LobbyPath (must include tableid)
	 */
	public void tableRemoved(LobbyPath path);
	
	
	/**
	 * Report path removed. This is not called for tables, but 
	 * for example when tournaments are remove.
	 * 
	 * @param path Lobby path, of which the leaf has been removed
	 */
	public void nodeRemoved(String path);
	
}
