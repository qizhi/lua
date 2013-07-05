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

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentInstance;
import com.cubeia.games.poker.tournament.configuration.lifecycle.ScheduledTournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;

import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.IDENTIFIER;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.REGISTRATION_OPENING_TIME;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.SIT_AND_GO;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.START_TIME;

public class ScheduledTournamentCreationParticipant extends PokerTournamentCreationParticipant {

    private ScheduledTournamentInstance instanceConfiguration;


    public ScheduledTournamentCreationParticipant(ScheduledTournamentInstance config, TournamentHistoryPersistenceService storageService,
                                                  SystemTime dateFetcher, CashGamesBackendService cashGamesBackendService) {
        super(config.getConfiguration(), storageService, dateFetcher,cashGamesBackendService);
        instanceConfiguration = config;
    }

    @Override
    protected int getMinutesVisibleAfterFinished() {
        return instanceConfiguration.getSchedule().getMinutesVisibleAfterFinished();
    }

    @Override
    protected TournamentLifeCycle getTournamentLifeCycle() {
        return new ScheduledTournamentLifeCycle(instanceConfiguration.getStartTime(), instanceConfiguration.getOpenRegistrationTime());
    }

    @Override
    protected void tournamentCreated(MTTStateSupport stateSupport, PokerTournamentState pokerState, LobbyAttributeAccessor lobbyAttributeAccessor) {
        super.tournamentCreated(stateSupport, pokerState, lobbyAttributeAccessor);
        setStatus(pokerState, lobbyAttributeAccessor, PokerTournamentStatus.ANNOUNCED);
        lobbyAttributeAccessor.setStringAttribute(IDENTIFIER.name(), instanceConfiguration.getIdentifier());
        lobbyAttributeAccessor.setStringAttribute(START_TIME.name(), "" + pokerState.getStartTime().getMillis());
        lobbyAttributeAccessor.setStringAttribute(REGISTRATION_OPENING_TIME.name(), ""+pokerState.getRegistrationStartDate().getMillis());
        setScheduledStartTime(pokerState);
    }

    private void setScheduledStartTime(PokerTournamentState pokerState) {
        if (storageService != null) {
            storageService.setScheduledStartTime(pokerState.getHistoricId(), instanceConfiguration.getStartTime().toDate());
        }
    }

    @Override
    protected boolean isSitAndGo() {
        return false;
    }

    @Override
    protected int getConfigurationTemplateId() {
        return instanceConfiguration.getTemplateId();
    }

    @Override
    protected String getType() {
        return "scheduled";
    }

    public ScheduledTournamentInstance getInstance() {
        return instanceConfiguration;
    }
}
