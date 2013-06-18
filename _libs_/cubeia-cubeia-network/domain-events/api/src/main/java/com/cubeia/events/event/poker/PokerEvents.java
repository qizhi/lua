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
package com.cubeia.events.event.poker;

import com.cubeia.events.event.GameEvent;
import com.cubeia.events.event.GameEventType;

public class PokerEvents {
	
	public static GameEvent createRoundEnd(String player, long stakeInCents, long totalWinAmountInCents) {
		GameEvent event = new GameEvent();
		event.game = PokerAttributes.poker.name();
		event.player = player;
		event.type = GameEventType.roundEnd.name();
		
		event.attributes.put(PokerAttributes.stake.name(), stakeInCents+"");
		event.attributes.put(PokerAttributes.winAmount.name(), totalWinAmountInCents+"");
		event.attributes.put("win", "true");
		
		return event;
	}
	
	public static GameEvent createPlayerSessionEnd(String playerId, String screenname, String accountBalance, String currency) {
		GameEvent event = new GameEvent();
		event.game = PokerAttributes.poker.name();
		event.player = playerId;
		event.type = GameEventType.leaveTable.name();
		
		event.attributes.put(PokerAttributes.accountBalance.name(), accountBalance);
		event.attributes.put(PokerAttributes.accountCurrency.name(), currency);
		event.attributes.put(PokerAttributes.screenname.name(), screenname);
		
		return event;
	}
	
}
