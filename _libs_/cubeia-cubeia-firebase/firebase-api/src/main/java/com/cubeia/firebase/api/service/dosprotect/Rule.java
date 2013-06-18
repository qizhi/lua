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
package com.cubeia.firebase.api.service.dosprotect;

/**
 * Rules are stateful objects which determine if access is
 * allowed for callers according to their internal logic.
 * 
 * <p>For house keeping purposes, the {@link #cleanup()} method
 * will be called by the service regularly. 
 * 
 * @author Larsan
 */
public interface Rule {

	/**
	 * Check if a caller is allowed access. This method should
	 * either return false if the caller is not allowed, or return 
	 * the result of invoking the "next" method on the rule chain 
	 * which will check the next rule.
	 * 
	 * @param callerId Caller id, never null
	 * @param chain Chain to pass on to if the access is granted, never null
	 * @return The return value of the chains "next" method, or false if the request is denied
	 */
	public boolean allow(Object callerId, RuleChain chain);
	
	
	/**
	 * This method is called regularly by the service. The rule should
	 * use this call to cleanup resources.
	 */
	public void cleanup();
	
}
