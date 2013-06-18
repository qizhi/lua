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
package com.cubeia.firebase.server.gateway.comm.jetty;

import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxJsonSerializer;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

class JsonUtil {
	
	private static final StyxJsonSerializer serializer = new StyxJsonSerializer(new ProtocolObjectFactory());

	private JsonUtil() { }
	
	static void unpack(String data, AsyncSession target) {
		List<ProtocolObject> list = null;
		if(!data.startsWith("[")) {
			ProtocolObject o = serializer.fromJson(data);
			list = Collections.singletonList(o);
		} else {
			list = serializer.fromJsonList(data);
		}
		target.handleIncoming(list);
	}
	
	static String pack(ProtocolObject o) {
		return (o == null ? null : serializer.toJson(o));
	}
	
	static String pack(List<ProtocolObject> list) {
		if(list == null || list.size() == 0) {
			return null;
		} else if(list.size() == 1) {
			return pack(list.get(0));
		} else {
			return serializer.toJsonList(list);
		}
	}
}
