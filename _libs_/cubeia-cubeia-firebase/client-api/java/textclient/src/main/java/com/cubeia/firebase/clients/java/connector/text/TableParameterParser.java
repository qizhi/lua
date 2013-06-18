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
package com.cubeia.firebase.clients.java.connector.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;

public class TableParameterParser {
	/**
     * Format
     * 
     * key=value
     * key>value
     * key<value
     * key>=value
     * key<=value
     * 
     * 
     * @param params
     * @return
     */
    public static List<ParamFilter> getFilteredParams(String params) throws Exception {
    	List<ParamFilter> result = new ArrayList<ParamFilter>();
    	
    	Pattern pattern = Pattern.compile("(\\p{Punct}?\\p{Alnum}++)(\\p{Punct}++)(\\S++)");
		Matcher matcher = pattern.matcher(params);
		
		while (matcher.find()) {
    		String key = matcher.group(1);
    		String op = matcher.group(2);
    		String value = matcher.group(3);
    		
    		Param identifier = new Param();
    		identifier.key = key;
    		identifier.type = getTypeCode(value);
    		identifier.value = getValueData(value);
    		
    		ParamFilter filter = new ParamFilter();
    		filter.op = getOpCode(op);
    		filter.param = identifier;
    		
    		result.add(filter);
    		
		}
    	
    	return result;
	}
    
    
    private static byte[] getValueData(String value) {
    	if (value.matches("[0-9]+")) {
			int i = Integer.parseInt(value);
			return DataUtil.intToBytes(i);
			
		} else {
			return value.getBytes();
		} 
	}

    private static byte getTypeCode(String value) {
		if (value.matches("[0-9]+")) {
			return (byte)Parameter.Type.INT.ordinal();
			
		} else {
			return (byte)Parameter.Type.STRING.ordinal();
			
		} 
	}

    private static byte getOpCode(String op) {
    	if (op.equals("=")) {
    		return (byte)Parameter.Operator.EQUALS.ordinal();
    		
    	} else if (op.equals(">")) {
    		return (byte)Parameter.Operator.GREATER_THAN.ordinal();
    		
    	}else if (op.equals("<")) {
    		return (byte)Parameter.Operator.SMALLER_THAN.ordinal();
    		
    	}else if (op.equals(">=") || op.equals("=>")) {
    		return (byte)Parameter.Operator.EQUALS_OR_GREATER_THAN.ordinal();
    		
    	}else if (op.equals("<=") || op.equals("=<")) {
    		return (byte)Parameter.Operator.EQUALS_OR_SMALLER_THAN.ordinal();
    		
    	} else {
    		return (byte)-1;
    	}
    }
}
