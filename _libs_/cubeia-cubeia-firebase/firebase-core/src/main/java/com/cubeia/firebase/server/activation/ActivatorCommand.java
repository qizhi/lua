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
package com.cubeia.firebase.server.activation;

import java.io.IOException;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.server.commands.Types;
import com.cubeia.firebase.util.Serializer;

public class ActivatorCommand extends Command<byte[]> {

	private static final long serialVersionUID = 1615558378609797207L;
	
	private final int id;
	private final boolean forGame;

	public ActivatorCommand(int id, ActivatorAction<?> action, boolean forGame) throws IOException {
		super(Types.ACTIVATOR.ordinal());
		this.forGame = forGame;
		setAttachment(new Serializer().serialize(action));
		this.id = id;
	}
	
	public boolean isForGame() {
		return forGame;
	}
	
	public ActivatorAction<?> getAction(ClassLoader targetLoader) throws IOException, ClassNotFoundException {
		if(getAttachment() == null) {
			return null;
		} else {
			if(targetLoader == null) {
				targetLoader = getClass().getClassLoader();
			}
			return (ActivatorAction<?>) new Serializer(targetLoader).deserialize(getAttachment());
		}
	}
	
	public int getId() {
		return id;
	}
}
