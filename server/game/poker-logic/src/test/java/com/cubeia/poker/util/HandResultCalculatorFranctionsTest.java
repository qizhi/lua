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

package com.cubeia.poker.util;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.model.PlayerHand;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.rake.LinearRakeWithLimitCalculator;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Ignore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@Ignore
public class HandResultCalculatorFranctionsTest extends TestCase {

    private static final Logger log = Logger.getLogger(HandResultCalculatorFranctionsTest.class);

    private Map<Integer, PokerPlayer> players;

    HandResultCalculator calc = new HandResultCalculator(new TexasHoldemHandCalculator());

    private ArrayList<PlayerHand> hands;
    private LinearRakeWithLimitCalculator rakeCalculator;

    private BigDecimal rakeFraction;

    private Currency eur = new Currency("EUR",2);

    @Override
    protected void setUp() throws Exception {
        // All players has 100 and bets 10, 20 and 40 respectively
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(new BigDecimal("1.00"));
        p1.addBet(new BigDecimal("0.03"));
        players.put(1,p1);

        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(new BigDecimal("1.00"));
        p2.addBet(new BigDecimal("0.03"));
        players.put(2,p2);

        for(int i = 3; i<7; i++) {
            PokerPlayer p = new DefaultPokerPlayer(1);
            p.addChips(new BigDecimal("1.00"));
            p.addBet(new BigDecimal("0.01"));
            players.put(i,p);
        }

        hands = new ArrayList<PlayerHand>();

        String community = "3c 3d 3s 3h As";
        hands.add(new PlayerHand(1, new Hand("5h 4h " + community)));
        hands.add(new PlayerHand(2, new Hand("2s 7d " + community)));
        hands.add(new PlayerHand(3, new Hand("3s 8d " + community)));
        hands.add(new PlayerHand(4, new Hand("4d 4c " + community)));
        hands.add(new PlayerHand(5, new Hand("7c 5c " + community)));
        hands.add(new PlayerHand(6, new Hand("2c 7h " + community)));

        rakeFraction = new BigDecimal("0.0");
        rakeCalculator = new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(rakeFraction), new Currency("EUR",2));
    }




    public void testGetWinnersWithRake() {
        hands = new ArrayList<PlayerHand>();

        PotHolder potHolder = new PotHolder(rakeCalculator);
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertThat(potHolder.getPotSize(0), is(new BigDecimal("0.10")));

        Map<PokerPlayer, Result> calc = this.calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players, eur);

        Result result1 = calc.get(players.get(1));
        assertThat(result1.getNetResult(), is(new BigDecimal("-0.10")));
        assertThat(result1.getWinningsIncludingOwnBets(), is(new BigDecimal("0.2")));

    }

    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
    private BigDecimal bd(long l) {
        return new BigDecimal(l);
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }


}
