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

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.common.time.DefaultSystemTime;
import com.cubeia.games.poker.tournament.activator.PokerActivator;
import com.cubeia.games.poker.tournament.activator.TournamentScanner;
import com.cubeia.games.poker.tournament.configuration.provider.SitAndGoConfigurationProvider;
import com.cubeia.games.poker.tournament.configuration.provider.TournamentScheduleProvider;
import com.cubeia.games.poker.tournament.configuration.provider.mock.MockSitAndGoConfigurationProvider;
import com.cubeia.games.poker.tournament.configuration.provider.mock.MockTournamentScheduleProvider;
import com.google.inject.AbstractModule;

public class MockActivatorModule extends AbstractModule {

    public void configure() {
        bind(TournamentScheduleProvider.class).to(MockTournamentScheduleProvider.class);
        bind(SitAndGoConfigurationProvider.class).to(MockSitAndGoConfigurationProvider.class);
        bind(SystemTime.class).to(DefaultSystemTime.class);
        bind(PokerActivator.class).to(TournamentScanner.class);
    }

}
