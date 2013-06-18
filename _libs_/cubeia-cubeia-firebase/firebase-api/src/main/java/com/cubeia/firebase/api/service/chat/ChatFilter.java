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
package com.cubeia.firebase.api.service.chat;

import com.cubeia.firebase.api.action.TableChatAction;
import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.service.Contract;

/**
 * This service contract should be implemented by filtering software. If
 * a chat filter is deployed it will be used by the chat modules on each node
 * in a cluster. This is a so-called plugin service which will only be used if
 * deployed. 
 * 
 * <p>Implementors should note that the {@link #channelCreated(int, int)} and
 * {@link #channelDestroyed(int)} methods are not guaranteed to be atomic across
 * multiple virtual machines. In effect, it is possible to get multiple 
 * creation/destruction events for any given channel. This should be fairly
 * uncommon though.
 * 
 * @author Larsan
 */
public interface ChatFilter extends Contract {

	/**
	 * A channel has been registered on a particular node by a connection
	 * registered with a particular user. This method is not atomic across
	 * multiple virtual machines. It is possible, however unlikely, that this
	 * method may be called multiple times but on different nodes, under high 
	 * concurrency situations.
	 * 
	 * @param playerId Player that requested the channel, or -1 if not known
	 * @param channelId The new channel id
	 */
    public void channelCreated(int playerId, int channelId);
    
    
    /**
     * This method can be used to filter or modify a given chat action. It can
     * also completely replace a given action. The chat channel will use the 
     * return of this method for further processing. If the action should be
     * dropped, this method should return null.
     * 
     * @param action The action to filter, never null
     * @return The action to continue with, or null to drop the action
     */
    public ChannelChatAction filter(ChannelChatAction action);
    
    
    /**
     * This method can be used to filter or modify a given table chat action. It can
     * also completely replace a given action. The table chat will use the 
     * return of this method for further processing. If the action should be
     * dropped, this method should return null.
     * 
     * @param action The action to filter, never null
     * @return The action to continue with, or null to drop the action
    */
    public TableChatAction filter(TableChatAction action);


    /**
     * This method will be invoked when a channel is dropped on a particular
     * node. This method is not atomic across multiple virtual machines. It is 
     * possible, however unlikely, that this method may be called multiple 
     * times but on different nodes, under high concurrency situations.
	 * 
     * @param channelId The dropped channel id
     */
    public void channelDestroyed(int channelId);
    
}
