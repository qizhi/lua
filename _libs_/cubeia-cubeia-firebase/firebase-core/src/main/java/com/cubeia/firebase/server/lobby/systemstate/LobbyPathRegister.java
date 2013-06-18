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

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;

import com.cubeia.firebase.util.FirebaseLockFactory;

public class LobbyPathRegister {

	private static Logger log = Logger.getLogger(LobbyPathRegister.class);
	
	private final LobbyPathRegisterNode root = new LobbyPathRegisterNode(null, "/", this);
	
	/*
	 * Trac #562: Using fair locks + #581
	 */
	private final ReadWriteLock lock = FirebaseLockFactory.createLock();
	
	/**
	 * This method should only ever be called for leaf nodes. In effect
	 * the method will strip the last path object from the path and call
	 * {@link #registerPath(String) with the remainder}.
	 * 
	 * @param Leaf path, the last id will be stripped, must not be null
	 */
	public void registerLeaf(String path) {
		int i = path.lastIndexOf('/');
		if(i != -1) {
			path = path.substring(0, i);
			registerPath(path);
		}
	}
	
	/**
	 * This method registers a leaf path. If the path 
	 * already exists this method quietly returns.
	 * 
	 * @param path Path to register, must not be leaf, and not null
	 */
	public void registerPath(String path) {
		lock.writeLock().lock();
		try {
			lockedRegisterPath(path);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * This method unregisters a node, ie the end of
	 * a path and all children and descendants of the path.
	 * 
	 * @param path Path to unregister, must not be null
	 */
	public void unregisterNode(String path) {
		lock.writeLock().lock();
		try {
			lockedUnregisterNode(path);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Set<String> getNodes(String path) {
		lock.readLock().lock();
		try {
			return lockedGetNodes(path, false);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			StringBuilder b = new StringBuilder("{ ");
			root.toString(b);
			b.append(" }");
			return b.toString();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Set<String> getEndNodes(String path) {
		lock.readLock().lock();
		try {
			return lockedGetNodes(path, true);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void lockedRegisterPath(String path) {
		LobbyPathRegisterNode current = root;
		String[] arr = path.split("/");
		for (String name : arr) {
			if(name.length() > 0) {
				current = current.get(name, true);
			}
		}
	}
	
	
	private Set<String> lockedGetNodes(String path, boolean onlyEndNodes) {
		Set<String> set = new TreeSet<String>();
		LobbyPathRegisterNode root = lockedFind(path);
		if(root != null) {
			root.collect(set, onlyEndNodes);
		}
		return set;
	}
	
	private void lockedUnregisterNode(String path) {
		LobbyPathRegisterNode node = lockedFind(path);
		if (node == null) {
			log.warn("Tried to remove non-existing node (will be ignored): "+path);
		} else if(node == root) {
			root.children.clear();
		} else {
			LobbyPathRegisterNode parent = node.parent;
			parent.remove(node.name);
		}
	}

	private LobbyPathRegisterNode lockedFind(String path) {
		LobbyPathRegisterNode current = root;
		String[] arr = path.split("/");
		for (String name : arr) {
			if(name.length() > 0) {
				LobbyPathRegisterNode next = current.get(name, false);
				if(next == null) {
					return null; // EARLY RETURN, THIS NODE DOES NOT EXIST
				} else {
					current = next;
				}
			}
		}
		return current;
	}



	/// --- PACKAGE METHODS --- ///
	
	LobbyPathRegisterNode getRoot() {
		return root;
	}
}
