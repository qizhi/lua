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

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

public class NullDatabaseStorageService implements TournamentHistoryPersistenceService {

    @Override
    public void addTable(String historicId, String externalTableId) {

    }

    @Override
    public void blindsUpdated(String historicId, BigDecimal ante, BigDecimal smallBlind, BigDecimal bigBlind, long now) {

    }

    @Override
    public String createHistoricTournament(String name, int id, int templateId, boolean isSitAndGo) {
        return "mockId";
    }

    @Override
    public List<HistoricTournament> findTournamentsToResurrect() {
        return emptyList();
    }

    @Override
    public HistoricTournament getHistoricTournament(String id) {
        return null;
    }

    @Override
    public void playerFailedOpeningSession(String historicId, int playerId, String message, long now) {

    }

    @Override
    public void playerFailedUnregistering(String historicId, int playerId, String message, long now) {

    }

    @Override
    public void playerMoved(int playerId, int tableId, String historicId, long now) {

    }

    @Override
    public void playerOpenedSession(String historicId, int playerId, String sessionId, long now) {

    }

    @Override
    public void playerOut(int playerId, int position, BigDecimal payout, String historicId, long now) {

    }

    @Override
    public void playerReRegistered(String historicId, int playerId, long now) {

    }

    @Override
    public void playerRegistered(String historicId, HistoricPlayer player, long now) {

    }

    @Override
    public void playerUnregistered(String historicId, int playerId, long now) {

    }

    @Override
    public void setEndTime(String historicId, long date) {

    }

    @Override
    public void setName(String historicId, String name) {

    }

    @Override
    public void setScheduledStartTime(String historicId, Date startTime) {

    }

    @Override
    public void setTournamentSessionId(String sessionId, String historicId) {

    }

    @Override
    public void setStartTime(String historicId, long date) {

    }

    @Override
    public void statusChanged(String status, String historicId, long now) {

    }

    @Override
    public void rebuysRequested(String historicId, Set<Integer> playerIds, long now) {

    }

    @Override
    public void rebuyPerformed(String historicId, int playerId, long now) {

    }

    @Override
    public void rebuyDeclined(String historicId, int playerId, long now) {

    }

    @Override
    public void rebuyFailed(String historicId, int playerId, long now) {

    }

    @Override
    public void addOnPerformed(String historicId, int playerId, long now) {

    }

    @Override
    public void addOnFailed(String historicId, int playerId, long now) {

    }

    @Override
    public void rebuyPeriodFinished(String historicId, long now) {

    }

    @Override
    public void addOnPeriodStarted(String historicId, long now) {

    }

    @Override
    public void addOnPeriodFinished(String historicId, long now) {

    }

}
