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

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

public class ServiceBeanMapData {

	public static TabularData getData(Map<String, ServiceBean> services) throws OpenDataException {
		TabularDataSupport t = new TabularDataSupport(ServiceBeanMapType.getType());
		t.putAll(toArray(services));
		return t;
	}
	
	private ServiceBeanMapData() { }
	
	public static CompositeData[] toArray(Map<String, ServiceBean> services) throws OpenDataException {
		int count = 0;
		CompositeData[] arr = new CompositeData[services.size()];
		for (ServiceBean b : services.values()) {
			arr[count++] = ServiceBeanData.getData(b);
		}
		return arr;
	}
}
