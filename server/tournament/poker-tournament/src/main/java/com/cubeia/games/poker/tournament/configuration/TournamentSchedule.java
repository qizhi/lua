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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.Trigger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Entity
public class TournamentSchedule implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private static final Logger log = Logger.getLogger(TournamentSchedule.class);

    private Date startDate;

    private Date endDate;

    private String cronSchedule;

    private int minutesInAnnounced;

    private int minutesInRegistering;

    private int minutesVisibleAfterFinished;

    public TournamentSchedule() {

    }

    public TournamentSchedule(Date startDate, Date endDate, String cronSchedule, int minutesInAnnounced, int minutesInRegistering,
            int minutesVisibleAfterFinished) {
        log.trace("Created tournament schedule. Start date: " + startDate + " End date: " + endDate);
        this.startDate = new Date(startDate.getTime());
        this.endDate = new Date(endDate.getTime());
        this.cronSchedule = cronSchedule;
        this.minutesInAnnounced = minutesInAnnounced;
        this.minutesInRegistering = minutesInRegistering;
        this.minutesVisibleAfterFinished = minutesVisibleAfterFinished;
    }

    public int getMinutesInAnnounced() {
        return minutesInAnnounced;
    }

    public int getMinutesInRegistering() {
        return minutesInRegistering;
    }

    public int getMinutesVisibleAfterFinished() {
        return minutesVisibleAfterFinished;
    }

    public DateTime getNextAnnounceTime(DateTime now) {
        log.trace("Getting next announce time after " + now + ". Start date = " + getSchedule().getStartTime());
        DateTime nextStartTime = getNextStartTime(now);
        log.trace("Next startTime: " + nextStartTime);
        if (nextStartTime == null) {
            return null;
        } else {
            DateTime nextAnnounceTime = new DateTime(nextStartTime).minusMinutes(minutesInRegistering).minusMinutes(minutesInAnnounced);
            log.trace("Next announce time: " + nextAnnounceTime);
            return nextAnnounceTime;
        }
    }

    public DateTime getNextStartTime(DateTime now) {
        Date nextStartTime = getSchedule().getFireTimeAfter(now.toDate());
        if (nextStartTime == null) {
            return null;
        } else {
            return new DateTime(nextStartTime);
        }
    }

    public Trigger getSchedule() {
        return newTrigger().withSchedule(cronSchedule(cronSchedule)).startAt(startDate).endAt(endDate).build();
    }

    public void setMinutesInAnnounced(int minutesInAnnounced) {
        this.minutesInAnnounced = minutesInAnnounced;
    }

    public void setMinutesInRegistering(int minutesInRegistering) {
        this.minutesInRegistering = minutesInRegistering;
    }

    public void setMinutesVisibleAfterFinished(int minutesVisibleAfterFinished) {
        this.minutesVisibleAfterFinished = minutesVisibleAfterFinished;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public void setStartDate(Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public void setEndDate(Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }
}
