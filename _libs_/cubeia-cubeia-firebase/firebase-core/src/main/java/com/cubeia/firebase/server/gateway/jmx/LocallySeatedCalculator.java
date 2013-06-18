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
package com.cubeia.firebase.server.gateway.jmx;

import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.clientreg.ClientStatusFields;

public class LocallySeatedCalculator {

	public static long countSeated(String nodeId, SystemStateServiceContract systemState) {
		Set<String> children = systemState.getChildren(SystemStateConstants.CLIENT_ROOT_FQN);
		int seatedCount = 0;
		
		for (String child : children) {
			String childNodePath = SystemStateConstants.CLIENT_ROOT_FQN + child + "/";
			Map<Object, Object> attributes = systemState.getAttributes(childNodePath);
		
			String currentNodeId = (String) attributes.get(String.valueOf(ClientStatusFields.NODE));
			if (nodeId.equals(currentNodeId)) {
				String tableNodePath = childNodePath + "table";
				for (Object key : systemState.getAttributes(tableNodePath).keySet()) {
					seatedCount += (key.toString().startsWith("_") ? 0 : 1);
				}
			}
		}
		return seatedCount;
	}
}
