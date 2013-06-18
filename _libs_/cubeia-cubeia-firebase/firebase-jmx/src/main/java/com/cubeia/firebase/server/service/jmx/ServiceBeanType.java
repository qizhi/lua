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
package com.cubeia.firebase.server.service.jmx;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * This is a static helper class for creating composite type descriptions
 * for service beans. The types, in order, are:
 * 
 * <ol>
 * 	<li>Service name /STRING</li>
 *  <li>Public id /STRING</li>
 *  <li>Service description /STRING</li>
 *  <li>Contract class name /STRING</li>
 *  <li>Service class name /STRING</li>
 *  <li>Is auto-start flag /BOOLEAN</li>
 *  <li>Is isolated flag /BOOLEAN</li>
 *  <li>Is public flag /BOOLEAN</li>
 *  <li>Service dependencies /STRING</li>
 *  <li>Is started /BOOLEAN</li>
 *  <li>Stack capture on startup /BOOLEAN</li>
 * </ol>
 * 
 * The actual type names are contained within the {@link #TYPE_NAMES} array and the 
 * corresponding open types in the {@link #TYPES} array.
 * 
 * @author Larsan
 */
public class ServiceBeanType {
	
	/**
	 * Type names.
	 */
	public static final String[] TYPE_NAMES = new String[] { "name", "public-id", "description", "contract-classes", "service-class", "autostart", "isolated", "public", "dependencies", "is-started", "startup-stack-capture" };
	
	
	/**
	 * Type descriptions.
	 */
	public static final String[] TYPE_DESCS = new String[] { "Service name", "Public id", "Service description", "Contract class name", "Service class name", "Autostart flag", "Isolation flag", "Public flag", "Service dependencies", "Is Started?", "Stack capture on startup" };
	
	
	/**
	 * Types.
	 */
	public static final OpenType<?>[] TYPES = new OpenType[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.STRING };
	
	/**
	 * Get the composite type used to describe a single service. This 
	 * is used when creating more complex tabular data.
	 * 
	 * @return A composite type for a service bean, never null
	 * @throws OpenDataException
	 */
	public static CompositeType getType() throws OpenDataException {
		return new CompositeType("ServiceType", 
				  "Firebase Service", 
				  TYPE_NAMES,
				  TYPE_DESCS,
				  TYPES);
		
	}
	
	/*
	 * No instantiation.
	 */
	private ServiceBeanType() { }

}