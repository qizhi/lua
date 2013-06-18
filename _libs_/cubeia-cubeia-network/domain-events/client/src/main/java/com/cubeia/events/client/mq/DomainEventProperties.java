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
package com.cubeia.events.client.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DomainEventProperties {
	
	private static String DEFAULT_PROPERTIES = "domain-events-default.properties";
	private static String CUSTOM_PROPERTIES = "domain-events.properties";

	private static String ACTIVE_MQ_URL = "activemq.url";
	private static String ENABLED = "activemq.enabled";

	
	Logger log = LoggerFactory.getLogger(getClass());
	
	private Properties props;
	
	public DomainEventProperties() {
		load(CUSTOM_PROPERTIES);
	}
	
	public DomainEventProperties(String customPropertyFileName) {
		load(customPropertyFileName);
	}
	
	
	private Properties load(String customPropertyFileName) {
		props = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(customPropertyFileName);
		 
		if (inputStream == null) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
		}
		 
		try {
			props.load(inputStream);
			log.info("Domain Events Client is configured with: "+props);
			return props;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getActiveMqUrl() {
		return props.getProperty(ACTIVE_MQ_URL);
	}

	public boolean getEnabled() {
		return props.getProperty(ENABLED).equalsIgnoreCase("true");
	}
	
}
