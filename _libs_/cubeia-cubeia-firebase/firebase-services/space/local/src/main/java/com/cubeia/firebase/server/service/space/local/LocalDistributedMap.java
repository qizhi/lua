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
package com.cubeia.firebase.server.service.space.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.ChannelException;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.space.service.RedistributionMap;

public class LocalDistributedMap<E extends Identifiable> implements RedistributionMap<E> {

	private final String name;
	private final Map<Integer, E> local;
	
	public LocalDistributedMap(String name) {
		local = new ConcurrentHashMap<Integer, E>();
		this.name = name;
	}
	
	@Override
	public void add(E... objects) {
		for (E e : objects) {
    		local.put(e.getId(), e);
    	}
	}

	@Override
	public void connect() throws ChannelException { }

	@Override
	public void disconnect() { }

	@Override
	public boolean existsBuddy(int id) {
		return false;
	}

	@Override
	public boolean existsLocal(int id) {
		return local.containsKey(id);
	}

	@Override
	public E get(int id) {
		return local.get(id);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public E peekBuddy(int i) {
		return null;
	}

	@Override
	public E peekLocal(int i) {
		return local.get(i);
	}

	@Override
	public void remove(int[] ids) {
    	for (int id : ids) {
    		local.remove(id);
    	}
	}

	@Override
	public void remove(int id) {
		remove(new int[] { id });
	}

	@Override
	public void reset() {
		local.clear();
	}

	@Override
	public void set(E object) {
		local.put(object.getId(), object);
	}
}
