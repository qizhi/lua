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

package com.cubeia.poker;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.factory.GameTypeFactory;

import java.math.BigDecimal;

public abstract class AbstractTexasHandTester extends GuiceTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setAnteLevel(500);
    }

    protected void setAnteLevel(int anteLevel) {
        GameType gameType = GameTypeFactory.createGameType(variant);
        state.init(gameType, createPokerSettings(new BigDecimal(anteLevel).setScale(2)));
    }

    protected void act(int playerId, PokerActionType actionType) {
        PossibleAction option = mockServerAdapter.getLastActionRequest().getOption(actionType);
        act(playerId, actionType, option.getMinAmount());
    }
    protected void act(int playerId, PokerActionType actionType, long amount){
        act(playerId,actionType,new BigDecimal(amount));
    }
    protected void act(int playerId, PokerActionType actionType, String amount){
        act(playerId,actionType,new BigDecimal(amount));
    }
    protected void act(int playerId, PokerActionType actionType, BigDecimal amount) {
        PokerAction action = new PokerAction(playerId, actionType);
        action.setBetAmount(amount);
        state.act(action);
    }

    protected void addPlayers(PokerState game, PokerPlayer[] p) {
        for (PokerPlayer pl : p) {
            game.addPlayer(pl);
        }
    }
    protected BigDecimal bd(String i) {
        return new BigDecimal(i);
    }
    protected BigDecimal bd(int i) {
        return new BigDecimal(i).setScale(2);
    }

}
