package com.cubeia.poker.tournament.history.api;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.*;

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

@Entity
public class HistoricTournament implements Serializable {

    /** This is the unique id of this historic tournament. */
    @Id
    private ObjectId id;

    /** This is the id of the tournament in Firebase, might not be unique. */
    private int tournamentId;

    /** This is the id of the template, from which this tournament was spawned. */
    private int tournamentTemplateId;

    private String tournamentName;

    private long startTime;

    private long endTime;

    private List<TournamentEvent> events = new ArrayList<TournamentEvent>();

    private List<PlayerPosition> positions = new ArrayList<PlayerPosition>();

    private List<String> tables = new ArrayList<String>();

    /** The playerIds of all registered players. Will be used for resurrecting tournaments. */
    private Set<HistoricPlayer> registeredPlayers = new HashSet<HistoricPlayer>();

    private boolean sitAndGo;

    private Date scheduledStartTime;

    private String status;

    private String tournamentSessionId;

    public HistoricTournament() {

    }

    public String getId() {
        return id.toString();
    }

    public String getTournamentSessionId() {
        return tournamentSessionId;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getTournamentTemplateId() {
        return tournamentTemplateId;
    }

    public void setTournamentSessionId(String tournamentSessionId) {
        this.tournamentSessionId = tournamentSessionId;
    }

    public void setTournamentTemplateId(int tournamentTemplateId) {
        this.tournamentTemplateId = tournamentTemplateId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
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

    public List<TournamentEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TournamentEvent> events) {
        this.events = events;
    }

    public List<PlayerPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<PlayerPosition> positions) {
        this.positions = positions;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public Set<HistoricPlayer> getRegisteredPlayers() {
        return registeredPlayers;
    }

    public boolean isSitAndGo() {
        return sitAndGo;
    }

    public void setSitAndGo(boolean sitAndGo) {
        this.sitAndGo = sitAndGo;
    }

    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
