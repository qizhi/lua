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
package com.cubeia.firebase.api.action;

/**
 * This marker interface can be used for system actions and 
 * other actions that can be handled transient by the system. If a
 * transient action arrives at a table, it will silently ignore 
 * hard failures.
 * 
 * <p>Eg. player status checks are transient as it doesn't matter if the
 * table the action is bound for exists or not. If the table has been 
 * removed the action is not necessary and may be silently dropped.
 * 
 * @author Larsan
 */
public interface TransientGameAction extends GameAction { }
