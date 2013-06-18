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
package com.cubeia.firebase.mtt.comm;

import com.cubeia.firebase.api.command.Command;

/**
 * MTT commands contains the mtt id, the game id and is ready
 * for a request/response pattern by enum type and request id.
 * 
 * @author Larsan
 */
public abstract class MttCommand<E> extends Command<E> {
	
	private static final long serialVersionUID = 6614875469844235658L;

	public static enum Type { REQUEST, RESPONSE, NAN }

	private final int mttId;
	private final int gameId;

	private final Type type;
	private final int requestId;

	protected MttCommand(int id, Type type, int mttId, int gameId, int requestId) {
		super(id);
		this.requestId = requestId;
		this.type = type;
		this.mttId = mttId;
		this.gameId = gameId;
	}
	
	protected MttCommand(int id, int mttId, int gameId, int requestId) {
		this(id, Type.NAN, mttId, gameId, requestId);
	}
	
//	public String toString() {
//	    return "MttCommand type["+type+"] mttId["+mttId+"] gameId["+gameId+"] requestId["+requestId+"]";
//	}
	
	public int getRequestId() {
		return requestId;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int getMttId() {
		return mttId;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean isRequest() {
		return Type.REQUEST.equals(type);
	}
}
