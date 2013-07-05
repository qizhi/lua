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

package com.cubeia.backend.cashgame.dto;

import com.cubeia.backend.cashgame.TournamentId;
import com.cubeia.games.poker.common.money.Money;

import java.io.Serializable;

public class OpenTournamentSessionRequest extends OpenSessionRequest implements Serializable {

    private static final long serialVersionUID = 74126213720786784L;

    public final TournamentId tournamentId;

    public static final String TOURNAMENT_ACCOUNT = "TOURNAMENT_ACCOUNT";

    public OpenTournamentSessionRequest(int playerId, TournamentId tournamentId, Money openingBalance) {
        super(playerId, tournamentId.getIntegrationId(), openingBalance);
        setAccountName(TOURNAMENT_ACCOUNT);
        this.tournamentId = tournamentId;
    }

    public TournamentId getTournamentId() {
        return tournamentId;
    }

    @Override
    public String toString() {
        return "OpenTournamentSessionRequest{" + "tournamentId=" + tournamentId + "} " + super.toString();
    }
}
