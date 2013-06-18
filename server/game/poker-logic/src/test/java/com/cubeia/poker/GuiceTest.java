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
import com.cubeia.poker.timing.TimingFactory;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.GameTypes;
import com.cubeia.poker.variant.PokerVariant;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class GuiceTest extends TestCase {

    protected Injector injector;

    protected MockServerAdapter mockServerAdapter;

    protected PokerState state;

    protected PokerVariant variant = PokerVariant.TEXAS_HOLDEM;

    protected Random rng = new Random();

    /**
     * Defaults to 10 seconds
     */
    protected long sitoutTimeLimitMilliseconds = 10000;

    @Override
    protected void setUp() throws Exception {
        List<Module> list = new LinkedList<Module>();
        list.add(new PokerGuiceModule());
        injector = Guice.createInjector(list);
        setupDefaultGame();
    }

    protected void setupDefaultGame() {
        mockServerAdapter = new MockServerAdapter();
        mockServerAdapter.random = rng;
        state = injector.getInstance(PokerState.class);
        GameType gameType = GameTypes.createTexasHoldem();
        state.init(gameType, createPokerSettings(new BigDecimal(100)));
        state.setServerAdapter(mockServerAdapter);
    }

    protected PokerSettings createPokerSettings(BigDecimal anteLevel) {
        BlindsLevel blinds = new BlindsLevel(anteLevel.divide(new BigDecimal(2)), anteLevel, anteLevel);
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(blinds, betStrategy, new BigDecimal(1000), new BigDecimal(10000),
                TimingFactory.getRegistry().getTimingProfile("MINIMUM_DELAY"), 6, TestUtils.createZeroRakeSettings(), new Currency("EUR",2), null);

        settings.setSitoutTimeLimitMilliseconds(sitoutTimeLimitMilliseconds);
        return settings;
    }
}
