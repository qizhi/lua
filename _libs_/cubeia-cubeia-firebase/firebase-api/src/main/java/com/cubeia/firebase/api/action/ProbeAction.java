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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;
import com.cubeia.firebase.api.common.Stamp;
import com.cubeia.firebase.api.util.Arguments;

public class ProbeAction extends AbstractPlayerAction {
	
	private static final long serialVersionUID = 3863893797139217233L;

	private int id; 
	
	private List<Stamp> stamps = new LinkedList<Stamp>();
	
	public ProbeAction(int playerId, int tableId, int id) {
		super(playerId, tableId);
		this.id = id;
	}

    public void addTimestamp(Class<?> cl) {
    	Arguments.notNull(cl, "class");
    	Stamp st = new Stamp(cl, System.currentTimeMillis());
    	stamps.add(st);
    }
    
    public void addTimestamp(String stamp) {
    	Arguments.notNull(stamp, "stamp");
    	Stamp st = new Stamp(stamp, System.currentTimeMillis());
    	stamps.add(st);
    }
    
    public Collection<Stamp> getTimestamps() {
    	return stamps;
    }
	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}	

	public String toString() {
		return "ProbeAction: playerId: " + getPlayerId() + " id: " + id;
	}


    public int getId() {
        return id;
    }
}
