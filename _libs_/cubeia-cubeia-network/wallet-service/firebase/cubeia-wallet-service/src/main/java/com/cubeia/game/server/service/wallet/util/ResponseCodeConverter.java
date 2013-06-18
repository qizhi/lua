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

package com.cubeia.game.server.service.wallet.util;

import java.util.HashMap;
import java.util.Map;

import com.cubeia.backoffice.wallet.api.dto.ErrorCodes;
import com.cubeia.network.wallet.firebase.domain.ResponseStatus.ErrorCode;

/**
 * Utility class providing conversion methods between internal and external status/error codes.
 * @author w
 */
public class ResponseCodeConverter {

	private static Map<ErrorCodes, ErrorCode> errorExternalToInternal = new HashMap<ErrorCodes, ErrorCode>();
	static {
		errorExternalToInternal.put(ErrorCodes.INSUFFICIENT_FUNDS, ErrorCode.INSUFFICIENT_FUNDS);
		errorExternalToInternal.put(ErrorCodes.INVALID_OR_UNAUTHORIZED_USER, ErrorCode.INVALID_OR_UNAUTHORIZED_USER);
		errorExternalToInternal.put(ErrorCodes.NON_BALANCED_TRANSACTION, ErrorCode.NON_BALANCED_TRANSACTION);
		errorExternalToInternal.put(ErrorCodes.REMOTE_ACCOUNT_CALL_FAILED, ErrorCode.REMOTE_ACCOUNT_CALL_FAILED);
		errorExternalToInternal.put(ErrorCodes.SESSION_CLOSED, ErrorCode.SESSION_CLOSED);
		errorExternalToInternal.put(ErrorCodes.SESSION_NOT_FOUND, ErrorCode.SESSION_NOT_FOUND);
		errorExternalToInternal.put(ErrorCodes.UNKNOWN_ERROR, ErrorCode.UNKNOWN_ERROR);
	}
	
	public static ErrorCode convertExternalErrorToInternal(int errorCodeOrdinal) {
		ErrorCodes[] errorValues = ErrorCodes.values();
		ErrorCodes errorCode = (errorCodeOrdinal >= 0  &&  errorCodeOrdinal < errorValues.length) 
			? errorValues[errorCodeOrdinal]
			: ErrorCodes.UNKNOWN_ERROR;
		
			return convertExternalErrorToInternal(errorCode);
	}
	
	public static ErrorCode convertExternalErrorToInternal(ErrorCodes error) {
		if (errorExternalToInternal.containsKey(error)) {
			return errorExternalToInternal.get(error);
		} else {
			return ErrorCode.UNKNOWN_ERROR;
		}
	}
}
