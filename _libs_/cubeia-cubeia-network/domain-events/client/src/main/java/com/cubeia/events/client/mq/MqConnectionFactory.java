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

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This connection factory is really bad performance wise, we should
 * pool connections and re-use them etc. 
 * 
 * @author Fredrik
 */
public class MqConnectionFactory {

	Logger log = LoggerFactory.getLogger(getClass());

	/** One shared pool of connections */
	private static PooledConnectionFactory pooledConnectionFactory;
	
	// URL of the JMS server.
	// private static String url = "tcp://localhost:61616?soTimeout=2000&connectionTimeout=2000";
	private static String url;

	private DomainEventProperties props = new DomainEventProperties();
	
	public MqConnectionFactory() {
		url = props.getActiveMqUrl();
		log.info("Events Client will connect to AMQ at url: "+url);
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);
		pooledConnectionFactory.setMaxConnections(32);
		pooledConnectionFactory.setCreateConnectionOnStartup(true);
	}
	
	public Connection getConnection() {
		try {
			// Getting JMS connection from the server and starting it
			Connection connection = pooledConnectionFactory.createConnection();
			connection.start();
			return connection;
		} catch (JMSException e) {
			log.warn("Failed to create connection to MQ["+url+"]. Will try to reconnect");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This is a one way street
	 */
	public void shutdown() {
		pooledConnectionFactory.stop();
	}
	
}
