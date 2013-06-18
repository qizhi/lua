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

import java.util.Map;

import com.cubeia.firebase.api.service.transaction.EventTransactionContext;

public interface InternalEventTransactionContext extends EventTransactionContext {

	/**
	 * This method returns a mutable map of associated objects. These
	 * objects may contains entity managers, connections etc. All names
	 * starting with "_" is reserved by the transaction system.
	 * 
	 * @return A map of associated objects, never null
	 */
	public Map<String, Object> getAssociations();
	
}
