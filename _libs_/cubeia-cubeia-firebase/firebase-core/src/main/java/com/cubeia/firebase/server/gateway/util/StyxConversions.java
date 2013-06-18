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
package com.cubeia.firebase.server.gateway.util;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.Param;

public class StyxConversions {
	
	/**
	 * 
	 * 
	 * @throws IllegalArgumentException if the parameter type was not recognized
	 * @param param
	 * @return
	 */
	public static Attribute convertToAttribute(Param param)  {
		// ByteBuffer buffer = ByteBuffer.wrap(param.value);
		
		AttributeValue value;
		
		if (param.type == Parameter.Type.INT.ordinal()) {
			Integer v = ParameterUtil.convertAsInt(param).getValue();
			value = AttributeValue.wrap(v);
		} else if (param.type == Parameter.Type.STRING.ordinal()) {
			String s = ParameterUtil.convertAsString(param).getValue();
			value = AttributeValue.wrap(s);
		} else if(param.type == Parameter.Type.DATE.ordinal()) {
			Date d = ParameterUtil.convertAsDate(param).getValue();
			value = AttributeValue.wrap(d);
		} else {
			throw new IllegalArgumentException("Unknown type in parameter for Filtered Join. Type: "+param.type);
		}
		
		return new Attribute(param.key, value);
	}
	
	
	public static List<Attribute> convertToAttributes(List<Param> params) {
		List<Attribute> attributes = new LinkedList<Attribute>();
    	for (Param param : params) {
    		Attribute attribute = convertToAttribute(param);
    		attributes.add(attribute);
    	}
		return attributes;
	}
}
