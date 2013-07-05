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

package com.cubeia.games.poker.adapter;

import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.poker.PokerState;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cubeia.firebase.api.game.player.PlayerStatus.CONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.WAITING_REJOIN;

public class DisconnectHandler {

    private static Logger log = LoggerFactory.getLogger(DisconnectHandler.class);

    @Inject
    PokerState state;

    @Inject
    FirebaseServerAdapter adapter;


    /**
     * Check the new status and adjust time to act accordingly and send
     * out notifications as needed.
     *
     */
    public void checkDisconnectTime(Table table, int playerId, PlayerStatus status) {
        log.debug("Check disconnect for player {} with new status {}", playerId, status);
        if (status.equals(WAITING_REJOIN)) {
            log.debug("Waiting for player " + playerId + " to reconnect to table " + table.getId());
        } else if (status.equals(CONNECTED)) {
            log.debug("Player " + playerId + " connected table " + table.getId());
        }
    }
}
