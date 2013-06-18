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
package com.cubeia.firebase.server.lobby.model;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.mtt.MTTState;

/**
 * Data object for tournament information. Holds some lobby 
 * information for an MTT.
 * 
 * @author Fredrik
 */
public class TournamentInfo {
	
	private String fqn;
	private int id = -1;
	private int mttLogicId = -1;
    
    private List<Parameter<?>> params = new ArrayList<Parameter<?>>();
    
    /**
     * This flag is set every time we map data to this bean.
     * However, it is only checked upon initial node discovery (FullSnapshot.initMttNode).
     *  
     */
    private boolean ready = false;
	
	/**
	 * Empty constructor
	 * 
	 */
	public TournamentInfo(){}
	
	/**
	 * Construct a TableInfo for a given table.
	 * 
	 * @param table
	 */
	public TournamentInfo(MTTState state) {
		id = state.getId();
		mttLogicId = state.getMttLogicId();
	}

	public String toString() {
		String info = "TournamentInfo - id["+id+"] Params:\n";
		for (Parameter<?> p : params) {
			info += "\t\t "+p.getKey()+" : "+p.getValue()+"\n";
		}
		
		return info;
	}
	
	
	public String getFqn() {
		return fqn;
	}

	public void setFqn(String fqn) {
		this.fqn = fqn;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public List<Parameter<?>> getParams() {
		return params;
	}
	
	public void addParam(Parameter<?> param) {
		params.add(param);
	}

	public int getMttLogicId() {
		return mttLogicId;
	}

	public void setMttLogicId(int mttLogicId) {
		this.mttLogicId = mttLogicId;
	}

    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public boolean isReady() {
        return ready;
    }
    
}
