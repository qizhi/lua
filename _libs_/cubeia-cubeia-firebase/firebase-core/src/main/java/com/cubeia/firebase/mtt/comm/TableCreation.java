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

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.commands.Types;
import com.cubeia.firebase.util.Serializer;

public final class TableCreation extends MttCommand<TableCommandData[]> {

	private static final long serialVersionUID = -6361826072809654684L;
	
	// private final Serializer ser = new Serializer();
	
	private byte[] attachment;

	public TableCreation(Type type, int mttId, int gameId, int requestId, TableCommandData[] data) {
		super(Types.MTT_TABLE_CREATION.ordinal(), type, mttId, gameId, requestId);
		setAttachment(data);
	}
	
	
	/**
	 * Response constructor which sets the type to response and copies
	 * mtt id, game id and request id from the original request.
	 * 
	 * @param request Creation request, must not be null
	 * @param data Attachment data, may be null
	 */
	public TableCreation(TableCreation request, TableCommandData[] data) {
		super(Types.MTT_TABLE_CREATION.ordinal(), Type.RESPONSE, request.getMttId(), request.getGameId(), request.getRequestId());
		setAttachment(data);
	}
	
	
	public void setGameAttachment(Object o) {
		if(o == null) {
			attachment = null;
		} else {
			try {
				attachment = new Serializer().serialize(o);
			} catch (IOException e) {
				Logger.getLogger(getClass()).error("Failed to serialize attachment", e);
			}
		}
	}
	
	public Object getGameAttachment() {
		return getGameAttachment(this.getClass().getClassLoader());
	}
	
	public Object getGameAttachment(ClassLoader gameClassLoader) {
		if(attachment == null) {
			return null;
		} else {
			try {
				return new Serializer(gameClassLoader).deserialize(attachment);
			} catch (Exception e) {
				Logger.getLogger(getClass()).error("Failed to de-serialize attachment", e);
				return null;
			}
		}
	}
}
