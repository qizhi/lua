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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelesinaCanPlayerBuyInTest {

    @Test
    public void testCanPlayerAffordEntryBet() {
        PokerPlayer player = mock(PokerPlayer.class);

        Telesina telesina = new Telesina(null, null, null);

        int anteLevel = 20;
        BlindsLevel level = new BlindsLevel(bd(anteLevel), bd(anteLevel * 2), bd(anteLevel));
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(level, betStrategy, bd(0), bd(0), null, 0, null, null, null);

        when(player.getBalance()).thenReturn(bd(anteLevel + 1));
        when(player.getPendingBalanceSum()).thenReturn(BigDecimal.ZERO);
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));

        when(player.getBalance()).thenReturn(bd(anteLevel + 0));
        when(player.getPendingBalanceSum()).thenReturn(BigDecimal.ZERO);
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));

        when(player.getBalance()).thenReturn(bd(anteLevel - 1));
        when(player.getPendingBalanceSum()).thenReturn(BigDecimal.ZERO);
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(false));
    }

    private BigDecimal bd(int anteLevel) {
        return  new BigDecimal(anteLevel);
    }

    @Test
    public void testCanPlayerAffordEntryBetWithPending() {
        PokerPlayer player = mock(PokerPlayer.class);

        Telesina telesina = new Telesina(null, null, null);

        int anteLevel = 20;
        BlindsLevel level = new BlindsLevel(bd(anteLevel), bd(anteLevel * 2), bd(anteLevel));
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(level, betStrategy, bd(0), bd(0), null, 0, null, null, null);

        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel + 1));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(false));

        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel + 0));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(false));

        when(player.getBalance()).thenReturn(BigDecimal.ZERO);
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel - 1));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(false));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(false));
    }


    @Test
    public void testCanPlayerAffordEntryBetWithBothPendingAndNormal() {
        PokerPlayer player = mock(PokerPlayer.class);

        Telesina telesina = new Telesina(null, null, null);

        int anteLevel = 20;
        BlindsLevel level = new BlindsLevel(bd(anteLevel), bd(anteLevel * 2), bd(anteLevel));
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(level, betStrategy, bd(0), bd(0), null, 0, null, null, null);

        when(player.getBalance()).thenReturn(bd(anteLevel / 2));
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel / 2));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(false));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));

        when(player.getBalance()).thenReturn(bd(anteLevel - 1));
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(false));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));

        when(player.getBalance()).thenReturn(bd(anteLevel));
        when(player.getPendingBalanceSum()).thenReturn(bd(anteLevel));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, false), is(true));
        assertThat(telesina.canPlayerAffordEntryBet(player, settings, true), is(true));
    }

}
