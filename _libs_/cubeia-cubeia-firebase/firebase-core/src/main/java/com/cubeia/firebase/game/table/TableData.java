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
package com.cubeia.firebase.game.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Seat;

/**
 * This interface represents the raw table data saved and distributed across
 * a Firebase cluster. It contains raw, unsynchronized, collection for saving
 * relevant data.
 * 
 * @author Larsan
 */
public interface TableData extends Identifiable {

	public InternalMetaData getMetaData();

	public void setMetaData(InternalMetaData metaData);

	public Map<Integer, GenericPlayer> getPlayers();

	public byte[] getScheduledActions();

	public List<Seat<GenericPlayer>> getSeats();

	public byte[] getStateData();

	public void setStateData(byte[] stateData);

	public Set<Integer> getWatchingPlayers();

}