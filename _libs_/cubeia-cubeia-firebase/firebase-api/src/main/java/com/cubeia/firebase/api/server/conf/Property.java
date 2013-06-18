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
package com.cubeia.firebase.api.server.conf;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for method level configuration interfaces. This annotation
 * can be used to override the class level inheritance policy, it can override
 * the property name and it can set an optional default value.
 * 
 * @author lars.nilsson
 * @see Configurable
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

	/**
	 * @return The inheritance level, defaults to {@link Inheritance.NAN}
	 */
	public Inheritance inheritance() default Inheritance.NAN;	
	
	
	/**
	 * @return The overridden property name, defaults to an empty string (not overridden)
	 */
	public String property() default "";
	
	
	/**
	 * @return Property default value, defaults to an empty string (no default)
	 */
	public String defaultValue() default "";
	
	
	/**
	 * @return Fall-back property if this property is not found, defaults to none (empty string)
	 */
	public String fallback() default "";
	
}
