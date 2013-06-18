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
package com.cubeia.firebase.server.util;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.conf.ConfigurationAdapter;
import com.cubeia.firebase.server.conf.MapConfiguration;

public class JGroupsConfigTest extends TestCase {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExtendingIFace() throws Exception {
		Class cl = ExtConfig.class;
		Method meth = cl.getMethod("getMcastAddress", (Class[])null);
		Property ann = meth.getAnnotation(Property.class);
		assertEquals("228.0.14.41:-1", ann.defaultValue());
		meth = cl.getMethod("getOobPoolProperties", (Class[])null);
		ann = meth.getAnnotation(Property.class);
		assertEquals("1,4,120000,true,2000", ann.defaultValue());
	}
	
	public void testSetup1() throws Exception {
		MapConfiguration props = new MapConfiguration();
		props.setProperty(new PropertyKey(new Namespace("test"), "mcast-address"), "228.0.14.41:9560");
		ConfigurationAdapter ad = new ConfigurationAdapter(props);
		JGroupsConfig conf = ad.implement(JGroupsConfig.class, new Namespace("test"));
		System.out.println(JGroupsConfigurator.toXMLCharacters(conf, new SocketAddress("0.0.0.0:-1"), "eth1"));
	}
}
