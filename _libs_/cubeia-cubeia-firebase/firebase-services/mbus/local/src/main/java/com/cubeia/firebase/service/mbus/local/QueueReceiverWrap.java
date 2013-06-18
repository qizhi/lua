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
package com.cubeia.firebase.service.mbus.local;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.mbus.common.KeyedQueueObject;
import com.cubeia.firebase.service.mbus.common.StrictQueueReceiver;

public class QueueReceiverWrap implements StrictQueueReceiver<String> {

	private final Deque<KeyedQueueObject<String>> queue = new LinkedBlockingDeque<KeyedQueueObject<String>>();
	private final Map<String, KeyedQueueObject<String>> exec = new ConcurrentHashMap<String, KeyedQueueObject<String>>();
	
	public void put(Event<?> e) {
		queue.add(new QueuedObject(e));
	}
	
	@Override
	public void push(KeyedQueueObject<String> o) {
		exec.remove(o.getKey());
		queue.addLast(o);
	}
	
	@Override
	public KeyedQueueObject<String> dequeue() {
		KeyedQueueObject<String> o = queue.poll();
		if(o == null) return null; // EARLY RETURN
		exec.put(o.getKey(), o);
		return o;
	}

	@Override
	public void remove(String key) {
		exec.remove(key);
	}

	@Override
	public int size() {
		return queue.size();
	}
}