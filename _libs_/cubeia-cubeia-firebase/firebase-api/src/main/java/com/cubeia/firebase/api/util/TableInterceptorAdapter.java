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
package com.cubeia.firebase.api.util;

import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;

/**
 * This is an adapter class for a table interceptor.
 * 
 * @author Lars J. Nilsson
 */
public class TableInterceptorAdapter implements TableInterceptor {

	@Override
	public InterceptionResponse allowJoin(Table table, SeatRequest request) {
		return null;
	}

	@Override
	public InterceptionResponse allowReservation(Table table, SeatRequest request) {
		return null;
	}

	@Override
	public InterceptionResponse allowLeave(Table table, int playerId) {
		return null;
	}
}
