/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.backoffice.accounting.perf;

import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/*
 * Bootstrapping main class. Sets up the injection scheme
 * and runs a server instance. 
 */
public class Bootstrap {

	private static Injector injector;

	public static void main(String[] args) {
		Properties p = toProperties(args);
		injector = Guice.createInjector(new Module[] { new BaseModule(p) });
		Server instance = (Server) injector.getInstance(Server.class);
		instance.run();
	}

	private static Properties toProperties(String[] args) {
		Properties p = new Properties();
		for (String tmp : args) {
			String[] split = tmp.split("=");
			p.put(split[0], split[1]);
		}
		return p;
	}
}
