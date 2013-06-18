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

import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;

public class PlayerUnseater {

    public void unseatPlayer(Table table, int playerId, boolean setAsWatcher) {
        table.getPlayerSet().unseatPlayer(playerId);
        table.getListener().playerLeft(table, playerId);
        if (setAsWatcher) {
            LeaveAction leave = new LeaveAction(playerId, table.getId());
            WatchResponseAction watch = new WatchResponseAction(table.getId(), WatchResponseStatus.OK);
            table.getNotifier().sendToClient(playerId, leave);
            table.getNotifier().sendToClient(playerId, watch);
            table.getWatcherSet().addWatcher(playerId);
            table.getListener().watcherJoined(table, playerId);
        }
    }

}
