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

package com.cubeia.poker.tournament.history.storage.impl;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.games.poker.common.mongo.BigDecimalConverter;
import com.cubeia.games.poker.common.mongo.DatabaseStorageConfiguration;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.api.PlayerPosition;
import com.cubeia.poker.tournament.history.api.TournamentEvent;
import com.cubeia.poker.tournament.history.dao.HistoricTournamentDao;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DatabaseStorageService implements TournamentHistoryPersistenceService, Service {

    private static final Logger log = Logger.getLogger(DatabaseStorageService.class);

    private HistoricTournamentDao dao;

    @Override
    public String createHistoricTournament(String name, int id, int templateId, boolean isSitAndGo) {
        return dao.createHistoricTournament(name, id, templateId, isSitAndGo);
    }

    @Override
    public HistoricTournament getHistoricTournament(String id) {
        return dao.getHistoricTournament(id);
    }

    @Override
    public void playerOut(int playerId, int position, BigDecimal payout, String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "player " + playerId + " out", "" + position));
        addPlayerPosition(historicId, playerId, position, payout);
    }

    @Override
    public void playerMoved(int playerId, int tableId, String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "player " + playerId + " moved", "" + tableId));
    }

    @Override
    public void statusChanged(String status, String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "status changed", status));
        dao.setStatus(historicId, status);
    }

    @Override
    public void rebuysRequested(String historicId, Set<Integer> playerIds, long now) {
        addEvent(historicId, new TournamentEvent(now, "Requested rebuys from players", Arrays.toString(playerIds.toArray())));
    }

    @Override
    public void rebuyPerformed(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Rebuy performed", "" + playerId));
    }

    @Override
    public void rebuyDeclined(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Rebuy declined", "" + playerId));
    }

    @Override
    public void rebuyFailed(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Rebuy failed", "" + playerId));
    }

    @Override
    public void addOnPerformed(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Add-on performed", "" + playerId));
    }

    @Override
    public void addOnFailed(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Add-on failed", "" + playerId));
    }

    @Override
    public void rebuyPeriodFinished(String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Rebuy period finished"));
    }

    @Override
    public void addOnPeriodStarted(String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Add-on period started"));
    }

    @Override
    public void addOnPeriodFinished(String historicId, long now) {
        addEvent(historicId, new TournamentEvent(now, "Add-on period finished"));
    }

    @Override
    public void blindsUpdated(String historicId, BigDecimal ante, BigDecimal smallBlind, BigDecimal bigBlind, long now) {
        addEvent(historicId, new TournamentEvent(now, "blinds updated", ante + "/" + smallBlind + "/" + bigBlind));
    }

    @Override
    public void setStartTime(String historicId, long now) {
        dao.setStartTime(historicId, now);
    }

    @Override
    public void setEndTime(String historicId, long now) {
        dao.setEndTime(historicId, now);
    }

    @Override
    public void setName(String historicId, String name) {
        dao.setName(historicId, name);
    }

    @Override
    public void addTable(String historicId, String externalTableId) {
        dao.addTable(historicId, externalTableId);
    }

    @Override
    public void playerRegistered(String historicId, HistoricPlayer player, long now) {
        addEvent(historicId, new TournamentEvent(now, "player registered", String.valueOf(player.getId())));
        dao.addRegisteredPlayer(historicId, player);
    }

    @Override
    public void playerReRegistered(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "player re-registered", String.valueOf(playerId)));
    }

    @Override
    public void playerUnregistered(String historicId, int playerId, long now) {
        addEvent(historicId, new TournamentEvent(now, "player un-registered", String.valueOf(playerId)));
        dao.removeRegisteredPlayer(historicId, playerId);
        removeRegisteredPlayer(historicId, playerId);
    }

    @Override
    public void playerFailedUnregistering(String historicId, int playerId, String message, long now) {
        addEvent(historicId, new TournamentEvent(now, "player failed un-registering: " + message, String.valueOf(playerId)));
    }

    @Override
    public void playerOpenedSession(String historicId, int playerId, String sessionId, long now) {
        addEvent(historicId, new TournamentEvent(now, "player opened session: " + sessionId, String.valueOf(playerId)));
    }

    @Override
    public void playerFailedOpeningSession(String historicId, int playerId, String message, long now) {
        addEvent(historicId, new TournamentEvent(now, "player failed opening session: " + message, String.valueOf(playerId)));
    }

    @Override
    public void setScheduledStartTime(String historicId, Date startTime) {
        dao.setScheduledStartTime(historicId, startTime);
    }

    @Override
    public void setTournamentSessionId(String sessionId, String historicId) {
        dao.setTournamentSessionId(sessionId, historicId);
    }

    @Override
    public List<HistoricTournament> findTournamentsToResurrect() {
        return dao.findTournamentsToResurrect();
    }

    private void removeRegisteredPlayer(String historicId, int playerId) {
        dao.removeRegisteredPlayer(historicId, playerId);
    }

    private void addEvent(String historicId, TournamentEvent event) {
        dao.addEvent(historicId, event);
    }

    private void addPlayerPosition(String historicId, int playerId, int position, BigDecimal payout) {
        dao.addPlayerPosition(historicId, new PlayerPosition(playerId, position, payout));
    }

    protected DatabaseStorageConfiguration getConfiguration(ServiceContext context) {
        return new DatabaseStorageConfiguration().load(context.getServerConfigDirectory().getAbsolutePath());
    }

    @Override
    public void init(ServiceContext context) throws SystemException {
        DatabaseStorageConfiguration configuration = getConfiguration(context);
        try {
            Mongo mongo = new Mongo(configuration.getHost(), configuration.getPort());

            Morphia morphia = new Morphia();
            morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);
            dao = new HistoricTournamentDao(morphia.createDatastore(mongo, configuration.getDatabaseName()));
        } catch (UnknownHostException e) {
            throw new SystemException("Failed initializing datasource", e);
        }
    }

    @Override
    public void start() {
        log.debug("Starting DatabaseStorageService.");
    }

    @Override
    public void stop() {
        log.debug("Stopping DatabaseStorageService.");
    }

    @Override
    public void destroy() {

    }

}
