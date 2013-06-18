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
package com.cubeia.events.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.events.event.GameEvent;
import com.cubeia.events.event.OperatorEvent;
import com.cubeia.events.event.PlayerEvent;
import com.cubeia.events.event.achievement.BonusEvent;
import com.cubeia.events.event.operator.OperatorEvents;
import com.cubeia.events.event.operator.PlayerEvents;

public class EventClientRunner implements EventListener {

	Logger log = LoggerFactory.getLogger(getClass());
	
	public static void main(String[] args) {
		try {
			
			EventClient client = new EventClient();
			// client.setEventListener(new EventClientRunner());
			
			Thread.sleep(1000);
			
			sendOperatorAuthenticationError(client);
			// sendPokerRoundEndEvent(client);
			// sendUserLoggedInEvent(client);
			
			// while (true) {}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void sendUserLoggedInEvent(EventClient client) {
		PlayerEvent event = PlayerEvents.createLoggedIn(22L, 0L, "whatever");
		client.send(event);
	}

	private static void sendOperatorAuthenticationError(EventClient client) {
		OperatorEvent event = OperatorEvents.createAuthenticationError(1L, "test", "http://localhost/foo");
		client.send(event);
	}
	
	@SuppressWarnings("unused")
	private static void sendPokerRoundEndEvent(EventClient client) {
		GameEvent event = new GameEvent();
		event.game = "kalaha";
		event.type = "roundEnd";
		event.player = "Bananen";
		event.attributes.put("stake", "10");
		event.attributes.put("winAmount", "20");
		event.attributes.put("win", "true");
		client.send(event);
	}

	public void onEvent(GameEvent event) {
		log.info(" *** On Game Event: "+event);
	}

	@Override
	public void onBonusEvent(BonusEvent event) {
		log.info(" *** On Bonus Event: "+event);
	}

}
