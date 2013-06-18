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
package com.cubeia.firebase.mock;

import java.util.List;

import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;

public class MockTableInterceptor implements TableInterceptor {

	private final boolean allowJoin;
	private final boolean allowLeave;

	public MockTableInterceptor(boolean allowJoin, boolean allowLeave) {
		this.allowJoin = allowJoin;
		this.allowLeave = allowLeave;
	}
	
	public InterceptionResponse allowJoin(Table table, int seat, int playerId, List<Attribute> parameters) {
		return new InterceptionResponse(allowJoin, -1);
	}

	public InterceptionResponse allowLeave(Table table, int playerId) {
		return new InterceptionResponse(allowLeave, -1);
	}

	public InterceptionResponse allowJoin(Table table, SeatRequest request) {
		return new InterceptionResponse(allowJoin, -1);
	}

	public InterceptionResponse allowReservation(Table table, SeatRequest request) {
		return new InterceptionResponse(allowJoin, -1);
	}

}
