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

package com.cubeia.backend.cashgame.exceptions;

import com.cubeia.backend.cashgame.dto.ReserveFailedResponse.ErrorCode;

public class ReserveFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    public final ErrorCode errorCode;
    public final boolean playerSessionNeedsToBeClosed;

    public ReserveFailedException(String message, ErrorCode errorCode, boolean playerSessionNeedsToBeClosed) {
        super(message);
        this.errorCode = errorCode;
        this.playerSessionNeedsToBeClosed = playerSessionNeedsToBeClosed;
    }

    public ReserveFailedException(String message, Throwable cause, ErrorCode errorCode, boolean playerSessionNeedsToBeClosed) {
        super(message, cause);
        this.errorCode = errorCode;
        this.playerSessionNeedsToBeClosed = playerSessionNeedsToBeClosed;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
