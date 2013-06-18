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
package com.cubeia.firebase.server.routing;

import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

public interface SystemRouter extends ServiceStackRouter, Haltable {

	/**
	 * @return Get router id, never null
	 */
	public String getId();
	
	/**
	 * @return Get router name, never null
	 */
	public String getName();
	
	
	
	// --- GLOBAL SYSTEM ACCESS --- //
	
	/**
	 * @return The sender for the global client node topic, never null
	 */
	public WrappingSender<ClientEvent<?>> getClientEventSender();
	
	/**
	 * @return The sender for the global mtt node topic, never null
	 */
	public WrappingSender<MttEvent> getMttSender();

	/**
	 * @param gameId The activator game id
	 * @return The system sender for game activator actions, never null
	 */
	public Sender<ActivatorAction<?>> getGameActivatorSender(int gameId); 

	/**
	 * @param mttId The activator mtt id
	 * @return The system sender for tournament activator actions, never null
	 */
	public Sender<ActivatorAction<?>> getMttActivatorSender(int mttId); 

}
