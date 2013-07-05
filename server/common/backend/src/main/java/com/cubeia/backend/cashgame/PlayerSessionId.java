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

package com.cubeia.backend.cashgame;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class PlayerSessionId implements Serializable {

    private static final long serialVersionUID = 6096309763354995789L;

    /**
     * Firebase player ID.
     */
    public final int playerId;

    /**
     * Optional ID for the session, may be null.
     */
    public final String integrationSessionId;

    /**
     * @param platformPlayerId     Firebase player ID, mandatory
     * @param integrationSessionId Session ID, mey be null
     */
    public PlayerSessionId(int platformPlayerId, String integrationSessionId) {
        this.playerId = platformPlayerId;
        this.integrationSessionId = integrationSessionId;
    }

    /**
     * This creates a session ID without an integration ID.
     *
     * @param playerId Firebase player ID, mandatory
     */
    public PlayerSessionId(int playerId) {
        this(playerId, null);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
