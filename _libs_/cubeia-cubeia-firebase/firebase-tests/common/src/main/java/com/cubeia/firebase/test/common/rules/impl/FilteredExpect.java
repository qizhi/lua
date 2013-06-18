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

import java.util.LinkedList;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.test.common.rules.Expect;
import com.cubeia.firebase.test.common.rules.Filter;

public class FilteredExpect implements Expect {

	private final Expect expect;
	private final LinkedList<Filter> filters;
	
	public FilteredExpect(Expect expect) {
		this(expect, (Filter[])null);
	}
	
	public FilteredExpect(Expect expect, Filter...filters) {
		Arguments.notNull(expect, "expect");
		this.expect = expect;
		this.filters = new LinkedList<Filter>();
		if(filters != null) {
			for (Filter filt : filters) {
				this.filters.add(filt);
			}
		}
	}
	
	public Object result() {
		return expect.result();
	}
	
	public void add(Filter filter) {
		Arguments.notNull(filter, "filter");
		filters.add(filter);
	}
	
	public Filter getLastFilter() {
		if(filters.size() > 0) {
			return filters.getLast();
		} else {
			return null;
		}
	}
	
	public Action accept(Object o) {
		Action acc = expect.accept(o);
		if(acc == Action.DONE) {
			Object next = expect.result();
			for (Filter filt : filters) {
				if(!filt.accept(next)) {
					return Action.PASS_THROUGH; // EARLY RETURN
				}
			}
			return Action.DONE;
		} else {
			return acc;
		}
	}
	
	@Override
	public String toString() {
		return "filtered: " + expect.toString();
	}
}
