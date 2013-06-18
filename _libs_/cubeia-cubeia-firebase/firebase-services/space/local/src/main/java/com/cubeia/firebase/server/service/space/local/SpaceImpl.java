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

import javax.management.MBeanServer;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.space.SpaceConfig;
import com.cubeia.space.service.AbstractLockSpace;

public class SpaceImpl<E extends Identifiable> extends AbstractLockSpace<E> {

	protected SpaceImpl(String name, SpaceConfig conf, EventType type, MBusContract mbus, MBeanServer serv, CoreTransactionManager manager) {
		super(name, conf, type, mbus, serv, manager);
		space = new LocalDistributedMap<E>(name);
	}
}