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

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * A helper class for proxying an interface to a remote MBean.
 * 
 * @author Lars J. Nilsson
 */
public class Jmx {

	/**
	 * Given a remote server with an mbean mounted at a known path, proxy the interface
	 * locally. It will attempt to connect to the following address:
	 * 
	 * <pre>
	 * 	service:jmx:rmi:///jndi/rmi://&lt;host&gt;:&lt;port&gt;/jmxrmi
	 * </pre>
	 * 
	 * @param <T> Type of the interface
	 * @param host Host to connect to, must not be null
	 * @param port Port to connect to, must not be null
	 * @param name Object name of the mbean, must not be null
	 * @param mbean MBean interface to proxy, must not be null
	 * @return A proxy of the given interface, never null
	 * @throws IOException On IO errors
	 */
	public static <T> T proxy(String host, int port, ObjectName name, Class<T> mbean) throws IOException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        JMXConnector con = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = con.getMBeanServerConnection();
        return (T) MBeanServerInvocationHandler.newProxyInstance(mbsc, name, mbean, false);
	}
	
	private Jmx() { }

}
