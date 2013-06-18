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

package com.cubeia.games.poker.tournament.configuration.provider;

import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.dao.TournamentConfigurationDao;
import com.google.inject.Inject;

import java.util.Collection;

public class RealTournamentScheduleProvider implements TournamentScheduleProvider {

    private TournamentConfigurationDao dao;

    @Inject
    public RealTournamentScheduleProvider(TournamentConfigurationDao dao) {
        this.dao = dao;
    }

    @Override
    public Collection<ScheduledTournamentConfiguration> getTournamentSchedule() {
        return dao.getScheduledTournamentConfigurations();
    }

    @Override
    public ScheduledTournamentConfiguration getScheduledTournamentConfiguration(int id) {
        return dao.getScheduledTournamentConfiguration(id);
    }

    @Override
    public SitAndGoConfiguration getSitAndGoTournamentConfiguration(int id) {
        return dao.getSitAndGoConfiguration(id);
    }
}
