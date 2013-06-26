package com.cubeia.games.poker.adapter.domainevents;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.events.event.GameEvent;
import com.cubeia.events.event.poker.PokerAttributes;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.settings.PokerSettings;

public class DomainEventAdapter {
	
	Logger log = LoggerFactory.getLogger(getClass());

	/** Service for sending and listening to bonus/achievement events to players */
	@Service DomainEventsService service;
	
	/**
	 * Report hand end result to the achievment service
	 * @param handResult
	 * @param handEndStatus
	 * @param tournamentTable
	 */
	public void notifyHandEnd(HandResult handResult, HandEndStatus handEndStatus, boolean tournamentTable, PokerSettings pokerSettings) {
		Map<PokerPlayer, Result> map = handResult.getResults();
		for (PokerPlayer player : map.keySet()) {
			sendPlayerHandEnd(player, map.get(player), handResult, tournamentTable, pokerSettings);	
		}
	}
	
	
	public void notifyEndPlayerSession(int playerId, String screenname, int operatorId, Money accountBalance) {
		log.debug("Domain event service: "+service);
		service.sendEndPlayerSessionEvent(playerId, screenname, operatorId, accountBalance);
	}
	
	
	private void sendPlayerHandEnd(PokerPlayer player, Result result, HandResult handResult, boolean tournamentTable, PokerSettings pokerSettings) {
		int operatorId = player.getOperatorId();
		
		// We don't want to push events for operator id 0 which is reserved for bots and internal users.
		// TODO: Perhaps make excluded operators configurable
		// You can override this by starting with the system property -Devent.bots 
		if (operatorId == 0 && System.getProperty("events.bots") == null) {
			return; 
		}
		
		BigDecimal stake = calculateStake(result);
		
		GameEvent event = new GameEvent();
		event.game = "poker";
		event.type = "roundEnd";
		event.player = player.getId()+"";
		event.operator = operatorId+"";
		event.attributes.put(PokerAttributes.stake.name(), stake+"");
		event.attributes.put(PokerAttributes.winAmount.name(), result.getWinningsIncludingOwnBets()+"");
		event.attributes.put(PokerAttributes.netResult.name(), result.getWinningsIncludingOwnBets().subtract(stake).toPlainString());
		event.attributes.put(PokerAttributes.screenname.name(), player.getScreenname());
		event.attributes.put(PokerAttributes.tournament.name(), tournamentTable+"");
		event.attributes.put(PokerAttributes.accountCurrency.name(), pokerSettings.getCurrency().getCode());
		
		boolean isWin = calculateIsWin(result);
		if (isWin) {
			event.attributes.put("win", "true");
		} else {
			event.attributes.put("lost", "true");
		}

		if (isWin) {
			RatedPlayerHand hand = getRatedPlayerHand(player, handResult);
			if (hand != null) {
				event.attributes.put("handType", hand.getHandInfo().getHandType().name());
			}
		}
		
		log.info("Send player hand end event: "+event);
		service.sendEvent(event);
	}

	private RatedPlayerHand getRatedPlayerHand(PokerPlayer player, HandResult handResult) {
		for (RatedPlayerHand rphand : handResult.getPlayerHands()) {
			if (rphand.getPlayerId() == player.getId()) {
				return rphand;
			}
		}
		// This is normal, hand types are only included for non-muck players and show down.
		// log.debug("Could not find hand type for player["+player.getId()+"]");
		return null;
	}

	private boolean calculateIsWin(Result result) {
		return result.getNetResult().compareTo(BigDecimal.ZERO) > 0;
	}

	private BigDecimal calculateStake(Result result) {
		return result.getWinningsIncludingOwnBets().subtract(result.getNetResult());
	}

	
}
