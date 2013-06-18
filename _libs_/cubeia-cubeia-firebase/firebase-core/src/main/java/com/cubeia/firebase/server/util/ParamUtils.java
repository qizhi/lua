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
package com.cubeia.firebase.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.Param;

/**
 * Utility class for working with {@link Param}s.
 * 
 */
public class ParamUtils {

	// private final static Logger log = Logger.getLogger(ParamUtils.class);

	/**
	 * Gets a list of {@link Param}s given a {@link Map} of parameters.
	 * 
	 * @param parameterMap
	 * @return
	 */
	public static List<Param> getParameterList(Map<Object, Object> parameterMap) {
		List<Param> params = new ArrayList<Param>(parameterMap.size());
		for (Object oKey : parameterMap.keySet()) {
			String key = String.valueOf(oKey);
			if (key.startsWith("_")) continue; // Ignore internal
			Object value = parameterMap.get(oKey);
			Param param = ParameterUtil.createParam(key, value);
			if (param != null) {
				params.add(param);
			}
		}
		return params;
	}
}
