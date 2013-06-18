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
package com.cubeia.firebase.api.mtt.model;

import com.cubeia.firebase.api.game.player.Player;

/**
 * This class will not be serialized directly but rather by the tournament 
 * player registry to save bandwidth.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public final class MttPlayer implements Player {

	/** Version ID */
	private static final long serialVersionUID = 1L;
	private int id;
	private String screenname;
	
	/** Player's current tournament status */
	private MttPlayerStatus status;
	
	/** Players position in the tournament */
	private int position = -1;
	
	public MttPlayer(int id) {
		this(id, "n/a");
	}
	
	public MttPlayer(int id, String screenname) {
		this.id = id;
		this.screenname = screenname;
	}
	
	public int getPlayerId() {
		return id;
	}

	public String getScreenname() {
		return screenname;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public MttPlayerStatus getStatus() {
		return status;
	}

	public void setStatus(MttPlayerStatus status) {
		this.status = status;
	}
	
	public String toString() {
		return "id["+id+"] nick["+screenname+"] status["+status+"]";
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MttPlayer other = (MttPlayer) obj;
        if (id != other.id)
            return false;
        return true;
    }
    
}
