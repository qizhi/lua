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
package com.cubeia.firebase.api.action.local;



/**
 * Interface for actions that are local to the client gateway node.
 * I.e. they do not propagate over the cluster to any game 
 * implementation / services.
 * 
 * They would however be allowed to propagate over other event-channels
 * as needed. There is currently no visitor defined for these type of 
 * actions. Evaluate implementing this if the list grows.
 *  
 * 
 * @author Fredrik
 *
 */
public interface LocalAction extends VisitableLocalAction {
	
}
