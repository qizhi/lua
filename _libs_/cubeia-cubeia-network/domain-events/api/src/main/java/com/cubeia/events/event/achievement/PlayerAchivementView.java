/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.events.event.achievement;


/**
 * Data object for an achievable player achievement.
 * 
 * This is not experience etc.
 * 
 * @author fredrik
 */
public class PlayerAchivementView {
	
	public String achievementNameId;
	public String name;
	public String description;
	public String playerId;
	public Boolean achieved = false;
	public String achievedTimestamp;
	public String imageUrl;
	
	public Long targetCount;
	public Long currentCount;
	
	public Long rank;
	public Long value;
	public String rewardDescription;
	
	public PlayerAchivementView() {}
	
	@Override
	public String toString() {
		return "PlayerAchivementView id["+achievementNameId+"] name["+name+"] playerId["+playerId+"] achieved["+achieved+"] targetCount["+targetCount+"] currentCount["+currentCount+"]";
	}
	
}
