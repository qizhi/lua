/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.configuration.provider.mock;

import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MockTournamentScheduleProviderTest {

    @Test
    public void testPayoutsNotNull() {
        Collection<ScheduledTournamentConfiguration> schedule = new MockTournamentScheduleProvider().getTournamentSchedule();
        ScheduledTournamentConfiguration firstTournament = schedule.iterator().next();
        
        System.out.println("First tournament: "+firstTournament.getConfiguration());
        
		assertThat(firstTournament.getConfiguration().getPayoutStructure(), notNullValue());
    }
}
