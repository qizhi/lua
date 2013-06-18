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
package com.cubeia.firebase.test.common.rules.impl;

import java.lang.reflect.Field;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.test.common.rules.Filter;
import com.cubeia.firebase.test.common.rules.Operator;

public class MemberAssertFilter implements Filter {

	private final String member;
	private Object value;
	private final Operator operator;
	private final boolean doAssert;

	public MemberAssertFilter(String member, Object value, Operator op, boolean doAssert) {
		Arguments.notNull(member, "member");
		Arguments.notNull(op, "operator");
		this.doAssert = doAssert;
		this.member = member;
		this.value = value;
		this.operator = op;
	}
	
	public MemberAssertFilter(String member, Object value, Operator op) {
		this(member, value, op, true);
	}
	
	public MemberAssertFilter(String member, Object value) {
		this(member, value, Operator.EQUALS, true);
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public boolean accept(Object o) {
		try {
			Object test = PropertyUtils.getSimpleProperty(o, member);
			return testValue(test);
		} catch(NoSuchMethodException e) {
			return checkDirectAccess(o);
		} catch (Exception e) {
			return handleError(e, o);
		} 
	}

	
	// --- PRIVATE METHODS --- //
	
	private boolean checkDirectAccess(Object o) {
		try {
			Object test = tryDirectAccess(o);
			return testValue(test);
		} catch (SecurityException e) {
			return handleError(e, o);
		} catch (Exception e) {
			return handleError(e, o);
		} 
	}

	private Object tryDirectAccess(Object o) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = o.getClass().getDeclaredFields();
		for (Field f : fields) {
			if(f.getName().equals(member)) {
				return f.get(o); // EARLY RETURN
			}
		}
		return null;
	}

	private boolean handleError(Throwable e, Object o) {
		Logger.getLogger(getClass()).warn("Could not extract member '" + member + "' from object of class: " + o.getClass().getName(), e);
		if(doAssert) {
			Assert.fail();
		} 
		return false;
	}

	private boolean testValue(Object test) {
		boolean b = operator.doTest(value, test);
		if(!b && doAssert) {
			Assert.fail("Expected member '" + member + "' to " + operator.name() + " '" + value + "', was: " + test);
		}
		return b;
	}
}
