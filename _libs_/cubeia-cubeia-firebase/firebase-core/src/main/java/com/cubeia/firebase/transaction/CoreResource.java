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
package com.cubeia.firebase.transaction;

/**
 * A resource is a transactional component wishing to 
 * participate in an ongoing transaction. For more information 
 * on transaction order and usage, please see the wiki.
 * 
 * <p>Resources are handled in FILO order by the transaction manager.
 * 
 * @author Larsan
 */
public interface CoreResource {

	/**
	 * Prepare the transaction for later commit. This implies the
	 * transaction is no longer recording. The state will be followed by 
	 * a "prepare" or a "commit" only. And resource not wishing the
	 * transaction to go through must thrown a resource exception.
	 * 
	 * @throws ResourceException On failures, or forced rollback
	 */
	public void prepare() throws ResourceException;
	
	
	/**
	 * Attempt to commit the underlying transaction. If any involved resource
	 * fails, other participants will still be committed. This state ends
	 * the transaction lifetime.
	 * 
	 * @throws ResourceException On failures
	 */
	public void commit() throws ResourceException;
	
	
	/**
	 * Attempt to rollback the underlying transaction. This state implies 
	 * another resource has failed during the transaction or in preparation, 
	 * and it ends the transaction lifetime.
	 */
	public void rollback();
	
}
