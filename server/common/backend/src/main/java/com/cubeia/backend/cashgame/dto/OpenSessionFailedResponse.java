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

import java.io.Serializable;

public class OpenSessionFailedResponse implements Serializable {

    private static final long serialVersionUID = -4319217503234073741L;

    private final ErrorCode errorCode;
    private final String message;
    private final int playerId;

    public OpenSessionFailedResponse(ErrorCode errorCode, String message, int playerId) {
        this.errorCode = errorCode;
        this.message = message;
        this.playerId = playerId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public enum ErrorCode {
        UNKOWN_PLATFORM_TABLE_ID, WALLET_CALL_FAILED, UNSPECIFIED_ERROR;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OpenSessionFailedResponse");
        sb.append("{errorCode=").append(errorCode);
        sb.append(", message='").append(message).append('\'');
        sb.append(", playerId=").append(playerId);
        sb.append('}');
        return sb.toString();
    }
}
