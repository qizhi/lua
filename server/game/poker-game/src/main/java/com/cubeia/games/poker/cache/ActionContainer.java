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

package com.cubeia.games.poker.cache;

import com.cubeia.firebase.api.action.GameAction;

public class ActionContainer {

    private final Integer playerId;

    private final Integer excludedPlayerId;

    private final GameAction gameAction;

    private final Long timestamp;

    private ActionContainer(Integer playerId, GameAction gameAction, Integer excludedPlayerId, Long timestamp) {
        this.playerId = playerId;
        this.gameAction = gameAction;
        this.excludedPlayerId = excludedPlayerId;
        this.timestamp = timestamp;
    }

    private ActionContainer(GameAction gameAction, Integer excludedPlayerId, Long timestamp) {
        this(null, gameAction, excludedPlayerId, timestamp);
    }

    private ActionContainer(Integer playerId, GameAction gameAction, Long timestamp) {
        this(playerId, gameAction, null, timestamp);
    }

    private ActionContainer(GameAction gameAction, Long timestamp) {
        this(null, gameAction, null, timestamp);
    }

    public static ActionContainer createPublic(GameAction gameAction, Long timestamp) {
        return new ActionContainer(null, gameAction, timestamp);
    }

    public static ActionContainer createPublic(GameAction gameAction, Integer excludedPlayerId, Long timestamp) {
        return new ActionContainer(null, gameAction, excludedPlayerId, timestamp);
    }

    public static ActionContainer createPrivate(int playerId, GameAction gameAction, Long timestamp) {
        return new ActionContainer(playerId, gameAction, timestamp);
    }

    public int getPlayerId() {
        return playerId;
    }

    public Integer getExcludedPlayerId() {
        return excludedPlayerId;
    }

    public GameAction getGameAction() {
        return gameAction;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean isPublic() {
        return playerId == null;
    }
}
