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

package com.cubeia.poker.tournament.history.storage.api;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.api.HistoricTournament;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface TournamentHistoryPersistenceService extends Contract {

    void addTable(String historicId, String externalTableId);

    void blindsUpdated(String historicId, BigDecimal ante, BigDecimal smallBlind, BigDecimal bigBlind, long now);

    /**
     * Creates a new historic tournament in preparation for storing information about a new tournament.
     *
     * @return the id of the new historic tournament
     */
    String createHistoricTournament(String name, int id, int templateId, boolean isSitAndGo);

    List<HistoricTournament> findTournamentsToResurrect();

    HistoricTournament getHistoricTournament(String id);

    void playerFailedOpeningSession(String historicId, int playerId, String message, long now);

    void playerFailedUnregistering(String historicId, int playerId, String message, long now);

    void playerMoved(int playerId, int tableId, String historicId, long now);

    void playerOpenedSession(String historicId, int playerId, String sessionId, long now);

    void playerOut(int playerId, int position, BigDecimal payout, String historicId, long now);

    void playerReRegistered(String historicId, int playerId, long now);

    void playerRegistered(String historicId, HistoricPlayer player, long now);

    void playerUnregistered(String historicId, int playerId, long now);

    void setEndTime(String historicId, long date);

    void setName(String historicId, String name);

    void setScheduledStartTime(String historicId, Date startTime);

    void setTournamentSessionId(String sessionId, String historicId);

    void setStartTime(String historicId, long date);

    void statusChanged(String status, String historicId, long now);

    void rebuysRequested(String historicId, Set<Integer> playerIds, long now);

    void rebuyPerformed(String historicId, int playerId, long now);

    void rebuyDeclined(String historicId, int playerId, long now);

    void rebuyFailed(String historicId, int playerId, long now);

    void addOnPerformed(String historicId, int playerId, long now);

    void addOnFailed(String historicId, int playerId, long now);

    void rebuyPeriodFinished(String historicId, long now);

    void addOnPeriodStarted(String historicId, long now);

    void addOnPeriodFinished(String historicId, long now);
}
