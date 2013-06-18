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
package com.cubeia.firebase.server.util;

import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class SysStateIdMapperMemory implements IdSysStateMapperMemory {

	private final static String KEY = "nextId";
	
	private final String fqn;
	private final SystemStateServiceContract state;
	
	public SysStateIdMapperMemory(SystemStateServiceContract state, String fqn) {
		this.state = state;
		this.fqn = fqn;
	}
	
	public int get() {
		Integer i = (Integer)state.getAttribute(fqn, KEY);
		if(i == null) {
			return -1;
		} else {
			return i.intValue();
		}
	}

	public void set(int id) {
		state.setAttribute(fqn, KEY, Integer.valueOf(id));
	}
}
