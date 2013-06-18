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

package com.cubeia.games.poker.tournament.configuration.lifecycle;

import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import org.joda.time.DateTime;

import java.io.Serializable;

public interface TournamentLifeCycle extends Serializable {

    public boolean shouldStartTournament(DateTime now, int nrRegistered, int capacity);

    public boolean shouldCancelTournament(DateTime now, int nrRegistered, int capacity);

    public boolean shouldScheduleRegistrationOpening(PokerTournamentStatus status, DateTime now);

    public boolean shouldScheduleTournamentStart(PokerTournamentStatus status, DateTime now);

    public boolean shouldOpenRegistration(DateTime now);

    public long getTimeToRegistrationStart(DateTime now);

    public long getTimeToTournamentStart(DateTime now);

    public DateTime getStartTime();

    public DateTime getOpenRegistrationTime();
}
