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
package com.cubeia.firebase.test.common.rules;

import java.util.LinkedList;

import com.cubeia.firebase.test.common.rules.impl.ClassExpect;
import com.cubeia.firebase.test.common.rules.impl.FilteredExpect;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.firebase.test.common.rules.impl.LinearGroupExpect;
import com.cubeia.firebase.test.common.rules.impl.MemberAssertFilter;
import com.cubeia.firebase.test.common.rules.impl.RandomGroupExpect;
import com.cubeia.firebase.test.common.util.Serializer;

/**
 * This is the base class for fluid style expect building. Please note that
 * this builder is still being developed, not all methods are implemented yet,
 * 
 * <p>The fluid classes are building expects in a strict left-to-right order, such 
 * that the following...
 * 
 * <pre>
 * 	new FluidBuilder().expect(Q).and(X).and(Y).or(Z)
 * </pre>
 * 
 * ... is equivalent to (in pseudo code)...
 *
 * <pre>
 *  (Q and X and Y) or (Z)
 * </pre>
 * 
 * If is possible to add fast fail "where" (or "with") clauses, which will be 
 * attached to the immediately previous expect, such that...
 * 
 * <pre>
 * 	new FluidBuilder().expect(Q).with(a).and(X).with(b)
 * </pre>
 * 
 * ... is equivalent to (in pseudo code)...
 * 
 * <pre>
 *  (Q (with a)) and (X (with b))
 * </pre>
 * 
 * Lists can be made in "linear" or "random", both of which acts as sparse lists (ie
 * allows items not matching the list to occur between significant items). For example, 
 * to match X, Y and Z in any order:
 * 
 * <pre>
 *  new FluidBuilder().expect(X).and(Y).and(Z).in(Order.RANDOM)
 * </pre>
 * 
 * @author Lars J. Nilsson
 */
public class FluidBuilder implements ExpectBuilder {
	
	private final LinkedList<Expect> list = new LinkedList<Expect>();
	
	public Action accept(Object o) {
		return new LinearGroupExpect(list).accept(o);
	}
	
	public Object result() {
		if(list.size() == 1) {
			return list.getLast().result();
		} else {
			return null;
		}
	}
	
	public AssertBuilder expect(Class<?> cl) {
		return expect(new ClassExpect(cl));
	}

	public AssertBuilder expect(Class<?> cl, Serializer ser) {
		return expect(new GameClassExpect(cl, ser));
	}

	public AssertBuilder expect(Expect e) {
		list.add(new FilteredExpect(e));
		return new Asserter();
	}

	
	// --- PRIVATE CLASSES --- //
	
	private class Asserter implements AssertBuilder, ContinuedAssertBuilder {

		public Action accept(Object o) {
			return FluidBuilder.this.accept(o);
		}
		
		public Object result() {
			return FluidBuilder.this.result();
		}
		
		public ContinuedAssertBuilder with(Filter filt) {
			((FilteredExpect)list.getLast()).add(filt);
			return this;
		}
		
		public ContinuedAssertBuilder andWith(Filter filt) {
			((FilteredExpect)list.getLast()).add(filt);
			return this;
		}
		
		public MemberBuilder andWhere(String member) {
			((FilteredExpect)list.getLast()).add(new MemberAssertFilter(member, null));
			return new Members();
		}
		
		public MemberBuilder where(String member) {
			((FilteredExpect)list.getLast()).add(new MemberAssertFilter(member, null));
			return new Members();
		}

		public AssertBuilder and(Class<?> cl) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder and(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder and(Expect e) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder butNot(Class<?> cl) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder butNot(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder butNot(Expect e) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder or(Class<?> cl) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder or(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder or(Expect e) {
			throw new UnsupportedOperationException();
		}

		public Expect in(Order order) {
			throw new UnsupportedOperationException();
		}
	}
	
	private class Members implements MemberBuilder {

		public ContinuedAssertBuilder is(Object value) {
			FilteredExpect last = (FilteredExpect) list.getLast();
			((MemberAssertFilter)last.getLastFilter()).setValue(value);
			return new Aggregator();
		}

		public ContinuedAssertBuilder isNot(Object value) {
			throw new UnsupportedOperationException();
		}
		
		public Action accept(Object o) {
			return FluidBuilder.this.accept(o);
		}
		
		public Object result() {
			return FluidBuilder.this.result();
		}
	}
	
	private class Aggregator implements AggregateBuilder, ContinuedAssertBuilder {

		public MemberBuilder andWhere(String member) {
			((FilteredExpect)list.getLast()).add(new MemberAssertFilter(member, null));
			return new Members();
		}
		
		public ContinuedAssertBuilder andWith(Filter filt) {
			((FilteredExpect)list.getLast()).add(filt);
			return this;
		}
		
		public Action accept(Object o) {
			return FluidBuilder.this.accept(o);
		}
		
		public Object result() {
			return FluidBuilder.this.result();
		}
		
		public AssertBuilder and(Class<?> cl) {
			list.add(new FilteredExpect(new ClassExpect(cl)));
			return new Asserter();
		}

		public AssertBuilder and(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder and(Expect e) {
			list.add(new FilteredExpect(e));
			return new Asserter();
		}

		public AssertBuilder butNot(Class<?> cl) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder butNot(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder butNot(Expect e) {
			throw new UnsupportedOperationException();
		}

		public Expect in(Order order) {
			if(order == Order.RANDOM) {
				return new RandomGroupExpect(list);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		public AssertBuilder or(Class<?> cl) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder or(Class<?> cl, Serializer ser) {
			throw new UnsupportedOperationException();
		}

		public AssertBuilder or(Expect e) {
			throw new UnsupportedOperationException();
		}
	}
}
