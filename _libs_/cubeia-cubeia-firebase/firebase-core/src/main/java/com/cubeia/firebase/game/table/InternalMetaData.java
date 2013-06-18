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

import static com.cubeia.firebase.api.game.table.TableType.NORMAL;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.ActionId;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.lobby.LobbyPath;

public final class InternalMetaData implements TableMetaData, Serializable {

    private static final long serialVersionUID = -6418166881964198706L;

    private LobbyPath lobbyPath = null;
    private String gameClass = null;
    private int gameId, revisionId = -1;
    private int tableId = -1;
    private int mttId = -1;
    private String name = "n/a";
    private TableType type = NORMAL;
    
    private Map<String, ActionId> lastExecuted = new TreeMap<String, ActionId>();
    
    @Override
    public String toString() {
    	return gameClass + " (" + gameId + "); tableId: " + tableId + "; mttId: " + mttId;
    }
    
    public void setLobbyPath(LobbyPath lobbyPath) {
		this.lobbyPath = lobbyPath;
	}
	
    public LobbyPath getLobbyPath() {
		return lobbyPath;
	}
    
    public String getGameClass() {
		return gameClass;
	}
	
	public void setGameClass(String gameClass) {
		this.gameClass = gameClass;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getRevisionId() {
		return revisionId;
	}
	
	public void setRevisionId(int revisionId) {
		this.revisionId = revisionId;
	}
	
	public int getTableId() {
		return tableId;
	}
	
	public void setTableId(int tableId) {
		this.tableId = tableId;
	} 
	
	public TableType getType() {
	    return type;
	}
	
	public void setType(TableType type) {
        this.type = type;
    }
	
	public int getMttId() {
        return mttId;
    }
	
	public void setMttId(int mttId) {
        this.mttId = mttId;
    }
	
    public long getLastExecuted(String serverId) {
    	if(lastExecuted.containsKey(serverId)) {
        	return lastExecuted.get(serverId).getSequence();
        } else {
        	return -1;
        }
    }
    
    public boolean isDoubleExecution(Action action) {
    	ActionId id = action.getActionId();
    	if(id != null && lastExecuted.containsKey(id.getServerId())) {
    		return lastExecuted.get(id.getServerId()).equals(id);
    	} else {
    		return false;
    	}
    }

    public void setLastExecuted(Action action) {
        ActionId id = action.getActionId();
        if(id != null) {
        	lastExecuted.put(id.getServerId(), id);
        }
    }
}
