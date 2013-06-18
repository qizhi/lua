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

import com.cubeia.games.poker.tournament.configuration.provider.RealSitAndGoConfigurationProvider;
import com.cubeia.games.poker.tournament.configuration.provider.RealTournamentScheduleProvider;
import com.cubeia.games.poker.tournament.configuration.provider.SitAndGoConfigurationProvider;
import com.cubeia.games.poker.tournament.configuration.provider.TournamentScheduleProvider;
import com.cubeia.games.poker.tournament.configuration.dao.TournamentConfigurationDao;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class ActivatorModule extends AbstractModule {

    public void configure() {
        install(new JpaPersistModule("pokerPersistenceUnit"));
        bind(TournamentConfigurationDao.class);
        bind(TournamentScheduleProvider.class).to(RealTournamentScheduleProvider.class);
        bind(SitAndGoConfigurationProvider.class).to(RealSitAndGoConfigurationProvider.class);
        bind(PokerActivator.class).to(TournamentScanner.class);
    }

}