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
package com.cubeia.firebase.service.wlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.service.wlist.linear.SimpleFilteredRequest;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

public class WaitingListGenerator {
	
	public static LobbyPath path = new LobbyPath(99, "");
	public static LobbyPath pathA = new LobbyPath(99, "a");
	public static LobbyPath pathB = new LobbyPath(99, "b");
	
	/**
	 * 
	 * @param size, total number of actions
	 * @param cycle, start over with p2 at this value
	 * @return
	 */
	public static List<FilteredJoinAction> createActions(int gid, String fqn, int size, int cycle) {
		List<FilteredJoinAction> actions = new ArrayList<FilteredJoinAction>(size);
		int p2 = cycle;
	
		for (int i = 1; i <= size; i++) {
			if (p2 == 0) p2 = cycle;
			actions.add(createAction(i, gid, fqn, "apa", p2));
			p2--;
		}
		
		return actions;
	}
	
	/**
	 * 
	 * @param size, total number of actions
	 * @param cycle, start over with p2 at this value
	 * @return
	 */
	public static List<FilteredRequest> createRequests(int gid, LobbyPath path, int size, int cycle) {
		List<FilteredRequest> requests = new ArrayList<FilteredRequest>();
		int p2 = cycle;
	
		for (int i = 1; i <= size; i++) {
			if (p2 == 0) p2 = cycle;
			requests.add(createRequest(i, gid, path.getDomain(), "apa", p2));
			p2--;
		}
		
		return requests;
	}
	
	
	/**
	 * We will create a miss every second data using p1
	 * 
	 * @param size
	 * @param cycle
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<Map> createDataList(int size, int cycle) {
		String p1 = "apa";
		List<Map> result = new ArrayList<Map>();
		int p2 = 1;
		cycle++;
		for (int i = 1; i <= size; i++) {
			if ((p2 % cycle) == 0) p2 = 1;
			if (i % 2 == 0) {
				p1 = "banan"; // miss
				
			} else {
				p1 = "apa"; // hit
				p2++;
			}
			
			result.add(createData(p1, p2));
			
		}
		return result;
	}
	
	
	public static FilteredJoinAction createAction(int pid, int gid, String fqn, String p1, int p2) {
		List<Parameter<?>> params = WaitingListGenerator.createParams(p1, p2);
		FilteredJoinAction action = new FilteredJoinAction(pid, gid);
		action.setParameters(params);
		action.setAddress(fqn);
		return action;
	}
	
	public static FilteredJoinAction createAction(int pid, int gid, String fqn, List<Parameter<?>> params) {
		FilteredJoinAction action = new FilteredJoinAction(pid, gid);
		action.setParameters(params);
		action.setAddress(fqn);
		return action;
	}
	
	public static FilteredRequest createRequest(int pid, int gid, String p1, int p2) {
		return createRequest(pid, gid, "", p1, p2);
	}
	
	public static FilteredRequest createRequest(int pid, int gid, String fqn, String p1, int p2) {
		List<Parameter<?>> params = WaitingListGenerator.createParams(p1, p2);
		LobbyPath path = new LobbyPath(gid, fqn);
		return new SimpleFilteredRequest(pid, path, params);
	}
	
	public static FilteredRequest createRequest(FilteredJoinAction action) {
		LobbyPath path = new LobbyPath(action.getGameId(), action.getAddress());
		return new SimpleFilteredRequest(action.getPlayerId(), path, action.getParameters());
	}
	
	public static List<Parameter<?>> createParams(String p1, int p2) {
		// Create a request with a String = String constraint only
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();
		Parameter<String> param1 = new Parameter<String>("p1", p1, Parameter.Type.STRING, Parameter.Operator.EQUALS);
		Parameter<Integer> param2 = new Parameter<Integer>("p2", p2, Parameter.Type.INT, Parameter.Operator.GREATER_THAN);
		params.add(param1);
		params.add(param2);
		return params;
	}
	
	
	public static Map<String, Object> createData(String p1, int p2) {
		// Create table attributes
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("p1", p1);
		data.put("p2", new Integer(p2));
		return data;
	}

	public static List<Parameter<?>> createParams(String string, int i, Date date) {
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();
		Parameter<String> param1 = new Parameter<String>("p1", string, Parameter.Type.STRING, Parameter.Operator.EQUALS);
		Parameter<Integer> param2 = new Parameter<Integer>("p2", i, Parameter.Type.INT, Parameter.Operator.GREATER_THAN);
		Parameter<Date> param3 = new Parameter<Date>("p3", date, Parameter.Type.DATE, Parameter.Operator.GREATER_THAN);
		params.add(param1);
		params.add(param2);
		params.add(param3);
		return params;
	}

	public static Map<String, Object> createData(String string, int i, Date date) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("p1", string);
		data.put("p2", new Integer(i));
		data.put("p3", date);
		return data;
	}


	
	
	
}
