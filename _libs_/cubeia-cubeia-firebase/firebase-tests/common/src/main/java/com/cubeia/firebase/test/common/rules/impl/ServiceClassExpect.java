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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.test.common.rules.Expect;
import com.cubeia.firebase.test.common.util.Serializer;

public class ServiceClassExpect implements Expect {

	private final Class<?> clazz;
	private final Serializer serializer;
	private Object packet;
	
	public ServiceClassExpect(Class<?> cl, Serializer serializer) {
		Arguments.notNull(cl, "class");
		Arguments.notNull(serializer, "serializer");
		this.serializer = serializer;
		clazz = cl;
	}
	
	public Action accept(Object o) {
		if(o instanceof ServiceTransportPacket) {
			byte[] bytes = ((ServiceTransportPacket)o).servicedata;
			try {
				packet = serializer.deserialize(bytes);
				boolean b = clazz.isAssignableFrom(packet.getClass());
				if(b) {
					return Action.DONE;
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
	
	@Override
	public String toString() {
		return "service class: " + clazz.getName();
	}

	public Object result() {
		return packet;
	}
}
