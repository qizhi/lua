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

public enum Operator {

	EQUALS,
	NOT_EQUALS;
	
	public boolean doTest(Object one, Object two) {
		if(this == EQUALS) {
			if(one == null && two == null) {
				return true;
			} else if(one != null && two != null) {
				return one.equals(two);
			} else {
				return false;
			}
		} else {
			if(one == null && two == null) {
				return true;
			} else if(one != null && two != null) {
				return !one.equals(two);
			} else {
				return false;
			}
		}
	}
}
