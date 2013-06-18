/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.transaction;

/**
 * This exception is used by resource during the commit phase. It
 * contains values for retry and retry delay hints.
 * 
 * @author Larsan
 */
public class ResourceException extends TransactionException {

	private static final long serialVersionUID = 3757123669226160672L;

	private final int retries;
	private long retryHint;
	
	public ResourceException(String msg) {
		this(msg, -1);
	}
	
	public ResourceException(int retries) {
		this(null, retries);
	}
	
	public ResourceException(String msg, int retries) {
		this(msg, null, retries);
	}
	
	public ResourceException(int retries, long hint) {
		this(null, null, retries, hint);
	}
	
	public ResourceException(String msg, int retries, long hint) {
		this(msg, null, retries, hint);
	}
	
	public ResourceException(String msg, Throwable th) {
		this(msg, th, -1);
	}
	
	public ResourceException(String msg, Throwable th, int retries) {
		this(msg, th, retries, -1);
	}
	
	public ResourceException(String msg, Throwable th, int retries, long hint) {
		super(msg, th);
		this.retries = retries;
		this.retryHint = hint;
	}
	
	public int getRetries() {
		return retries;
	}
	
	public long getRetryHint() {
		return retryHint;
	}
}
