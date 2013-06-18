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
package com.cubeia.firebase.test.common.rules.impl;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.test.common.rules.Expect;

public class ClassExpect implements Expect {

	private final Class<?> clazz;
	private Object object;
	
	public ClassExpect(Class<?> cl) {
		Arguments.notNull(cl, "class");
		clazz = cl;
	}
	
	public Action accept(Object o) {
		boolean b = clazz.isAssignableFrom(o.getClass());
		if(b) {
			object = o;
			return Action.DONE;
		} else {
			return Action.PASS_THROUGH;
		}
	}
	
	public Object result() {
		return object;
	}
	
	@Override
	public String toString() {
		return "class: " + clazz.getName();
	}
}
