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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.test.common.rules.Expect;

public class LinearGroupExpect extends GroupExpect {
	
	private final Set<Expect> done = new HashSet<Expect>();
	
	public LinearGroupExpect(Expect...e) {
		super(e);
	}
	
	public LinearGroupExpect(List<Expect> list) {
		if(list != null) {
			super.list.addAll(list);
		}
	}

	public Action accept(Object o) {
		for (Expect e : super.list) {
			// Only run an expect once...
			if(!done.contains(e)) {
				Action a = e.accept(o);
				if(a == Action.DONE) {
					done.add(e);
				} else if(a == Action.FAIL) {
					return a; // EARLY RETURN
				} else {
					if(!checkRest(o, e)) {
						return Action.FAIL;
					}
					break;
				}
			}
		}
		if(done.size() == list.size()) {
			return Action.DONE;
		} else {
			return Action.PASS_THROUGH;
		}
	}

	public Object result() {
		return (list.size() == 1 ? list.getLast().result() : null);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private boolean checkRest(Object o, Expect from) {
		boolean pass = false;
		for (Expect e : super.list) {
			if(e == from) {
				pass = true;
			} else if(pass) {
				if(e.accept(o) == Action.DONE) {
					return false; // NO OTHER ACTION SHOULD MATCH
				}
			}
		}
		return true;
	}
}
