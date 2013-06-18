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
package com.cubeia.firebase.server;

public class ContextLookup {

	//private final static Logger log = Logger.getLogger(ContextLookup.class);
	
	private ContextLookup() { }

	/*public static QueueConnectionFactory getQueueConnectionFactory() {
		try {
			return (QueueConnectionFactory)Constants.ROOT_CONTEXT.lookup(Constants.JMS_QUEUE_CONNECTION_FACTORY_NAME);
		} catch (NamingException e) {
			log.fatal("failed to retreive queue connection factory", e);
			return null;
		}
	}
	
	public static TopicConnectionFactory getTopicConnectionFactory() {
		try {
			return (TopicConnectionFactory)Constants.ROOT_CONTEXT.lookup(Constants.JMS_TOPIC_CONNECTION_FACTORY_NAME);
		} catch (NamingException e) {
			log.fatal("failed to retreive topic connection factory", e);
			return null;
		}
	}*/
}
