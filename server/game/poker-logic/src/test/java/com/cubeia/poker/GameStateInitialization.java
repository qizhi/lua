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

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.factory.GameTypeFactory;
import com.cubeia.poker.variant.telesina.Telesina;
import com.cubeia.poker.variant.texasholdem.TexasHoldem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static com.cubeia.poker.TestUtils.createOnePercentRakeSettings;
import static com.cubeia.poker.variant.PokerVariant.TELESINA;
import static com.cubeia.poker.variant.PokerVariant.TEXAS_HOLDEM;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GameStateInitialization {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createGameTypeByVariant() {
        // PokerState state = new PokerState();
        GameType gameType = GameTypeFactory.createGameType(TELESINA);
        assertThat(gameType, instanceOf(Telesina.class));
        gameType = GameTypeFactory.createGameType(TEXAS_HOLDEM);
        assertThat(gameType, instanceOf(TexasHoldem.class));
    }

    @Test
    public void init() {
        TimingProfile timing = Mockito.mock(TimingProfile.class);
        BigDecimal anteLevel = new BigDecimal(1234);
        BlindsLevel level = new BlindsLevel(anteLevel, anteLevel.multiply(new BigDecimal(2)), anteLevel);
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(level, betStrategy, new BigDecimal(100), new BigDecimal(1000), timing, 6, createOnePercentRakeSettings(), new Currency("EUR",2), null);
        PokerState state = new PokerState();
        GameType gt = GameTypeFactory.createGameType(TELESINA);
        state.init(gt, settings);
        assertThat(state.getAnteLevel(), is(anteLevel));
        assertThat(state.getTimingProfile(), is(timing));
        assertThat(state.getTableSize(), is(6));
    }
}
