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
package com.cubeia.firebase.service.action;

import java.util.concurrent.atomic.AtomicLong;

import com.cubeia.firebase.api.action.ActionId;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;

/**
 * See the {@link IdGenerator implemented interface} for a functional 
 * description of this service. This is a trivial implementation using an
 * atomic long for counter, which is combined with the server ID from the
 * service context.
 * 
 * @see IdGenerator
 * @see ActionId
 * @author Lars J. Nilsson
 */
public class Generator implements IdGenerator, Service {

	private ServiceContext con;
	private AtomicLong counter;
	
	/**
	 * Create a new generator, this initiated an internal atomic
	 * long for sequence generation.
	 */
	public Generator() {
		counter = new AtomicLong();
	}
	
	@Override
	public ActionId generate() {
		return new Id(con.getServerId(), counter.incrementAndGet());
	}

	@Override
	public void destroy() { }

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }

}
