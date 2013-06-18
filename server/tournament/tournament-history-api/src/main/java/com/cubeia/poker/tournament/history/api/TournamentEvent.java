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

package com.cubeia.poker.tournament.history.api;

import com.google.code.morphia.annotations.Embedded;

import java.io.Serializable;
import java.util.Date;

@Embedded
public class TournamentEvent implements Serializable {

    private long timestamp;

    private String event;

    private String details;

    public TournamentEvent() {
    }

    public TournamentEvent(long timestamp, String event, String details) {
        this.timestamp = timestamp;
        this.event = event;
        this.details = details;
    }

    public TournamentEvent(long timestamp, String event) {
        this(timestamp, event, "");
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "TournamentEvent{" +
                "timestamp=" + timestamp +
                ", event='" + event + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
