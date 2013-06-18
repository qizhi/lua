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
 * A chain of rules. This object is used by the DOC protector
 * to call a number of rules as one method call.
 * 
 * @author Larsan
 */
public interface RuleChain {

	/**
	 * This method should be called when the chain is invoked. It
	 * will iterate all available rules.
	 * 
	 * @param callerId Caller to check, never null
	 * @return True if all rules allowed the access, false otherwise
	 */
	public boolean next(Object callerId);
	
}
