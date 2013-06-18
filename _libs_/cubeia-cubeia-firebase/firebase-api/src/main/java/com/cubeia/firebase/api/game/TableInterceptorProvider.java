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
package com.cubeia.firebase.api.game;

import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;

/**
 * Implement this interface in your Game implementation
 * if you wish to provide a TableInterceptor. If you do
 * not which to use a separate object, your game may implement
 * TableInterceptor directly.
 *
 * @author Fredrik
 */
public interface TableInterceptorProvider {

	/**
	 * Get a TableInterceptor for the Table.
	 * 
	 * @return
	 */
	public TableInterceptor getTableInterceptor(Table table);
	
}
