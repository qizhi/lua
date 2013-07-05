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

import com.cubeia.backend.cashgame.PlayerSessionId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class OpenSessionResponse implements Serializable {
    private final PlayerSessionId sessionId;
    private final Map<String, String> sessionProperties;

    public OpenSessionResponse(PlayerSessionId sessionId, Map<String, String> sessionProperties) {

        this.sessionId = sessionId;
        this.sessionProperties = sessionProperties;
    }

    public String getProperty(String key) {
        return getSessionProperties().get(key);
    }

    public void setProperty(String key, String value) {
        sessionProperties.put(key, value);
    }

    public PlayerSessionId getSessionId() {
        return sessionId;
    }

    public Map<String, String> getSessionProperties() {
        return new HashMap<String, String>(sessionProperties);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OpenSessionResponse");
        sb.append("{sessionId=").append(sessionId);
        sb.append(", sessionProperties=").append(sessionProperties);
        sb.append('}');
        return sb.toString();
    }
}
