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
package com.cubeia.firebase.server.gateway.util;

import static com.cubeia.firebase.server.gateway.util.WireFormat.STYX_BINARY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import com.cubeia.firebase.io.ObjectFactory;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

public class CompilationCache {

	private final ObjectFactory factory = new ProtocolObjectFactory();
	private final StyxSerializer binary = new StyxSerializer(factory);
//	private final StyxJsonSerializer json = new StyxJsonSerializer(factory);
//	private final Logger log = Logger.getLogger(getClass());
	
	private final EnumMap<WireFormat, Object> cache = new EnumMap<WireFormat, Object>(WireFormat.class);
	
	private final List<ProtocolObject> objects;
	
	public CompilationCache(List<ProtocolObject> objects) {
		this.objects = objects;
	}
	
	public List<ProtocolObject> getObjects() {
		return objects;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<byte[]> getSyxBinary() {
		List<byte[]> list = (List<byte[]>) cache.get(STYX_BINARY);
		if(list == null) {
			list = new ArrayList<byte[]>(objects.size());
			for (ProtocolObject o : objects) {
				// try {
					list.add(binary.pack(o).array());
				/*} catch (IOException e) {
				}*/
			}
			cache.put(STYX_BINARY, list);
		}
		return list;
	}
}
