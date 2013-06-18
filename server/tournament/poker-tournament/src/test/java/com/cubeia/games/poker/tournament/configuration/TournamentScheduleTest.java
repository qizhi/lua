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

package com.cubeia.games.poker.tournament.configuration;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class TournamentScheduleTest {

    private TimeZone originalTimeZone;

    @Before
    public void setup() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
    }

    @After
    public void after() {
        TimeZone.setDefault(originalTimeZone);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(originalTimeZone));
    }

    @Test
    public void testNextAnnounceTime() {
        Date startDate = new DateTime(2011, 7, 5, 9, 0, 0).toDate();
        Date endDate = new DateTime(2012, 7, 5, 9, 0, 0).toDate();
        TournamentSchedule tournamentSchedule = new TournamentSchedule(startDate, endDate, "0 30 14 * * ?", 10, 20, 30);

        DateTime nextAnnounceTime = tournamentSchedule.getNextAnnounceTime(new DateTime(2012, 6, 2, 9, 0, 0));
        assertEquals(new DateTime(2012, 6, 2, 14, 0, 0), nextAnnounceTime);
    }

    @Test
    public void testNoMoreTournamentsAfterEndDate() {
        Date start = new DateTime(2012, 6, 5, 9, 0, 0).toDate();
        Date end = new DateTime(2012, 7, 5, 9, 0, 0).toDate();
        TournamentSchedule tournamentSchedule = new TournamentSchedule(start, end, "0 30 14 * * ?", 10, 20, 30);

        DateTime nextAnnounceTime = tournamentSchedule.getNextAnnounceTime(new DateTime(2012, 7, 9, 9, 0, 0));
        assertNull("Should be null, but was " + nextAnnounceTime, nextAnnounceTime);
    }

    @Test
    public void test10MinuteSchedule() {
        Date start = new DateTime(2011, 7, 5, 9, 0, 0).toDate();
        Date end = new DateTime(2013, 7, 5, 9, 0, 0).toDate();
        TournamentSchedule tournamentSchedule = new TournamentSchedule(start, end, "0 */10 * * * ?", 3, 5, 5);

        DateTime nextAnnounceTime = tournamentSchedule.getNextStartTime(new DateTime(2012, 7, 9, 15, 3, 0));
        assertEquals(new DateTime(2012, 7, 9, 15, 10, 0), nextAnnounceTime);
    }
}
