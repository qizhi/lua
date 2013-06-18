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
package com.cubeia.firebase.server.game;

import com.cubeia.firebase.server.node.BaseNodeMBean;

/**
 * This MBean represent a game node mounted on a server. It
 * contains methods for forcibly halting and resuming the node, 
 * these should only be used as a last resort.
 *  
 * @author Larsan
 */
public interface GameNodeMBean extends BaseNodeMBean { 
	
	/**
	 * Check if the game node is halted or not. 
	 * 
	 * @return True if the server is halted, false if no
	 */
	public boolean isHalted();
	
	/**
	 * Forcibly resume the game node. This should only be used
	 * as a last resort if the system is in an inconsistent state.
	 */
	public void resume();
	
	/**
	 * Forcibly halt the game node. This should only be used
	 * as a last resort if the system is in an inconsistent state.
	 */
	public void halt();
	
}
