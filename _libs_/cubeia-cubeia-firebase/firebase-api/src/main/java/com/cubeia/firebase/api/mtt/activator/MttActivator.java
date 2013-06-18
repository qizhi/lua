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
package com.cubeia.firebase.api.mtt.activator;

import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.mtt.MttFactory;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.Startable;

/**
 * The game activator is responsible for table creation and maintenance in the
 * Firebase cluster. It is maintained by the cluster as a singleton, ie. only one
 * instance exists across the entire cluster.
 * 
 * <p>Please compare to {@link GameActivator}.
 * 
 * <p>If your activator should be eligible to receive actions, please
 * implement {@link RoutableActivator}.
 */
public interface MttActivator extends Initializable<ActivatorContext>, Startable {
	
	/**
	 * The MTT Factory will be injected before init().
	 * 
	 * @param factory
	 */
	public void setMttFactory(MttFactory factory);
	
}
