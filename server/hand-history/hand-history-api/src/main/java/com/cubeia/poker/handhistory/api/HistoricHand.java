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

package com.cubeia.poker.handhistory.api;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class HistoricHand implements Serializable {

    private static final long serialVersionUID = -6540659414320234750L;

    @Id
    private String id;
    private Table table;

    private long startTime;
    private long endTime;

    private DeckInfo deckInfo;
    private Results results;

    private List<HandHistoryEvent> events = new ArrayList<HandHistoryEvent>();
    private List<Player> seats = new ArrayList<Player>(6);

    public HistoricHand() {
    }

    public HistoricHand(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public DeckInfo getDeckInfo() {
        return deckInfo;
    }

    public void setDeckInfo(DeckInfo deckInfo) {
        this.deckInfo = deckInfo;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public List<HandHistoryEvent> getEvents() {
        return events;
    }

    public void setEvents(List<HandHistoryEvent> events) {
        this.events = events;
    }

    public List<Player> getSeats() {
        return seats;
    }

    public void setSeats(List<Player> seats) {
        this.seats = seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoricHand that = (HistoricHand) o;

        if (endTime != that.endTime) return false;
        if (startTime != that.startTime) return false;
        if (deckInfo != null ? !deckInfo.equals(that.deckInfo) : that.deckInfo != null) return false;
        if (events != null ? !events.equals(that.events) : that.events != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (results != null ? !results.equals(that.results) : that.results != null) return false;
        if (seats != null ? !seats.equals(that.seats) : that.seats != null) return false;
        if (table != null ? !table.equals(that.table) : that.table != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (table != null ? table.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        result = 31 * result + (deckInfo != null ? deckInfo.hashCode() : 0);
        result = 31 * result + (results != null ? results.hashCode() : 0);
        result = 31 * result + (events != null ? events.hashCode() : 0);
        result = 31 * result + (seats != null ? seats.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HistoricHand{" +
                "id='" + id + '\'' +
                ", table=" + table +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", deckInfo=" + deckInfo +
                ", results=" + results +
                ", events=" + events +
                ", seats=" + seats +
                '}';
    }
}
