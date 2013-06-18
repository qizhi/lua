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
package com.cubeia.firebase.server.lobby.model;

import java.util.Date;
import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class DefaultLobbyAttributeAccessor extends LobbyAccessorBase implements LobbyTableAttributeAccessor {

	private final LobbyPath path;

	public DefaultLobbyAttributeAccessor(SystemStateServiceContract state, LobbyPath path) {
		super(state);
		this.path = path;
	}
	
	public String getFullPath() {
		return path.getSystemPath();
	}

	public Map<String, AttributeValue> getAllAttributes() {
		return doGetAllAttributes(path);
	}

	public AttributeValue getAttribute(String attribute) {
		return doGetAttribute(path, attribute);
	}

	public Date getDateAttribute(String attribute) throws ClassCastException {
		return doGetDateAttribute(path, attribute);
	}

	public int getIntAttribute(String attribute) throws ClassCastException {
		return doGetIntAttribute(path, attribute);
	}

	public String getStringAttribute(String attribute) throws ClassCastException {
		return doGetStringAttribute(path, attribute);
	}

	public void removeAttribute(String attribute) {
		doRemoveAttribute(path, attribute);
	}

	public void setAttribute(String attribute, AttributeValue value) {
		doSetAttribute(path, attribute, value);
	}

	public void setDateAttribute(String attribute, Date value) {
		doSetDateAttribute(path, attribute, value);
	}

	public void setIntAttribute(String attribute, int value) {
		doSetIntAttribute(path, attribute, value);
	}

	public void setStringAttribute(String attribute, String value) {
		doSetStringAttribute(path, attribute, value);
	}
	
	/**
	 * Batched attribute update (recommended for performance)
	 * 
	 * @param o
	 * @return
	 */
	public void setAttributes(Map<String, AttributeValue> attributes) {
		doSetAttributes(path, attributes);
	}
	
	public boolean exists() {
		return doHasNode(path);
	}	
}
