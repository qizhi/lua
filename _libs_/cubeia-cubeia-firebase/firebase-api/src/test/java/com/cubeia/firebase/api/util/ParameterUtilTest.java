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
package com.cubeia.firebase.api.util;

import java.util.Date;

import junit.framework.TestCase;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.defined.Parameter.Type;
import com.cubeia.firebase.io.protocol.Param;

public class ParameterUtilTest extends TestCase {

	public void testString() throws Exception {
		String key = "blärg";
		String test = "? & % $ €";
		Param p = ParameterUtil.convert(new Parameter<String>(key, test, Type.STRING));
		Parameter<String> param = ParameterUtil.convertAsString(p);
		assertEquals(Type.STRING, param.getType());
		assertEquals(key, param.getKey());
		assertEquals(test, param.getValue());
	}
	
	public void testInt() throws Exception {
		int val = 200675;
		String key = "blärg";
		Param p = ParameterUtil.convert(new Parameter<Integer>(key, val, Type.INT));
		Parameter<Integer> param = ParameterUtil.convertAsInt(p);
		assertEquals(Type.INT, param.getType());
		assertEquals(val, param.getValue().intValue());
		assertEquals(key, param.getKey());
	}
	
	public void testDate() throws Exception {
		String key = "blärg";
		Date date = new Date();
		Param p = ParameterUtil.convert(new Parameter<Date>(key, date, Type.DATE));
		Parameter<Date> param = ParameterUtil.convertAsDate(p);
		assertEquals(Type.DATE, param.getType());
		assertEquals(adjustForSeconds(date.getTime()), param.getValue().getTime());
		assertEquals(key, param.getKey());
	}

	private long adjustForSeconds(long time) {
		int sec = (int)(time / 1000);
		return (sec * 1000L);
	}
}
