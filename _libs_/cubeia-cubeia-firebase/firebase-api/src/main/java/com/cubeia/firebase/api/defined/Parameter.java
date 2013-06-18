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
package com.cubeia.firebase.api.defined;

/**
 * <p>The Parameter DTO holds a constraint for any arbitrary parameter.</p>
 * 
 * <p>A parameter constraint is defined through:</b>
 * <ul>
 * 	<li>Key</li>
 * 	<li>Type</li>
 * 	<li>Value</li>
 *  <li>Operator</li>
 * </ul>
 * </b>
 * Where <em>key</em> is the identifier for the parameter, <em>type</em> is the object type of the value and 
 * <em>value</em> is the value of the parameter.
 * </p>
 * <p>Operator is the constraint for the given key/value.</p>
 * 
 * <p>
 * E.g. to filter out all tables that have more then 5 players seated:</b>
 * <ul>
 * 	<li>Key = "SEATED"</li>
 * 	<li>Type = Parameter.Type.INT</li>
 * 	<li>Value = 5</li>
 *  <li>Operator = Parameter.Operator.GREATER_THAN</li>
 * </ul>
 * 
 * 
 * @author Fredrik
 *
 */
public class Parameter<T> {

	/**
	 * Type for the value.
	 * 
	 * @author Fredrik
	 *
	 */
	public static enum Type {
		STRING,
		INT,
		DATE
	}
	
	/**
	 * Operator for any given parameter.
	 * The ordinals in the enum corresponds to the
	 * defined operator id's in IOConstants.
	 * 
	 * @author Fredrik
	 *
	 */
	public static enum Operator {
		EQUALS,
		GREATER_THAN,
		SMALLER_THAN,
		EQUALS_OR_GREATER_THAN,
		EQUALS_OR_SMALLER_THAN
	}
	
	private final String key;
	private final T value;
	private final Type type;
	private Operator operator;
	
	/**
	 * Constructor using key, value and type.
	 */
	public Parameter(String key, T value, Type type) {
		this.key = key;
		this.value = value;
		this.type = type;		
	}
	
	/**
	 * Constructor with operator constraint.
	 * 
	 * @param key
	 * @param value
	 * @param type
	 * @param operator
	 */
	public Parameter(String key, T value, Type type, Operator operator) {
		this.key = key;
		this.value = value;
		this.type = type;
		this.operator = operator;
		
	}
	
	public String toString() {
		return "key["+key+"] value["+value+"] op["+operator+"] type["+type+"]";
	}
	
	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String getKey() {
		return key;
	}

	public Type getType() {
		return type;
	}

	public T getValue() {
		return value;
	}
}
