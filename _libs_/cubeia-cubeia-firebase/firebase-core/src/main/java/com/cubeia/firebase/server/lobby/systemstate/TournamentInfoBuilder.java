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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;

public class TournamentInfoBuilder {

	private static transient Logger log = Logger.getLogger(TournamentInfoBuilder.class);
	
	/**
	 * Create a TableInfo instance from a map of table parameters.
	 * 
	 * @param node
	 * @param data
	 * @return
	 */
	public static TournamentInfo createTableInfo(String path, Map<Object, Object> data) {
		TournamentInfo tournament = new TournamentInfo();
		tournament.setFqn(removeTrailingSlash(path));
		
		Arguments.notNull(data, "data");
		
		if(data.containsKey(DefaultMttAttributes._ID.toString())) tournament.setId((Integer)data.get(DefaultMttAttributes._ID.toString()));
		if(data.containsKey(DefaultMttAttributes._READY.toString())) tournament.setReady(true);
		
		// Get all non-internal attributes as parameters
		for (Object oKey : data.keySet()) {
			String key = String.valueOf(oKey);
			
			if (key.startsWith("_")) continue; // Ignore internal
			
			Object value = data.get(key);
			
			if (value instanceof Integer) {
				Integer iValue = (Integer) value;
				Parameter<Integer> param = new Parameter<Integer>(key, iValue, Parameter.Type.INT);
				tournament.addParam(param);
				
			} else if (value instanceof String) {
				String sValue = String.valueOf(value);
				Parameter<String> param = new Parameter<String>(key, sValue, Parameter.Type.STRING);
				tournament.addParam(param);
				
			} else if (value instanceof Date) {
				Parameter<Date> param = new Parameter<Date>(key, (Date)value, Parameter.Type.DATE);
				tournament.addParam(param);
			} else {
				log.warn("Unknown table info parameter type: ["+key+" : "+data.get(key)+"]");
			}
			
		}
		return tournament;
	}


	/*private static Integer getIntAttribute(DefaultMttAttributes attribute, Map<Object, Object> data) {
		return (Integer)data.get(attribute.toString());
	}*/
	
	
	private static String removeTrailingSlash(String fqn) {
		if (fqn.endsWith("/")) {
			return fqn.substring(0, fqn.lastIndexOf("/"));
		} else {
			return fqn;
		}
	}
}
