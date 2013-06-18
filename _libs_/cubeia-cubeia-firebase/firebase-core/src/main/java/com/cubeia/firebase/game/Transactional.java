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
package com.cubeia.firebase.game;

public interface Transactional {

	/**
	 * Begin transaction and obtain a suitable lock
	 * on the object. The method will return true if the lock
	 * was obtained.
	 * 
	 * @param timeout Timeout for optional waiting on lock
	 * @return True if the lock was obtain and the transaction started, false otherwise
	 * @throws InterruptedException
	 */
	public boolean begin(long timeout) throws InterruptedException;
	
	
	/**
	 * Flush changes down to underlying data object. This method
	 * must be called in order for changes to take effect.
	 */
	public void commit();
	
	
	/**
	 * Roll-back any non-transient changes. This should be used
	 * to unsure under-the-hood optimizations does not go through.
	 */
	public void rollback();

	
	/**
	 * Release the lock and the transaction. It is illegal to call 
	 * this method if the object is not locked.
	 */
	public void release();
	
}
