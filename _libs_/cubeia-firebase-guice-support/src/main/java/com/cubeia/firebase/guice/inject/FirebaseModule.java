/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.inject;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * A common module for all Firebase components. It binds the
 * special annotations for {@link Log4j log4j} and Firebase
 * {@link Service services}.
 * 
 * @author larsan
 */
public class FirebaseModule extends AbstractModule {

	private final ServiceRegistry reg;
	
	/**
	 * @param reg Service registry, must not be null
	 */
	public FirebaseModule(ServiceRegistry reg) {
		this.reg = reg;
	}
	
	@Override
	protected void configure() {
		bindListener(Matchers.any(), new ServiceTypeListener(reg));
		bindListener(Matchers.any(), new Log4jTypeListener());
	}
}
