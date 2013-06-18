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
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.events.client.EventListener;
import com.cubeia.events.event.GameEvent;

public class Consumer implements MessageListener, ExceptionListener {

	// Name of the queue we will receive messages from
	private static String subject = "bonusEvents";

	Logger log = LoggerFactory.getLogger(getClass());
	
	MqConnectionFactory connections = new MqConnectionFactory();

	EventListener eventListener;
	
	ObjectMapper mapper = new ObjectMapper();
	
	public void startConsuming() {
		try {
			Connection connection = connections.getConnection();

			// JMS messages are sent and received using a Session. We will
			// create here a non-transactional session object. If you want
			// to use transactions you should set the first parameter to 'true'
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Getting the topic 
			Destination destination = session.createTopic(subject);

			MessageConsumer consumer = session.createConsumer(destination);
			connection.setExceptionListener(this);
			consumer.setMessageListener(this);
			
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}


	public void onException(JMSException e) {
		log.debug("Event MQ reported an exception: " +e);
	}


	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				GameEvent event = mapper.readValue(textMessage.getText(), GameEvent.class);
				log.debug("Bonus client received Event: "+event+". Will route to listener: "+eventListener.getClass().getSimpleName());
				eventListener.onEvent(event);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}
}
