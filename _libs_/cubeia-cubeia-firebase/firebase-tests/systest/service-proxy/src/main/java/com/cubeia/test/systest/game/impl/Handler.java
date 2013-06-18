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
package com.cubeia.test.systest.game.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;

public class Handler implements InvocationHandler {
	
	public static final ThreadLocal<AtomicBoolean> CHECK = new ThreadLocal<AtomicBoolean>() {
		
		@Override
		protected AtomicBoolean initialValue() {
			return new AtomicBoolean(false);
		}
	};
	
	@Inject
	private ServiceContract impl;
	
	@Inject
	private PersistService serv;
	
	@Inject 
	private UnitOfWork unit;

	private void init() {
		if(impl == null) {
			Injector injector = Guice.createInjector(new TestModule(), new JpaPersistModule("systest"));
			injector.injectMembers(this);
			serv.start();
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		init();
		unit.begin();
		CHECK.get().set(true);
		try {
			return method.invoke(impl, args);
		} finally {
			CHECK.get().set(false);
			unit.end();
		}
	}
}
