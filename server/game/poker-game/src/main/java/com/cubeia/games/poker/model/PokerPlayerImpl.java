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

package com.cubeia.games.poker.model;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.poker.player.DefaultPokerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a player that is active in the game.
 * <p/>
 * Part of replicated game state
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerPlayerImpl extends DefaultPokerPlayer implements Serializable {
    public static final String ATTR_PLAYER_EXTERNAL_SESSION_ID = "PLAYER_EXTERNAL_SESSION_ID";

    private static final Logger log = LoggerFactory.getLogger(PokerPlayerImpl.class);

    private static final long serialVersionUID = 1L;

    private GenericPlayer placeholder;

    private Map<Serializable, Serializable> attributes = new HashMap<Serializable, Serializable>();

    private PlayerSessionId playerSessionId;

//	private String externalPlayerSessionId;

    public PokerPlayerImpl(GenericPlayer placeholder) {
        super(placeholder.getPlayerId());
        this.placeholder = placeholder;
        setScreenname(placeholder.getName());
    }

    @Override
    public int getSeatId() {
        return placeholder.getSeatId();
    }

    /**
     * Returns the player attribute map.
     *
     * @return attributes
     */
    public Map<Serializable, Serializable> getAttributes() {
        return attributes;
    }

    /**
     * Sets a session id for this player.
     *
     * @param playerSessionId the session id, or null to leave the session
     */
    public void setPlayerSessionId(PlayerSessionId playerSessionId) {
        log.debug("updating player {} session id: {} -> {}", new Object[]{getId(), getPlayerSessionId(), playerSessionId});
        this.playerSessionId = playerSessionId;
    }

    /**
     * Returns the session id for this player.
     *
     * @return the session id, null if not in a session
     */
    public PlayerSessionId getPlayerSessionId() {
        return playerSessionId;
    }

}
