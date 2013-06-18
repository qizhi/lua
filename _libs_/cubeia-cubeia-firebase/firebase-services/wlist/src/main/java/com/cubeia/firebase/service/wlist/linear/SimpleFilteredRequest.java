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
package com.cubeia.firebase.service.wlist.linear;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.defined.Parameter.Operator;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

/**
 * Holds all constraints for a filtered join request.
 *
 * @author Fredrik
 */
public class SimpleFilteredRequest implements FilteredRequest {
	
	private final long timeStamp = System.currentTimeMillis();
	
	/**
	 * Holds all parameters. This map is not thread safe. It is also fail-fast when iterating.
	 * However, the nodes will use a synch lock so we shouldn't be accessing this map
	 * concurrently. If concurrent access is needed, substitute to a ConcurrentMap as commented
	 * out below.
	 * 
	 * Using the non-synchronized HashMap implementation makes a match roughly twice as fast.
	 * 
	 */
	private Map<String, Parameter<?>> params = new HashMap<String, Parameter<?>>();
	//private Map<String, Parameter> params = new ConcurrentHashMap<String, Parameter>();
	
	private final int playerId;
	
	private final LobbyPath fqn;
	
	/** This id is used for cancellation and general identification of the request */
	private long id = -1;
	
	/**
	 * Direct loopback to the client.
	 * TODO: This is not the preferred way of communicating.
	 * using the loopback will result in a synchronous blocking call to send
	 * packet(s) to the client.
	 * 
	 * However, we are using the waitinglist locally, so we definately want 
	 * to avoid going to the router and broadcast the table available to 
	 * all gateway nodes, that would be a waste of internal bandwidth.
	 */
	private LocalActionHandler loopback;
	
	public SimpleFilteredRequest(int playerId, LobbyPath fqn, List<Parameter<?>> requestParams) {
		this.playerId = playerId;
		this.fqn = fqn;
		for (Parameter<?> p : requestParams) {
			params.put(p.getKey(), p);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.wlist.model.FilteredRequest#preMatch(java.lang.String, java.lang.Object)
	 */
	public boolean preMatch(String attribute, Object value) {
		return matchAttribute(attribute, value);
	}


	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.wlist.model.FilteredRequest#match(java.util.Map)
	 */
	public boolean match(Map<?, ?> data) {
		boolean match = true;
		
		// OK... lets iterate all attributes and see if we have a match =/
		// I will iterate the parameters since I think they would contain less keys
		for (String key : params.keySet()) {
			// If the data doesn't hold the key, then it is not a match
			Object value = data.get(key);
			if (value == null) {
				match = false;
				break;
			}
			
			// We have a value, evaluate
			match &= matchAttribute(key, value);
			
		}
		
		return match;
	}
	
	
	public int compareTo(FilteredRequest o) {
		if (timeStamp != o.getTimeStamp()) {
			return (int)(timeStamp - o.getTimeStamp());
		} else {
			// We have the exact same time, it doesnt matter. Use hashcode
			return hashCode()-o.hashCode();
		}
	}

	
	public String toString() {
		String val =  "pid["+playerId+"] fqn["+fqn+"] time["+timeStamp+"] ->";
		for (Parameter<?> p : params.values()) {
			val += "\""+p.getKey()+" "+p.getOperator()+" "+p.getValue()+"\" ";
		}
		return val;
	}
	
	public int getPlayerId() {
		return playerId;
	}

	
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public Map<String, Parameter<?>> getParameters() {
		return params;
	}

	public LobbyPath getFqn() {
		return fqn;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean matchAttribute(String attribute, Object value) {
		boolean match = false;
		
		Parameter p = params.get(attribute);
		if (p != null) {
			// We have a constraint for this attribute. Evaluate it
			if (p.getType() == Parameter.Type.STRING) {
				match = evalString(p, value);
				
			} else if (p.getType() == Parameter.Type.INT) {
				match = evalInt(p, value);
				
			} else if (p.getType() == Parameter.Type.DATE) {
				match = evalDate(p, value);
				
			} 
		}
		
		return match;
	}
	


	/**
	 * Evaluate String attribute.
	 * Only EQUALS is allowed here.
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	private boolean evalString(Parameter<String> p, Object value) {
		if (p.getOperator() == Parameter.Operator.EQUALS) {
			return p.getValue().equals(value);
		} else {
			return false;
		}
	}
	
	private boolean evalDate(Parameter<Date> p, Object value) {
		if(value instanceof Date) {
			int c = (int)(p.getValue().getTime() / 1000);
			int v = (int)(((Date)value).getTime() / 1000);
			return matchInts(p.getOperator(), c, v);
		} else {
			return false;
		}
	}
	
	
	/**
	 * Evaluate integer attribute.
	 * All operators allowed.
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	private boolean evalInt(Parameter<Integer> p, Object value) {
		if (value instanceof Integer) {
			int c = p.getValue();
			int v = ((Integer) value).intValue();
			return matchInts(p.getOperator(), c, v);
		} else {
			return false;
		}
	}

	private boolean matchInts(Operator o, int c, int v) {
		boolean match = false;
		
		switch (o) {

		case EQUALS :
			match = v == c;
			break;
			
		case GREATER_THAN :
			match = v > c;
			break;
			
		case SMALLER_THAN :
			match = v < c;
			break;
			
		case EQUALS_OR_GREATER_THAN :
			match = v >= c;
			break;
			
		case EQUALS_OR_SMALLER_THAN :
			match = v <= c;
			break;

		}
		return match;
	}

	public LocalActionHandler getLoopback() {
		return loopback;
	}

	public void setLoopback(LocalActionHandler loopback) {
		this.loopback = loopback;
	}

	
	
	
}
