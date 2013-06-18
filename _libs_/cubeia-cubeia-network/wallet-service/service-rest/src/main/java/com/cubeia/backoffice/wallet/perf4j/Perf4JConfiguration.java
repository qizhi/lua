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
package com.cubeia.backoffice.wallet.perf4j;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Perf4JConfiguration {

	public Perf4JConfiguration() {
		// Load extra perf4j configuration which will be appended to any existing log4j config.
		// PropertyConfigurator log4jConfigurator = new PropertyConfigurator();
		URL resource = this.getClass().getResource("/perf4j.xml");
		System.out.println("Append Perf4J configuration: "+resource);
		DOMConfigurator.configure(resource);
	}
	
}
