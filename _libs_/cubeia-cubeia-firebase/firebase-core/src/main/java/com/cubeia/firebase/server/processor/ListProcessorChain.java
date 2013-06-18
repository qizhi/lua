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
package com.cubeia.firebase.server.processor;

import static com.cubeia.firebase.api.util.Arguments.notNull;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.common.Identifiable;

public class ListProcessorChain<T extends Identifiable, A extends Action> implements ProcessorChain<T, A> {

	private int index = 0;
	protected ArrayList<ProcessorFilter<T, A>> list;

	public ListProcessorChain(ProcessorFilter<T, A>...filters) {
		notNull(filters, "filters");
		this.list = new ArrayList<ProcessorFilter<T, A>>(filters.length);
		for (ProcessorFilter<T, A> filter : filters) {
			list.add(filter);
		}
	}
	
	public ListProcessorChain(List<ProcessorFilter<T, A>> filters) {
		notNull(filters, "filters");
		this.list = new ArrayList<ProcessorFilter<T, A>>(filters.size());
		this.list.addAll(filters);
	}
	
	public ListProcessorChain() {
		this.list = new ArrayList<ProcessorFilter<T, A>>();
	}

	@Override
	public void next(A action, T data) {
		if (index < list.size()) {
			list.get(index++).process(action, data, this);
		}
	}
}
