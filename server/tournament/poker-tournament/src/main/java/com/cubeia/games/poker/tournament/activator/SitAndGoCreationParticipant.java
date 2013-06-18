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
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.lifecycle.SitAndGoLifeCycle;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;

public class SitAndGoCreationParticipant extends PokerTournamentCreationParticipant {

    private final SitAndGoConfiguration template;

    public SitAndGoCreationParticipant(SitAndGoConfiguration config, TournamentHistoryPersistenceService storageService,
                                       SystemTime dateFetcher, CashGamesBackendService cashGamesBackendService) {
        super(config.getConfiguration(), storageService, dateFetcher,cashGamesBackendService);
        this.template = config;
    }

    @Override
    protected int getMinutesVisibleAfterFinished() {
        return 1;
    }

    @Override
    protected TournamentLifeCycle getTournamentLifeCycle() {
        return new SitAndGoLifeCycle();
    }

    @Override
    protected void tournamentCreated(MTTStateSupport stateSupport, PokerTournamentState pokerState, LobbyAttributeAccessor lobbyAttributeAccessor) {
        super.tournamentCreated(stateSupport, pokerState, lobbyAttributeAccessor);
        // Sit and go tournaments start in registering mode.
        setStatus(pokerState, lobbyAttributeAccessor, PokerTournamentStatus.REGISTERING);
    }

    @Override
    protected boolean isSitAndGo() {
        return true;
    }

    @Override
    protected int getConfigurationTemplateId() {
        return template.getId();
    }

    @Override
    protected String getType() {
        return "sitandgo";
    }

}
