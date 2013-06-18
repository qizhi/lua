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

package com.cubeia.games.poker.tournament;

import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;

import java.io.Serializable;

public class PokerTournamentUtil implements Serializable {

    public PokerTournamentState getPokerState(MttInstance instance) {
        return getPokerState((MTTStateSupport) instance.getState());
    }

    public MTTStateSupport getStateSupport(MttInstance instance) {
        return (MTTStateSupport) instance.getState();
    }

    public PokerTournamentState getPokerState(MTTStateSupport state) {
        return ((PokerTournament) state.getState()).getPokerTournamentState();
    }

}
