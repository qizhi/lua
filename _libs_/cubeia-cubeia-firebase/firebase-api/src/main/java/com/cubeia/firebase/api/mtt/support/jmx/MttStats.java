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
package com.cubeia.firebase.api.mtt.support.jmx;

public class MttStats implements MttStatsMBean {
	
	private final int mttInstanceId;
	
	private int capacity = 0;
	private int registeredPlayers = 0;
	private int tableCount = 0;
	private int playingPlayers = 0;

	private final String name;

	private final int mttId;

	private int remainingPlayers;

	public MttStats(int mttId, int mttInstanceId, String name) {
		this.mttId = mttId;
		this.mttInstanceId = mttInstanceId;
		this.name = name;
	}
	
	public int getMttLogicId() {
		return mttId;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getRegisteredPlayers() {
		return registeredPlayers;
	}
	
	public void setRegisteredPlayers(int registeredPlayers) {
		this.registeredPlayers = registeredPlayers;
	}

	public int getTableCount() {
		return tableCount;
	}
	
	public void setTableCount(int tableCount) {
		this.tableCount = tableCount;
	}
	
	public int getMttInstanceId() {
		return mttInstanceId;
	}

	public int getPlayingPlayers() {
		return playingPlayers;
	}

	public void setPlayingPlayers(int playingPlayers) {
		this.playingPlayers = playingPlayers;
	}

	public void setRemainingPlayers(int remainingPlayers) {
		this.remainingPlayers = remainingPlayers;
	}
	
	public int getRemainingPlayers() {
		return remainingPlayers;
	}
}
