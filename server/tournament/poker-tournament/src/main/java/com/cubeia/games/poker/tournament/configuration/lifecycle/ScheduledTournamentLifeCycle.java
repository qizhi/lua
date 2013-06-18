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

public class ScheduledTournamentLifeCycle implements TournamentLifeCycle {

    private DateTime startTime;

    private DateTime openRegistrationTime;

    public ScheduledTournamentLifeCycle(DateTime startTime, DateTime openRegistrationTime) {
        this.startTime = startTime;
        this.openRegistrationTime = openRegistrationTime;
    }

    @Override
    public boolean shouldStartTournament(DateTime now, int nrRegistered, int capacity) {
        return now.isAfter(startTime);
    }

    @Override
    public boolean shouldCancelTournament(DateTime now, int nrRegistered, int capacity) {
        return now.isAfter(startTime) && nrRegistered < capacity;
    }

    @Override
    public boolean shouldOpenRegistration(DateTime now) {
        return now.isAfter(openRegistrationTime);
    }

    @Override
    public boolean shouldScheduleRegistrationOpening(PokerTournamentStatus status, DateTime now) {
        return status == PokerTournamentStatus.ANNOUNCED && now.isBefore(openRegistrationTime);
    }

    @Override
    public boolean shouldScheduleTournamentStart(PokerTournamentStatus status, DateTime now) {
        return status == PokerTournamentStatus.REGISTERING && now.isAfter(openRegistrationTime);
    }

    @Override
    public long getTimeToTournamentStart(DateTime now) {
        long timeToStart = startTime.toDate().getTime() - now.toDate().getTime();
        // If the registration should already have opened, schedule it in one second.
        return (timeToStart <= 1000) ? 1000 : timeToStart;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    @Override
    public DateTime getOpenRegistrationTime() {
        return this.openRegistrationTime;
    }


    @Override
    public long getTimeToRegistrationStart(DateTime now) {
        long timeToRegistrationOpening = openRegistrationTime.toDate().getTime() - now.toDate().getTime();
        // If the registration should already have opened, schedule it in one second.
        return (timeToRegistrationOpening <= 1000) ? 1000 : timeToRegistrationOpening;
    }
}
