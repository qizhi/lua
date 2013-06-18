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

package com.cubeia.games.poker.activator;

import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingFactory;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.cubeia.poker.settings.RakeSettings.createDefaultRakeSettings;
import static com.cubeia.poker.variant.PokerVariant.TELESINA;
import static com.cubeia.poker.variant.PokerVariant.TEXAS_HOLDEM;

@Singleton
public class SimpleTableConfigTemplateProvider implements TableConfigTemplateProvider {

    @Override
    public List<TableConfigTemplate> getTemplates() {
        TableConfigTemplate texasNoLimit = new TableConfigTemplate();
        texasNoLimit.setId(0);
        texasNoLimit.setSmallBlind(bd(0.5));
        texasNoLimit.setBigBlind(bd(1));
        texasNoLimit.setMinBuyIn(bd(10));
        texasNoLimit.setMaxBuyIn(bd(100));
        texasNoLimit.setSeats(10);
        texasNoLimit.setVariant(TEXAS_HOLDEM);
        texasNoLimit.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        texasNoLimit.setBetStrategy(BetStrategyType.NO_LIMIT);
        texasNoLimit.setTTL(60000);
        texasNoLimit.setMinEmptyTables(5);
        texasNoLimit.setMinTables(10);
        texasNoLimit.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        texasNoLimit.setCurrency("EUR");

        TableConfigTemplate texasNoLimit2Plrs = new TableConfigTemplate();
        texasNoLimit2Plrs.setId(2);
        texasNoLimit2Plrs.setSmallBlind(bd(50));
        texasNoLimit2Plrs.setBigBlind(bd(100));
        texasNoLimit2Plrs.setMinBuyIn(bd(1000));
        texasNoLimit2Plrs.setMaxBuyIn(bd(10000));
        texasNoLimit2Plrs.setSeats(2);
        texasNoLimit2Plrs.setVariant(TEXAS_HOLDEM);
        texasNoLimit2Plrs.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        texasNoLimit2Plrs.setBetStrategy(BetStrategyType.NO_LIMIT);
        texasNoLimit2Plrs.setTTL(60000);
        texasNoLimit2Plrs.setMinEmptyTables(2);
        texasNoLimit2Plrs.setMinTables(4);
        texasNoLimit2Plrs.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        texasNoLimit2Plrs.setCurrency("EUR");

        TableConfigTemplate texasNoLimit6Plrs = new TableConfigTemplate();
        texasNoLimit6Plrs.setId(3);
        texasNoLimit6Plrs.setSmallBlind(bd(50));
        texasNoLimit6Plrs.setBigBlind(bd(100));
        texasNoLimit6Plrs.setMinBuyIn(bd(1000));
        texasNoLimit6Plrs.setMaxBuyIn(bd(10000));
        texasNoLimit6Plrs.setSeats(6);
        texasNoLimit6Plrs.setVariant(TEXAS_HOLDEM);
        texasNoLimit6Plrs.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        texasNoLimit6Plrs.setBetStrategy(BetStrategyType.NO_LIMIT);
        texasNoLimit6Plrs.setTTL(60000);
        texasNoLimit6Plrs.setMinEmptyTables(2);
        texasNoLimit6Plrs.setMinTables(4);
        texasNoLimit6Plrs.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        texasNoLimit6Plrs.setCurrency("EUR");

        TableConfigTemplate texasNoLimit5Plrs = new TableConfigTemplate();
        texasNoLimit5Plrs.setId(4);
        texasNoLimit5Plrs.setSmallBlind(bd(50));
        texasNoLimit5Plrs.setBigBlind(bd(100));
        texasNoLimit5Plrs.setMinBuyIn(bd(1000));
        texasNoLimit5Plrs.setMaxBuyIn(bd(10000));
        texasNoLimit5Plrs.setSeats(5);
        texasNoLimit5Plrs.setVariant(TEXAS_HOLDEM);
        texasNoLimit5Plrs.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        texasNoLimit5Plrs.setBetStrategy(BetStrategyType.NO_LIMIT);
        texasNoLimit5Plrs.setTTL(60000);
        texasNoLimit5Plrs.setMinEmptyTables(2);
        texasNoLimit5Plrs.setMinTables(4);
        texasNoLimit5Plrs.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        texasNoLimit5Plrs.setCurrency("EUR");


        TableConfigTemplate texasFixedLimit = new TableConfigTemplate();
        texasFixedLimit.setId(5);
        texasFixedLimit.setSmallBlind(bd(50));
        texasFixedLimit.setBigBlind(bd(100));
        texasFixedLimit.setMinBuyIn(bd(1000));
        texasFixedLimit.setMaxBuyIn(bd(10000));
        texasFixedLimit.setSeats(10);
        texasFixedLimit.setVariant(TEXAS_HOLDEM);
        texasFixedLimit.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        texasFixedLimit.setBetStrategy(BetStrategyType.FIXED_LIMIT);
        texasFixedLimit.setTTL(60000);
        texasFixedLimit.setMinEmptyTables(5);
        texasFixedLimit.setMinTables(10);
        texasFixedLimit.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        texasFixedLimit.setCurrency("EUR");

        TableConfigTemplate telesina = new TableConfigTemplate();
        telesina.setId(6);
        telesina.setAnte(bd(100));
        telesina.setSmallBlind(bd(0));
        telesina.setBigBlind(bd(0));
        telesina.setMinBuyIn(bd(1000));
        telesina.setMaxBuyIn(bd(10000));
        telesina.setSeats(6);
        telesina.setVariant(TELESINA);
        telesina.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        telesina.setBetStrategy(BetStrategyType.NO_LIMIT);
        telesina.setTTL(60000);
        telesina.setMinEmptyTables(1);
        telesina.setMinTables(1);
        telesina.setRakeSettings(createDefaultRakeSettings(new BigDecimal(0.02)));
        telesina.setCurrency("EUR");

        return Arrays.asList(texasNoLimit, texasFixedLimit, texasNoLimit2Plrs,texasNoLimit5Plrs,texasNoLimit6Plrs,telesina);
    }


    private BigDecimal bd(double i) {
        return new BigDecimal(i);
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }
}
