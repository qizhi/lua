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

package com.cubeia.poker.result;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.pot.RakeInfoContainer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class HandResultRakeContributionTest {

    private Currency eur = new Currency("EUR",2);

    @Test
    public void testGetRakeContributionByPlayer() {
        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();

        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        PokerPlayer player3 = mock(PokerPlayer.class);

        Result result1 = new Result(BigDecimal.ZERO, new BigDecimal(500), new HashMap<Pot, BigDecimal>());
        Result result2 = new Result(BigDecimal.ZERO, new BigDecimal(1500), new HashMap<Pot, BigDecimal>());
        Result result3 = new Result(BigDecimal.ZERO, new BigDecimal(1500), new HashMap<Pot, BigDecimal>());

        results.put(player1, result1);
        results.put(player2, result2);
        results.put(player3, result3);

        BigDecimal totalPot = new BigDecimal(500 * 3 + 1000 * 2);
        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(totalPot, totalPot.divide(BigDecimal.TEN), null);
        HandResult result = new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(),eur);

        assertThat(result.getRakeContributionByPlayer(player1), is(new BigDecimal("50.00")));
        assertThat(result.getRakeContributionByPlayer(player2), is(new BigDecimal("150.00")));
        assertThat(result.getRakeContributionByPlayer(player3), is(new BigDecimal("150.00")));
    }

    @Test
    public void testGetRakeContributionDecimalOverflow() {

        // here we should test when players own contribution is exactly a third of the total pot
        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();

        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        PokerPlayer player3 = mock(PokerPlayer.class);

        Result result1 = new Result(BigDecimal.ZERO, new BigDecimal(2), new HashMap<Pot, BigDecimal>()); // ante
        Result result2 = new Result(BigDecimal.ZERO, new BigDecimal(2+52), new HashMap<Pot, BigDecimal>());
        Result result3 = new Result(BigDecimal.ZERO, new BigDecimal(2+52), new HashMap<Pot, BigDecimal>()); // ante + call

        results.put(player1, result1);
        results.put(player2, result2);
        results.put(player3, result3);

        BigDecimal totalPot = new BigDecimal(110);
        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(totalPot, totalPot.multiply(new BigDecimal(0.05)), null); // 5% rake
        new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(),  eur);

    }

    @Test
    public void testGetRakeContributionWhenRakeIsTiny() {

        // here we should test when players own contribution is exactly a third of the total pot
        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();

        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        PokerPlayer player3 = mock(PokerPlayer.class);

        Result result1 = new Result(BigDecimal.ZERO, new BigDecimal(20), new HashMap<Pot, BigDecimal>()); // ante
        Result result2 = new Result(BigDecimal.ZERO, new BigDecimal(540), new HashMap<Pot, BigDecimal>()); // ante + bet
        Result result3 = new Result(BigDecimal.ZERO, new BigDecimal(540), new HashMap<Pot, BigDecimal>()); // ante + call

        results.put(player1, result1);
        results.put(player2, result2);
        results.put(player3, result3);

        BigDecimal totalPot = new BigDecimal(1100);
        BigDecimal totalRake = totalPot.multiply(new BigDecimal("0.05"));
        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(totalPot, totalRake, null); // 5% rake
        HandResult result = new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(),eur);

        BigDecimal p1Rake = result.getRakeContributionByPlayer(player1);
        BigDecimal p2Rake = result.getRakeContributionByPlayer(player2);
        BigDecimal p3Rake = result.getRakeContributionByPlayer(player3);

        // check that its 
        assertThat(p1Rake.add(p2Rake).add(p3Rake), is(totalRake));

        // check that the rakes is about right for each person
        // this is a bit random so we can not see the absolute exact value for each person since
        // it rounds the rake and someone will have to take the rounding error 
        assertThat(p1Rake, new InRangeMatcher(BigDecimal.ONE, new BigDecimal("2")));
        assertThat(p2Rake, new InRangeMatcher(new BigDecimal("27"), new BigDecimal("28")));
        assertThat(p3Rake, new InRangeMatcher(new BigDecimal("27"), new BigDecimal("28")));


    }

    public class InRangeMatcher extends BaseMatcher<BigDecimal> {


        private final BigDecimal min;
        private final BigDecimal max;

        public InRangeMatcher(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean matches(Object item) {
            BigDecimal l = (BigDecimal) item;

            return (l.compareTo(min) >= 0 && l.compareTo(max) <= 0);

        }

        @Override
        public void describeTo(Description description) {
            description.appendText("value between " + min + " inclusive and " + max + " inclusive");
        }


    }


}


