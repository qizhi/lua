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

import static com.cubeia.firebase.api.common.AttributeValue.Type.DATE;
import static com.cubeia.firebase.api.common.AttributeValue.Type.INT;
import static com.cubeia.firebase.api.common.AttributeValue.Type.STRING;

import java.io.Serializable;
import java.util.Date;

import com.cubeia.firebase.api.util.Arguments;

/**
 * This is a "struct" for attribute values as displayed by the lobby accessor.
 * A value is either an int, a string or a date and it contains a {@link Type type}
 * discriminator and a serializable value which can be cast into the appropriate
 * type class.
 *
 * @author lars.j.nilsson
 */
public final class AttributeValue implements Serializable {

	private static final long serialVersionUID = -5166580525368812991L;

	/**
	 * Attempts to convert an object to an attribute value. This method
	 * returns null should the object not be of type int, string or date.
	 *
	 * @param o Object to wrap, may be null
	 * @return An attribute value, or null
	 */
	public static AttributeValue wrapObject(Object o) {
		if(o instanceof Integer) return new AttributeValue((Integer)o);
		else if(o instanceof String) return new AttributeValue((String)o);
		else if(o instanceof Date) return new AttributeValue((Date)o);
		else return null;
	}

	/**
	 * Wraps an int in an AttributeValue.
	 *
	 * @param integer int to wrap, may not be null
	 * @return The integer as an attribute value
	 */
	public static AttributeValue wrap(Integer integer) {
		return new AttributeValue(integer);
	}

	/**
	 * Wraps a string in an AttributeValue.
	 *
	 * @param string String to wrap, may not be null
	 * @return The string as an attribute value
	 */

	public static AttributeValue wrap(String string) {
		return new AttributeValue(string);
	}

	/**
	 * Wraps a date in an AttributeValue.
	 *
	 * @param date Date to wrap, may not be null
	 * @return The date as an attribute value
	 */
	public static AttributeValue wrap(Date date) {
		return new AttributeValue(date);
	}


	/**
	 * Attribute type enumeration. Available types are <code>int</code>, {@link String} and {@link Date}.
	 */
	public static enum Type {
		INT,
		STRING,
		DATE
	}

	// --- INSTANCE MEMBERS --- //

	public final Type type;
	public final Serializable data;

	public AttributeValue(int value) {
		this.data = value;
		this.type = Type.INT;
	}

	/**
	 * @param value String value, must not be null
	 */
	public AttributeValue(String value) {
		Arguments.notNull(value, "value");
		this.type = Type.STRING;
		this.data = value;
	}

	/**
	 * @param value Date value, must not be null
	 */
	public AttributeValue(Date value) {
		Arguments.notNull(value, "value");
		this.type = Type.DATE;
		this.data = value;
	}

	public Type getType() {
		return type;
	}

	public Serializable getData() {
		return data;
	}

	/**
	 * Gets value as an Integer.
	 * If the value type is not Type.INT then a ClassCastException will be thrown.
	 *
	 * @return The value as an int
	 */
	public int getIntValue() {
		if (type == INT) {
			return (Integer)data;
		} else {
			throw new ClassCastException("Tried to get value as Integer. Value is type: "+type.name());
		}
	}

	/**
	 * Gets value as a String.
	 * If the value type is not Type.STRING then a ClassCastException will be thrown.
	 *
	 * @return The value as String (never null)
	 */
	public String getStringValue() {
		if (type == STRING) {
			return (String)data;
		} else {
			throw new ClassCastException("Tried to get value as String. Value is type: "+type.name());
		}
	}

	/**
	 * Gets value as an Integer.
	 * If the value type is not Type.DATE then a ClassCastException will be thrown.
	 *
	 * @return The value as Date (never null)
	 * @throws ClassCastException if the value type is not Type.DATE.
	 */
	public Date getDateValue() {
		if (type == DATE) {
			return (Date)data;
		} else {
			throw new ClassCastException("Tried to get value as Date. Value is type: "+type.name());
		}
	}

	// --- OBJECT METHODS --- //

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof AttributeValue)) return false;
		AttributeValue v = (AttributeValue)o;
		return (v.type == type && v.data.equals(data));
	}

	@Override
	public int hashCode() {
		return type.hashCode() ^ data.hashCode();
	}

	@Override
	public String toString() {
		return "Attribute; type: " + type + "; data: " + data;
	}
}
