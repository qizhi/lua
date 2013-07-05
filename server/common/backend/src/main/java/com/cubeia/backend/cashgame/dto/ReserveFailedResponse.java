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


@SuppressWarnings("serial")
public class ReserveFailedResponse implements Serializable {

    private final ErrorCode errorCode;
    private final String message;
    private final PlayerSessionId sessionId;
    private final boolean playerSessionNeedsToBeClosed;

    public ReserveFailedResponse(PlayerSessionId sessionId, ErrorCode errorCode, String message, boolean playerSessionNeedsToBeClosed) {
        this.sessionId = sessionId;
        this.errorCode = errorCode;
        this.message = message;
        this.playerSessionNeedsToBeClosed = playerSessionNeedsToBeClosed;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public PlayerSessionId getSessionId() {
        return sessionId;
    }

    public boolean isPlayerSessionNeedsToBeClosed() {
        return playerSessionNeedsToBeClosed;
    }

    public enum ErrorCode {
        AMOUNT_TOO_HIGH, UNSPECIFIED_FAILURE, SESSION_NOT_OPEN, MAX_LIMIT_REACHED;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ReserveFailedResponse");
        sb.append("{errorCode=").append(errorCode);
        sb.append(", message='").append(message).append('\'');
        sb.append(", sessionId=").append(sessionId);
        sb.append(", playerSessionNeedsToBeClosed=").append(playerSessionNeedsToBeClosed);
        sb.append('}');
        return sb.toString();
    }

}
