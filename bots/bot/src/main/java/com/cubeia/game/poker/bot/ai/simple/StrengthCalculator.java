package com.cubeia.game.poker.bot.ai.simple;

import com.cubeia.game.poker.bot.ai.GameState;
import com.cubeia.game.poker.bot.ai.simple.SimpleAI.Strategy;
import com.cubeia.games.poker.io.protocol.RequestAction;
import com.cubeia.games.poker.io.protocol.Enums.HandPhaseHoldem;
import com.cubeia.poker.hand.HandStrength;

public class StrengthCalculator {

	public Strategy getStrategy(RequestAction request, GameState state, HandStrength handStrength) {

		Strategy strategy = Strategy.WEAK;

		if (state.getPhase() == HandPhaseHoldem.PREFLOP) {
			switch (handStrength.getHandType()) {
			case NOT_RANKED:
				strategy = Strategy.WEAK;
				break;
			case HIGH_CARD:
				if (handStrength.getHighestRank().ordinal() < 6) {
					strategy = Strategy.WEAK;
				} else {
					strategy = Strategy.NEUTRAL;
				}
				break;
			default: 
				strategy = Strategy.STRONG;
				break;
			}
		}

		else if (state.getPhase() == HandPhaseHoldem.FLOP) {
			switch (handStrength.getHandType()) {
			case NOT_RANKED:
				strategy = Strategy.WEAK;
				break;
			case HIGH_CARD:
				strategy = Strategy.WEAK;
				break;
			case PAIR:
				if (handStrength.getHighestRank().ordinal() < 8) {
					strategy = Strategy.NEUTRAL;
				} else {
					strategy = Strategy.STRONG;
				}
				break;
			default: 
				strategy = Strategy.STRONG;
				break;
			}
		}

		else if (state.getPhase() == HandPhaseHoldem.TURN) {
			switch (handStrength.getHandType()) {
			case NOT_RANKED:
				strategy = Strategy.WEAK;
				break;
			case HIGH_CARD:
				strategy = Strategy.WEAK;
				break;
			case PAIR:
				if (handStrength.getHighestRank().ordinal() < 9) {
					strategy = Strategy.NEUTRAL;
				} else {
					strategy = Strategy.STRONG;
				}
				break;
			default: 
				strategy = Strategy.STRONG;
				break;
			}
		}

		else if (state.getPhase() == HandPhaseHoldem.RIVER) {
			switch (handStrength.getHandType()) {
			case NOT_RANKED:
				strategy = Strategy.WEAK;
				break;
			case HIGH_CARD:
				strategy = Strategy.WEAK;
				break;
			case PAIR:
				if (handStrength.getHighestRank().ordinal() < 10) {
					strategy = Strategy.NEUTRAL;
				} else {
					strategy = Strategy.STRONG;
				}
				break;
			default: 
				strategy = Strategy.STRONG;
				break;
			}
		}

		return strategy;
	}

}
