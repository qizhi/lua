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

package com.cubeia.poker.variant.texasholdem;

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.betting.FutureActionsCalculator;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

public class TexasHoldemFutureActionsCalculatorTest {

    @Test
    public void testAllIn() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(true);
        when(player.hasFolded()).thenReturn(false);

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.isEmpty(), is(true));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    @Test
    public void testNotAllIn() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);
        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.isEmpty(), not(true));
    }


    @Test
    public void testNotFolded() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.isEmpty(), not(true));
    }

    @Test
    public void testFolded() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(true);
        when(player.getBetStack()).thenReturn(BigDecimal.ZERO);

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.isEmpty(), is(true));
    }


    @Test
    public void testHavingHighestBet() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.hasActed()).thenReturn(false);
        when(player.getBetStack()).thenReturn(bd(100));
        when(player.getBalance()).thenReturn(bd(2000));

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.size(), is(3));
        assertThat(options, hasItems(PokerActionType.RAISE,PokerActionType.FOLD,PokerActionType.CHECK));

    }

    @Test
    public void testNotHavingHighestBet() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.getBetStack()).thenReturn(bd(50));
        when(player.getBalance()).thenReturn(bd(2000));

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options, hasItems(PokerActionType.FOLD,PokerActionType.CALL,PokerActionType.RAISE));
        assertThat(options.size(), is(3));
    }
    @Test
    public void testNotHavingHighestBetAndBettingCapped() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.getBetStack()).thenReturn(bd(50));
        when(player.getBalance()).thenReturn(bd(2000));

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),true);
        assertThat(options, hasItems(PokerActionType.FOLD,PokerActionType.CALL));
        assertThat(options.size(), is(2));
    }

    @Test
    public void testNotHavingHighestBetNoLimit() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.NO_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.getBetStack()).thenReturn(bd(50));
        when(player.getBalance()).thenReturn(bd(2000));

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options, hasItems(PokerActionType.FOLD,PokerActionType.CALL));
        assertThat(options.size(), is(2));
    }

    @Test
    public void testHavingHighestBetButHaveActed() {

        FutureActionsCalculator calc = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);

        PokerPlayer player = Mockito.mock(PokerPlayer.class);
        when(player.isAllIn()).thenReturn(false);
        when(player.hasFolded()).thenReturn(false);
        when(player.hasActed()).thenReturn(true);
        when(player.getBetStack()).thenReturn(bd(100));
        when(player.getBalance()).thenReturn(bd(2000));

        List<PokerActionType> options = calc.calculateFutureActionOptionList(player, bd(100),false);
        assertThat(options.size(), is(0));

    }


}
