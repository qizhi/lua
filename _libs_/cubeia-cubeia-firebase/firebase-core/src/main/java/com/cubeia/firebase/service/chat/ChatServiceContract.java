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
package com.cubeia.firebase.service.chat;

import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.event.ChatEventDaemonLoopback;

/**
 * This service handles and relays chat messages. It manages as set of
 * chat "channels" similarly to that of an IRC server. Each channel is
 * added on demand as the first client registers to it, and will be 
 * destroyed when the last player is removed from it. 
 * 
 * <p>Messages handled by this service will propagate via chat channels
 * which are separate from any table chat events. Hence, chat actions will 
 * not be visible at tables. 
 * 
 * <p>Filtering and registering of chat messages can be done using a plugin
 * service. If there is an implementation of {@link ChatFilter} deployed this will 
 * be picked up and used. 
 * 
 * @author Fredrik
 */
public interface ChatServiceContract extends Contract {
	
	/*
	 * The Chat handler needs a router consumer to dispatch
	 * client chat actions.
	 * 
	 * @param clientRouter
	 */
	// public void setClientRouter(Router clientRouter);
	
	/**
	 * Register a player to the channel with the 
	 * given channel id. If the channel does not exist, it
	 * will be created.
	 * 
	 * @param player Player to add, must not be null
	 * @param channelid Channel ID
	 */
	public void addPlayer(Client player, int channelid);
	
	/**
	 * Remove a player from the channel with the given 
	 * channel id. If the channel is empty after the removal
	 * it will be destroyed. If the channel is not found this
	 * method fails silently.
	 * 
	 * @param player Player to remove, must not be null
	 * @param channelid Channel ID
	 */
	public void removePlayer(Client player, int channelid);
	
	/**
	 * Handle a channel chat action. The message will be propagated to
	 * all registered clients for the channel, if it is a broadcast. Private
	 * chat messages will be sent to single clients only. The sender does not
	 * need to be registered with the channel.
	 * 
	 * @param chat Message to send, must not be null
	 */
	public void handle(ChannelChatAction chat);

	/**
	 * Setting an event loop back on the service. This is an interim
	 * method until the router is re-written.
	 * 
	 * @param daemon Loop-back, must not be null
	 */
	public void setLocalLoopback(ChatEventDaemonLoopback loopback);

	
}
