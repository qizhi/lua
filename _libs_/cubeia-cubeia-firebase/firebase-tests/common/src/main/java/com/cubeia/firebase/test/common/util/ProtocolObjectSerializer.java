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
package com.cubeia.firebase.test.common.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.io.ObjectFactory;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;

public class ProtocolObjectSerializer implements Serializer {

	private StyxSerializer serializer;

	public ProtocolObjectSerializer(ObjectFactory factory) {
		Arguments.notNull(factory, "factory");
		serializer = new StyxSerializer(factory);
	}
	
	public Object deserialize(byte[] bytes) throws IOException {
		return serializer.unpack(ByteBuffer.wrap(bytes));
	}

	public byte[] serialize(Object o) throws IOException {
		return serializer.packArray((ProtocolObject)o);
	}

}
