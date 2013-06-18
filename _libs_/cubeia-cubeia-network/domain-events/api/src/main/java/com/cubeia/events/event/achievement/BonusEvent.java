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

import java.util.HashMap;
import java.util.Map;

// @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
public class BonusEvent {

	public String game;
	public String player;
	public String type;
	public String name;
	public boolean broadcast = false;
	
	public PlayerAchivementView achievement;
	
	public Map<String, String> attributes = new HashMap<String, String>();

    public BonusEvent(){    }

    
    @Override
    public String toString() {
    	return "BonusEvent, player["+player+"] game["+game+"] type["+type+"] name["+name+"] attributes["+attributes+"] achievement["+achievement+"]";
    }
}