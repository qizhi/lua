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
package com.cubeia.firebase.server.service.jmx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

public class RegistryStats implements RegistryStatsMBean {

	private Map<String, ServiceBean> services = new ConcurrentHashMap<String, ServiceBean>();
	
	public void registerService(ServiceBean info) {
		services.put(info.getPublicId(), info);
	}
	
	public void unRegisterService(String pubId) {
		services.remove(pubId);
	}
	
	public TabularData getServices() {
		try {
			return ServiceBeanMapData.getData(services);
		} catch (OpenDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public int getServiceCount() {
		return services.size();
	}
}
