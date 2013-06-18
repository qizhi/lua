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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.common.Identifiable;

public class ListProcessorChainTest extends TestCase {

	public void testStackOrder() {
		List<String> stack = new ArrayList<String>();
		List<ProcessorFilter<IntId, ProbeAction>> filters = new ArrayList<ProcessorFilter<IntId, ProbeAction>>();
		filters.add(new Processor("olle", stack));
		filters.add(new Processor("kalle", stack));
		filters.add(new Processor("pelle", stack));
		ListProcessorChain<IntId, ProbeAction> chain = new ListProcessorChain<IntId, ProbeAction>(filters);
		chain.next(new ProbeAction(666, 666, 666), new IntId(666));
		// check the stack length
		assertEquals(6, stack.size());
		// check the stack order
		assertEquals("olle", stack.get(0));
		assertEquals("kalle", stack.get(1));
		assertEquals("pelle", stack.get(2));
		assertEquals("pelle", stack.get(3));
		assertEquals("kalle", stack.get(4));
		assertEquals("olle", stack.get(5));
	}
	
	
	private static class Processor implements ProcessorFilter<IntId, ProbeAction> { 
		
		private final String id;
		private final List<String> testStack;

		private Processor(String id, List<String> testStack) {
			this.id = id;
			this.testStack = testStack;
		}
		
		@Override
		public void process(ProbeAction action, IntId data, ProcessorChain<IntId, ProbeAction> filters) {
			testStack.add(id);
			filters.next(action, data);
			testStack.add(id);
		}
	}
	
	private static class IntId implements Identifiable {
		
		private final int id;

		private IntId(int id) {
			this.id = id;
		}
		
		@Override
		public int getId() {
			return id;
		}
	}
}
