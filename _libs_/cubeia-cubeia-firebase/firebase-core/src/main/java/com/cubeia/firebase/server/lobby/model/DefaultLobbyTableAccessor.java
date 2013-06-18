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
import com.cubeia.firebase.api.game.lobby.LobbyTableAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class DefaultLobbyTableAccessor extends LobbyAccessorBase implements LobbyTableAccessor {

	public DefaultLobbyTableAccessor(SystemStateServiceContract state) {
		super(state);
	}
	
	public void clearNode(LobbyPath path) {
		doClearNode(path);
	}

	public Map<String, AttributeValue> getAllAttributes(LobbyPath path) {
		return doGetAllAttributes(path);
	}

	public AttributeValue getAttribute(LobbyPath path, String attribute) {
		return doGetAttribute(path, attribute);
	}

	public Date getDateAttribute(LobbyPath path, String attribute) throws ClassCastException {
		return doGetDateAttribute(path, attribute);
	}

	public int getIntAttribute(LobbyPath path, String attribute) throws ClassCastException {
		return doGetIntAttribute(path, attribute);
	}

	public String getStringAttribute(LobbyPath path, String attribute) throws ClassCastException {
		return doGetStringAttribute(path, attribute);
	}

	public boolean hasNode(LobbyPath path) {
		return doHasNode(path);
	}

	public void removeAttribute(LobbyPath path, String attribute) {
		doRemoveAttribute(path, attribute);
	}

	public void removeNode(LobbyPath path) {
		doRemoveNode(path);
	}

	public void setAttribute(LobbyPath path, String attribute, AttributeValue value) {
		doSetAttribute(path, attribute, value);
	}

	public void setDateAttribute(LobbyPath path, String attribute, Date value) {
		doSetDateAttribute(path, attribute, value);
	}

	public void setIntAttribute(LobbyPath path, String attribute, int value) {
		doSetIntAttribute(path, attribute, value);
	}

	public void setStringAttribute(LobbyPath path, String attribute, String value) {
		doSetStringAttribute(path, attribute, value);
	}


}
