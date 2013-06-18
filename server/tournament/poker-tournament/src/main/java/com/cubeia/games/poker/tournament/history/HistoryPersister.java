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

package com.cubeia.games.poker.tournament.history;

import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Set;

public class HistoryPersister {

    private static final Logger log = Logger.getLogger(HistoryPersister.class);

    private String historicId;

    private TournamentHistoryPersistenceService storageService;

    private SystemTime dateFetcher;

    public HistoryPersister(String historicId, TournamentHistoryPersistenceService storageService, SystemTime dateFetcher) {
        this.historicId = historicId;
        this.dateFetcher = dateFetcher;
        if (storageService == null) {
            log.warn("No tournament history service available, logging to file.");
            this.storageService = new LoggingHistoryPersister();
        } else {
            this.storageService = storageService;
        }
    }

    public void playerOut(int playerId, int position, BigDecimal payout) {
        storageService.playerOut(playerId, position, payout, historicId, dateFetcher.now());
    }

    public void playerMoved(int playerId, int tableId) {
        storageService.playerMoved(playerId, tableId, historicId, dateFetcher.now());
    }

    public void setTournamentSessionId(String sessionId) {
        storageService.setTournamentSessionId(sessionId, historicId);
    }

    public void statusChanged(String status) {
        storageService.statusChanged(status, historicId, dateFetcher.now());
    }

    public void tournamentStarted(String name) {
        storageService.setStartTime(historicId, dateFetcher.now());
        storageService.setName(historicId, name);
    }

    public void tournamentFinished() {
        storageService.setEndTime(historicId, dateFetcher.now());
    }

    public void blindsIncreased(Level level) {
        storageService.blindsUpdated(historicId, level.getAnteAmount(), level.getSmallBlindAmount(), level.getBigBlindAmount(), dateFetcher.now());
    }

    public void addTable(String externalTableId) {
        storageService.addTable(historicId, externalTableId);
    }

    public void playerRegistered(HistoricPlayer player) {
        storageService.playerRegistered(historicId, player, dateFetcher.now());
    }

    public void playerReRegistered(int playerId) {
        storageService.playerReRegistered(historicId, playerId, dateFetcher.now());
    }

    public void playerUnregistered(int playerId) {
        storageService.playerUnregistered(historicId, playerId, dateFetcher.now());
    }

    public void playerFailedUnregistering(int playerId, String message) {
        storageService.playerFailedUnregistering(historicId, playerId, message, dateFetcher.now());
    }

    public void playerFailedOpeningSession(int playerId, String message) {
        storageService.playerFailedOpeningSession(historicId, playerId, message, dateFetcher.now());
    }

    public void playerOpenedSession(int playerId, String integrationSessionId) {
        storageService.playerOpenedSession(historicId, playerId, integrationSessionId, dateFetcher.now());
    }

    public void rebuysRequested(Set<Integer> playerIds) {
        storageService.rebuysRequested(historicId, playerIds, dateFetcher.now());
    }

    public void rebuyPerformed(int playerId) {
        storageService.rebuyPerformed(historicId, playerId, dateFetcher.now());
    }

    public void rebuyDeclined(int playerId) {
        storageService.rebuyDeclined(historicId, playerId, dateFetcher.now());
    }

    public void rebuyFailed(int playerId) {
        storageService.rebuyFailed(historicId, playerId, dateFetcher.now());
    }

    public void addOnPerformed(int playerId) {
        storageService.addOnPerformed(historicId, playerId, dateFetcher.now());
    }

    public void addOnFailed(int playerId) {
        storageService.addOnFailed(historicId, playerId, dateFetcher.now());
    }

    public void rebuyPeriodFinished() {
        storageService.rebuyPeriodFinished(historicId, dateFetcher.now());
    }

    public void addOnPeriodStarted() {
        storageService.addOnPeriodStarted(historicId, dateFetcher.now());
    }

    public void addOnPeriodFinished() {
        storageService.addOnPeriodFinished(historicId, dateFetcher.now());
    }
}
