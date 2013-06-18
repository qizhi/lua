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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.transaction.CoreResourceAdapter;
import com.cubeia.firebase.transaction.ResourceException;

public class SpaceResource<E extends Identifiable> extends CoreResourceAdapter {

	private final AbstractLockSpace<E> space;
	private final E obj;
	// private final int id;

	public SpaceResource(E obj, int id, AbstractLockSpace<E> space) {
		this.obj = obj;
		// this.id = id;
		this.space = space;
	}

	@Override
	public void commit() throws ResourceException {
		boolean done = false;
		long millis = System.currentTimeMillis();
		try {
			space.put(obj);
			done = true;
		} catch(LockException e) {
			/*
			 * Ticket #558: We have an illegal state exception because
			 * the table has been removed during execution, we'll log a 
			 * warning and go on. /LJN
			 */
			Logger.getLogger(getClass()).warn("Failure: object " + obj.getId() + " is not locked, causing commit to fail. This is most likely due to a removed table or tournament (see Trac issue 554).");
		} finally {
			if(done) {
				millis = System.currentTimeMillis() - millis;
				space.recordCommitTime(millis);
			}
		}
	}
}
