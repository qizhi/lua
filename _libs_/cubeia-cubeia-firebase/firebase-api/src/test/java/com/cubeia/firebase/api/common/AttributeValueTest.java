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
package com.cubeia.firebase.api.common;

import java.util.Date;

import junit.framework.TestCase;

public class AttributeValueTest extends TestCase {

	public void testGetIntValue() {
		AttributeValue val = new AttributeValue(123);
		assertEquals(123, val.getIntValue());
		
		try {
			val.getStringValue();
			fail();
		} catch (ClassCastException e) {}
		
		try {
			val.getDateValue();
			fail();
		} catch (ClassCastException e) {}
	}

	public void testGetStringValue() {
		AttributeValue val = new AttributeValue("apa");
		assertEquals("apa", val.getStringValue());
		
		try {
			val.getIntValue();
			fail();
		} catch (ClassCastException e) {}
		
		try {
			val.getDateValue();
			fail();
		} catch (ClassCastException e) {}
	}

	public void testGetDateValue() {
		Date now = new Date();
		AttributeValue val = new AttributeValue(now);
		assertEquals(now, val.getDateValue());
		
		try {
			val.getStringValue();
			fail();
		} catch (ClassCastException e) {}
		
		try {
			val.getIntValue();
			fail();
		} catch (ClassCastException e) {}
	}

}
