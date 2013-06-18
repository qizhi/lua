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
package com.cubeia.space.service;

import java.util.concurrent.locks.ReentrantLock;

import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.ResourceException;

public class LockResource implements CoreResource {

	private final ReentrantLock lock;
	// private final int id;

	public LockResource(ReentrantLock lock, int id) {
		this.lock = lock;
		// this.id = id;
	}

	@Override
	public void commit() throws ResourceException { 
		unlock();
	}

	@Override
	public void prepare() throws ResourceException { }

	@Override
	public void rollback() { 
		unlock();
	}

	
	// --- PRIVATE METHODS --- //
	
	private void unlock() {
		if(lock.isHeldByCurrentThread()) {
			lock.unlock();
		}
	}
}
