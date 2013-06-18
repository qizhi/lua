package com.cubeia.game.poker.bot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cubeia.games.poker.io.protocol.Enums;
import com.cubeia.games.poker.io.protocol.Enums.ActionType;
import com.cubeia.games.poker.io.protocol.PlayerAction;

public class StrategyTest {

	private Strategy strategy;

	@Before
	public void init() {
		strategy = new Strategy();
	}
	
	@Test
	public void doNotFoldIfCheckIsAvailable() {
		List<PlayerAction> list = newActionList(Enums.ActionType.CHECK, Enums.ActionType.FOLD);
		// this is random, but I think 100 should do...
		for (int i = 0; i < 100; i++) {
			PlayerAction action = strategy.getAction(list);
			assertNotSame(ActionType.FOLD, action.type);
		}
	}

	private List<PlayerAction> newActionList(ActionType...types) {
		List<PlayerAction> list = new ArrayList<PlayerAction>(types.length);
		for (ActionType t : types) {
			list.add(new PlayerAction(t, "-1", "-1"));
		}
		return list;
	}	
}
