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
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.lobby.model.TableInfo;

public class TableInfoBuilder {

	private static transient Logger log = Logger.getLogger(TableInfoBuilder.class);
	
	/**
	 * Create a TableInfo instance from a map of table parameters.
	 * 
	 * @param node
	 * @param data
	 * @return
	 */
	public static TableInfo createTableInfo(String path, Map<Object, Object> data) {
		TableInfo table = new TableInfo();
		table.setFqn(removeTrailingSlash(path));
		
		Arguments.notNull(data, "data");
		
		if(data.containsKey(DefaultTableAttributes._ID.toString())) table.setId((Integer)data.get(DefaultTableAttributes._ID.toString()));
		if(data.containsKey(DefaultTableAttributes._NAME.toString())) table.setName(data.get(DefaultTableAttributes._NAME.toString()).toString());
		if(data.containsKey(DefaultTableAttributes._CAPACITY.toString())) table.setCapacity((Integer)data.get(DefaultTableAttributes._CAPACITY.toString()));
		if(data.containsKey(DefaultTableAttributes._SEATED.toString())) table.setSeated((Integer)data.get(DefaultTableAttributes._SEATED.toString()));
		if(data.containsKey(DefaultTableAttributes._GAMEID.toString())) table.setGameId((Integer)data.get(DefaultTableAttributes._GAMEID.toString()));
		
		// Iterating the data.keySet gives concurrent modification exception even though
		// jboss cache specifies the data to be immutable.
		HashSet<Object> set = new HashSet<Object>(data.keySet());
		
		// Get all non-internal attributes as parameters
		for (Object oKey : set) {
			String key = String.valueOf(oKey);
			
			if (key.startsWith("_")) continue; // Ignore internal
			
			Object value = data.get(key);
			
			if (value instanceof Integer) {
				Integer iValue = (Integer) value;
				Parameter<Integer> param = new Parameter<Integer>(key, iValue, Parameter.Type.INT);
				table.addParam(param);
				
			} else if (value instanceof String) {
				String sValue = String.valueOf(value);
				Parameter<String> param = new Parameter<String>(key, sValue, Parameter.Type.STRING);
				table.addParam(param);
			} else if (value instanceof Date) {
				Parameter<Date> param = new Parameter<Date>(key, (Date)value, Parameter.Type.DATE);
				table.addParam(param);
			} else {
				log.warn("Unknown table info parameter type: ["+key+" : "+data.get(key)+"]");
			}
			
		}
		return table;
	}
	
	
	private static String removeTrailingSlash(String fqn) {
		if (fqn.endsWith("/")) {
			return fqn.substring(0, fqn.lastIndexOf("/"));
		} else {
			return fqn;
		}
	}
}
