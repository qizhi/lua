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

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import com.cubeia.firebase.api.util.UnmodifiableList;
import com.cubeia.firebase.server.service.Dependency;

public class ServiceBeanData {

	public static CompositeData getData(ServiceBean bean) throws OpenDataException {
		return new CompositeDataSupport(ServiceBeanType.getType(), toServiceTypeMap(bean));
	}
	
	private ServiceBeanData() { }
	
	public static Map<String, Object> toServiceTypeMap(ServiceBean serv) {
		Map<String, Object> map = new HashMap<String, Object>(9);
		map.put(ServiceBeanType.TYPE_NAMES[0], serv.getName());
		map.put(ServiceBeanType.TYPE_NAMES[1], serv.getPublicId());
		map.put(ServiceBeanType.TYPE_NAMES[2], serv.getDescription());
		map.put(ServiceBeanType.TYPE_NAMES[3], toString(serv.getContractClasses()));
		map.put(ServiceBeanType.TYPE_NAMES[4], serv.getServiceClass());
		map.put(ServiceBeanType.TYPE_NAMES[5], serv.isAutoStart());
		map.put(ServiceBeanType.TYPE_NAMES[6], serv.isIsolated());
		map.put(ServiceBeanType.TYPE_NAMES[7], serv.isPublic());
		map.put(ServiceBeanType.TYPE_NAMES[8], toString(serv.getDependencies()));
		map.put(ServiceBeanType.TYPE_NAMES[9], serv.isStarted());
		map.put(ServiceBeanType.TYPE_NAMES[10], serv.getStartupStackCapture());
		return map;
	}
	
	private static Object toString(String[] classes) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < classes.length; i++) {
			b.append(classes[i]);
			if(i + 1 < classes.length) {
				b.append(", ");
			}
		}
		return b.toString();
	}

	private static Object toString(UnmodifiableList<Dependency> dependencies) {
		StringBuilder b = new StringBuilder();
		for (Dependency d : dependencies) {
			if(b.length() > 0) b.append(", ");
			b.append("[").append(d.isContract ? "contract:" : "id:");
			b.append(d.data).append("]");
		}
		return b.toString();
	}
}
