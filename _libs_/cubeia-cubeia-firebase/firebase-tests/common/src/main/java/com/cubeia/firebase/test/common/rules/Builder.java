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

import com.cubeia.firebase.test.common.rules.impl.ClassExpect;
import com.cubeia.firebase.test.common.rules.impl.FilteredExpect;
import com.cubeia.firebase.test.common.rules.impl.GameClassExpect;
import com.cubeia.firebase.test.common.util.Serializer;

/**
 * A trivial static builder class.
 * 
 * @author Lars J. Nilsson
 */
public class Builder {

	/**
	 * Create class expect. This is equivalent to...
	 * 
	 * <pre>
	 * 	new ClassExpect(cl)
	 * </pre>
	 * 
	 * @param cl Class to expect, must not be null
	 * @return A new class expect, never null
	 */
	public static Expect expect(Class<?> cl) {
		return new ClassExpect(cl);
	}
	
	/**
	 * Create a class expect with filters. This is
	 * equivalent to...
	 * 
	 * <pre>
	 * 	new FilteredExpect(new ClassExpect(cl), filters)
	 * </pre>
	 * 
	 * @param cl Class to expect, must not be null
	 * @param filters Filters to use, may be null
	 * @return A new filtered expect, never null
	 */
	public static Expect expect(Class<?> cl, Filter...filters) {
		return new FilteredExpect(new ClassExpect(cl), filters);
	}
	
	/**
	 * Create a new game class expected. Ie an expect for a
	 * game data object, which uses the given serializer to 
	 * manifest itself. This is equivalent to...
	 * 
	 * <pre>
	 * 	new GameClassExpect(cl, serializer)
	 * </pre>
	 * 
	 * @param cl Game class to expect, must not be null
	 * @param serializer Serializer to use, must not be null
	 * @return A new game class expect, never null
	 */
	public static Expect expect(Class<?> cl, Serializer serializer) {
		return new GameClassExpect(cl, serializer);
	}
	
	public Builder() { }

}
