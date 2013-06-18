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
package com.cubeia.firebase.service.mbus.common;

import java.util.Set;

public interface SelectorSet {

	public void add(int i);

	public void add(Set<Integer> set);

	public void remove(int i);

	public boolean contains(int i);

	public int size();

	/**
	 * @param clear True if the set should be cleared on method return, false otherwise
	 * @return The set content, never null
	 */
	public Set<Integer> get(boolean clear);

	/**
	 * This method gets the set contents. If the set is empty this method will 
	 * wait until it becomes non-empty.
	 * 
	 * @param clear True if the set should be cleared on method return, false otherwise
	 * @return The set content, never null
	 * @throws InterruptedException
	 */
	public Set<Integer> getWait(boolean clear) throws InterruptedException;

}