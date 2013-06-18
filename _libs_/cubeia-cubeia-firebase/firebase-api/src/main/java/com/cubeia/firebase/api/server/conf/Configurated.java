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
 * Annotation for class level configuration interfaces. This annotation is used
 * to specify the namespace for the configuration contract and the default inheritance 
 * rules for the property resolution. Please refer to {@link Configurable} for more
 * information.
 * 
 * @author lars.j.nilsson
 * @see Configurable
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurated {

	/**
	 * @return The given {@link Namespace namespace}, defaults to an empty string (null) 
	 */
	public String namespace() default "";
	
	
	/**
	 * @return The inheritance rule for the class, defaults to {@link Inheritance#NAN NAN}.
	 */
	public Inheritance inheritance() default Inheritance.NAN;
	
}
