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
package com.cubeia.firebase.test.common.rules.impl;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.test.common.rules.Expect;
import com.cubeia.firebase.test.common.util.Serializer;

public class GameClassExpect implements Expect {

	private final Class<?> clazz;
	private final Serializer serializer;
	private final List<Attribute> attributes;
	private Object packet;
	
	public GameClassExpect(Class<?> cl, Serializer serializer) {
		this(cl, serializer, null);
	}
	
	public GameClassExpect(Class<?> cl, Serializer serializer, List<Attribute> attributes) {
		Arguments.notNull(cl, "class");
		Arguments.notNull(serializer, "serializer");
		this.attributes = attributes;
		this.serializer = serializer;
		clazz = cl;
	}
	
	public Action accept(Object o) {
		if(o instanceof GameTransportPacket) {
			GameTransportPacket p = (GameTransportPacket) o;
			byte[] bytes = p.gamedata;
			try {
				packet = serializer.deserialize(bytes);
				if(clazz.isAssignableFrom(packet.getClass())) {
					if(checkAttributes(p)) {
						return Action.DONE;
					} else {
						Logger.getLogger(getClass()).error("One or more attribute mismatches; Expected: " + attributes + "; Got: " + p.attributes);
						return Action.FAIL;
					}
				} else {
					return Action.PASS_THROUGH;
				}
			} catch(IOException e) {
				Logger.getLogger(getClass()).error("Failed to unpack game packet", e);
				return Action.FAIL;
			}
		} else {
			return Action.PASS_THROUGH;
		}
	}
	
	private boolean checkAttributes(GameTransportPacket o) {
		return AttributeUtil.checkAttributes(attributes, o.attributes);
	}
	
	@Override
	public String toString() {
		return "game class: " + clazz.getName();
	}

	public Object result() {
		return packet;
	}
}
