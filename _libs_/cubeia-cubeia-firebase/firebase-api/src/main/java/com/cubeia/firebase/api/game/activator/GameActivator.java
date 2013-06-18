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

import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.Startable;

/**
 * The game activator is responsible for table creation and maintenance in the
 * Firebase cluster. It is maintained by the cluster as a singleton, ie. only one
 * instance exists across the entire cluster.
 * 
 * <p>A game activator should check the table lobby when initiated to check if it
 * must create any initial tables from configuration or database.
 * 
 * <p>An activator may between its start and stop methods regularly check and update
 * the number of tables in the system.
 * 
 * <p>Should the node in the cluster keeping the activator be brought down, or crash, a
 * new activator will be created, initialized and started on another node.
 * 
 * <p>A simple run-down of what an activator should do, may look like this:
 * <ol>
 * 	<li>On init, check if any tables exists, using the table factory from the 
 * 		context. If no tables exists this is during startup, so schedule some tables
 * 		to be created.</li>
 *  <li>On start, schedule a task which regularly checks if the number of tables in the 
 *  	system should be changed.</li>
 *  <li>On stop, stop all scheduled tasks.</li>
 *  <li>On destroy, cleanup resources.</li>
 * </ol>
 * 
 * <b>NB: </b>If your game is participating in tournaments, please make sure
 * your activator also implements the {@link MttAwareActivator} interface. Also, 
 * if it is possible for game clients to request tables to be created (eg "private
 * tables"), please make sure the activator implements {@link RequestAwareActivator}
 * as well. Finally, if your activator should be eligible to receive actions, please
 * implement {@link RoutableActivator}.
 * 
 * @author lars.j.nilsson
 */
public interface GameActivator extends Initializable<ActivatorContext>, Startable { }
