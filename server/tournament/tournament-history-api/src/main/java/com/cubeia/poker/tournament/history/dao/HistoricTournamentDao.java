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

package com.cubeia.poker.tournament.history.dao;

import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.api.PlayerPosition;
import com.cubeia.poker.tournament.history.api.TournamentEvent;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

public class HistoricTournamentDao {

    private Datastore datastore;

    public HistoricTournamentDao(Datastore datastore) {
        this.datastore = datastore;
    }

    public String createHistoricTournament(String name, int id, int templateId, boolean sitAndGo) {
        HistoricTournament historicTournament = new HistoricTournament();
        historicTournament.setTournamentName(name);
        historicTournament.setTournamentId(id);
        historicTournament.setTournamentTemplateId(templateId);
        historicTournament.setSitAndGo(sitAndGo);

        datastore.save(historicTournament);
        return historicTournament.getId();
    }

    public Key<HistoricTournament> createHistoricTournamentKey() {
        return datastore.save(new HistoricTournament());
    }

    public HistoricTournament getHistoricTournament(String historicId) {
        return createQuery(historicId).get();
    }

    public void setTournamentSessionId(String sessionId, String historicId) {
        setProperty(historicId, "tournamentSessionId", sessionId);
    }

    public void setStartTime(String historicId, long date) {
        setProperty(historicId, "startTime", date);
    }

    public void setEndTime(String historicId, long date) {
        setProperty(historicId, "endTime", date);
    }

    public void setName(String historicId, String name) {
        setProperty(historicId, "tournamentName", name);
    }

    public void addTable(String historicId, String externalTableId) {
        UpdateOperations<HistoricTournament> update = datastore.createUpdateOperations(HistoricTournament.class).add("tables", externalTableId);
        datastore.update(createQuery(historicId), update);
    }

    public void addRegisteredPlayer(String historicId, HistoricPlayer player) {
        addObjectToCollection(historicId, player, "registeredPlayers");
    }

    public void removeRegisteredPlayer(String historicId, int playerId) {
        removeObjectFromCollection(historicId, new HistoricPlayer(playerId, null, null), "registeredPlayers");
    }

    public void setStatus(String historicId, String status) {
        setProperty(historicId, "status", status);
    }

    public void store(Object object) {
        datastore.save(object);
    }
    
    public HistoricTournament getHistoricTournamentByKey(Key<HistoricTournament> id) {
        return datastore.getByKey(HistoricTournament.class, id);
    }

    public void setScheduledStartTime(String historicId, Date startTime) {
        setProperty(historicId, "scheduledStartTime", startTime);
    }

    public void addEvent(String historicId, TournamentEvent event) {
        addObjectToCollection(historicId, event, "events");
    }

    public void addPlayerPosition(String historicId, PlayerPosition playerPosition) {
        addObjectToCollection(historicId, playerPosition, "positions");
    }

    /**
     * Finds tournaments that need to be resurrected. A tournament needs to be resurrected if:
     *
     *  - It has been created but has not started (it's announced or registering).
     *
     * Note that once a tournament has started it needs to be resolved rather than resurrected.
     *
     * @return a list of tournaments that need to be resurrected
     */
    public List<HistoricTournament> findTournamentsToResurrect() {
        Query<HistoricTournament> query = datastore.createQuery(HistoricTournament.class);
        // Status should either be "ANNOUNCED | REGISTERING" or "RUNNING" but only if it's a sitAndGo.
        query.or(query.criteria("status").in(asList("ANNOUNCED", "REGISTERING")),
                 query.and(query.criteria("status").equal("RUNNING"), query.criteria("sitAndGo").equal(true)));
        return query.asList();
    }

    private void setProperty(String historicId, String property, Object value) {
        UpdateOperations<HistoricTournament> update = datastore.createUpdateOperations(HistoricTournament.class).set(property, value);
        datastore.update(createQuery(historicId), update);
    }

    private Query<HistoricTournament> createQuery(String historicId) {
        return datastore.createQuery(HistoricTournament.class).field("id").equal(new ObjectId(historicId));
    }

    private void addObjectToCollection(String historicId, Object object, String collection) {
        UpdateOperations<HistoricTournament> update = datastore.createUpdateOperations(HistoricTournament.class).add(collection, object);
        datastore.update(createQuery(historicId), update);
    }

    private void removeObjectFromCollection(String historicId, Object object, String collection) {
        UpdateOperations<HistoricTournament> update = datastore.createUpdateOperations(HistoricTournament.class).removeAll(collection, object);
        datastore.update(createQuery(historicId), update);
    }

}
