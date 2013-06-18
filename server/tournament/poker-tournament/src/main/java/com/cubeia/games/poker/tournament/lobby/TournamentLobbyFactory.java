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

package com.cubeia.games.poker.tournament.lobby;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.util.PacketSenderFactory;

import javax.inject.Inject;

public class TournamentLobbyFactory {

    @Inject
    private PacketSenderFactory sender;

    @Inject
    private SystemTime dateFetcher;

    public TournamentLobby create(MttInstance instance, MTTStateSupport stateSupport, PokerTournamentState pokerState, CashGamesBackendService backend) {
        return new TournamentLobby(sender.create(instance.getMttNotifier(), instance), dateFetcher, backend, stateSupport,  pokerState);
    }
}
