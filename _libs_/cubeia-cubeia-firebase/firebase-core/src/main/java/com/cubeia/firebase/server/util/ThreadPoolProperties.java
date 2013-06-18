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
package com.cubeia.firebase.server.util;

import java.util.StringTokenizer;

/**
 * This simple class manages a thread pool property
 * as a comma separated string of the following values
 * in order:
 * 
 * <ul>
 *   <li>coresize - int</li>
 *   <li>maxsize - int</li>
 *   <li>timeout - long</li>
 *   <li>enableQueuing - boolean</li>
 *   <li>queusize - int</li>
 * </ul>
 * 
 * The trhee first values must be present. If the fourth is present so
 * must the fifth be.
 * 
 * <p>Examples:
 * 
 * <pre>
 * 		2,5,60000
 * 		5,10,20000,true,10
 * 		1,2,10000,false,2
 * 		10,60,240000
 * </pre>
 * 
 * @author Larsan
 */
public final class ThreadPoolProperties {

	private final int coreSize;
	private final int maxSize;
	private final long timeout;
	private final boolean enableQueueing;
	private final int queueSize;
	
	public ThreadPoolProperties(String s) throws IllegalArgumentException {
		StringTokenizer tok = new StringTokenizer(s, ",", false);
		if(tok.countTokens() != 3  && tok.countTokens() != 5) throw new IllegalArgumentException("Illegal number of values in string: " + tok.countTokens() + "; Expected 3 or 5.");
		coreSize = Integer.parseInt(tok.nextToken());
		maxSize = Integer.parseInt(tok.nextToken());
		timeout = Long.parseLong(tok.nextToken());
		if(tok.hasMoreElements()) {
			enableQueueing = Boolean.parseBoolean(tok.nextToken());
			queueSize = Integer.parseInt(tok.nextToken());
		} else {
			enableQueueing = false;
			queueSize = 0;
		}
	}
	
	@Override
	public String toString() {
		return coreSize + "," + maxSize + "," + timeout + "," + enableQueueing + "," + queueSize;
	}
	
	public int getCoreSize() {
		return coreSize;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public int getQueueSize() {
		return queueSize;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public boolean isQueueingEnable() {
		return enableQueueing;
	}
}
