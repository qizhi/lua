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

package com.cubeia.network.wallet.firebase.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ResponseStatus {
	public enum StatusCode {
		OK, 
		ERROR
	};
	
	public enum ErrorCode {
		NO_ERROR,
		AUTHENTICATION_ERROR,
		AUTHORIZATION_ERROR,
		COMMUNICATION_ERROR,
		UNKNOWN_ERROR,
		SESSION_NOT_FOUND,
		SESSION_CLOSED,
		INVALID_OR_UNAUTHORIZED_USER,
		REMOTE_ACCOUNT_CALL_FAILED,
		INSUFFICIENT_FUNDS,
		NON_BALANCED_TRANSACTION
	}
	
	
	private StatusCode statusCode;
	private ErrorCode errorCode;
	private String message;
	
	/**
	 * Creates a OK response status.
	 */
	public ResponseStatus() {
		statusCode = StatusCode.OK;
		errorCode = ErrorCode.NO_ERROR;
		message = null;
	}
	
	public ResponseStatus(StatusCode statusCode, ErrorCode errorCode, String message) {
		this.statusCode = statusCode;
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public StatusCode getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(StatusCode code) {
		this.statusCode = code;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isError() {
		return statusCode != StatusCode.OK;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
