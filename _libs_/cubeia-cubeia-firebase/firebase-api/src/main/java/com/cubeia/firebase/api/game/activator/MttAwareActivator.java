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
package com.cubeia.firebase.api.game.activator;

import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.support.MTTSupport;

/**
 * This interface should be implemented by activators which
 * are aware of MTT code and wants to be notified on shared events
 * such as table creation.
 * 
 * @author Larsan
 */
public interface MttAwareActivator { 
	
	/**
	 * This method is called when an mtt table has been created. It gives the
	 * activator a chance to (1) set initial table data; and (2) modify the
	 * table attributes as they appear in the lobby.
	 * 
	 * @param table Table to set initial attributes for, never null
	 * @param mttId the mtt instance id
	 * @param commandAttachment Optional attachment for the command sent by 
	 *   {@link MTTSupport#createTables(com.cubeia.firebase.api.mtt.support.MTTStateSupport, int, String, Object)}, may be null
	 * @param acc Attribute accessor, never null
	 */
	public void mttTableCreated(Table table, int mttId, Object commandAttachment, LobbyAttributeAccessor acc);

}
