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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LobbyPathRegisterNode {
	
	final String name;
	final LobbyPathRegisterNode parent;
	final Map<String, LobbyPathRegisterNode> children;
	private final LobbyPathRegister register;
	
	LobbyPathRegisterNode(LobbyPathRegisterNode parent, String name, LobbyPathRegister register) {
		this.register = register;
		children = new TreeMap<String, LobbyPathRegisterNode>();
		this.parent = parent;
		this.name = name;
	}
	
	public void toString(StringBuilder b) {
		if(this == register.getRoot()) {
			b.append("/");
		} else {
			b.append(toString());
		}
		for (LobbyPathRegisterNode child : children.values()) {
			child.toString(b.append(", "));
		}
	}

	public void remove(String child) {
		children.remove(child);
	}

	public void collect(Set<String> set, boolean onlyEndNodes) {
		if(onlyEndNodes) {
			if(children.size() == 0 && this != register.getRoot()) {
				addSelf(set);
			} else if(children.size() > 0) {
				addChildren(set, onlyEndNodes);
			}
		} else {
			addChildren(set, onlyEndNodes);
			if(this != register.getRoot()) {
				addSelf(set);
			}
		}
	}

	private void addChildren(Set<String> set, boolean onlyEndNodes) {
		for (LobbyPathRegisterNode node : children.values()) {
			node.collect(set, onlyEndNodes);
		}
	}

	private void addSelf(Set<String> set) {
		set.add(toString());
	}

	public LobbyPathRegisterNode get(String child, boolean create) {
		LobbyPathRegisterNode node = children.get(child);
		if(node == null && create) {
			node = new LobbyPathRegisterNode(this, child, register);
			children.put(child, node);
		}
		return node;
	}
	
	public String getPath() {
		return toString();
	}

	@Override
	public String toString() {
		return (parent == null ? "" : parent.toString() + "/" + name); 
	}
}