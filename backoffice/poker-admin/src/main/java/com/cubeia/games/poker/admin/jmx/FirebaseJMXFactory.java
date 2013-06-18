/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.jmx;

import java.io.InputStream;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.cubeia.poker.shutdown.impl.ShutdownServiceMBean;
import org.apache.log4j.Logger;

import com.cubeia.firebase.service.clientreg.state.StateClientRegistryMBean;
import org.springframework.stereotype.Component;

@Component
public class FirebaseJMXFactory {
    
    private static final transient Logger log = Logger.getLogger(FirebaseJMXFactory.class);
    
    private String serverUrl;
    
    /**
     * Will read the property file every time the factory is created.
     * This may not be optimal for performance, but it allows for
     * runtime changes to the property file.
     */
    public FirebaseJMXFactory() {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("gameserver.properties");
            
            if (resourceAsStream == null) {
                resourceAsStream = this.getClass().getResourceAsStream("/gameserver.properties");
            }
            
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                serverUrl = properties.getProperty("firebase.gateway");
                log.debug("Loaded gameserver url, 'firebase.gateway', as: "+ serverUrl);
                resourceAsStream.close();
            } else {
                throw new RuntimeException("Could not find gameserver.properties in classpath");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Could not read gameserver.properties", e);
        }
    }
    
    public StateClientRegistryMBean createClientRegistryProxy() {
        return getMBean(StateClientRegistryMBean.class, "com.cubeia.firebase.clients:type=ClientRegistry");
    }

    public ShutdownServiceMBean createShutdownServiceProxy() {
        return getMBean(ShutdownServiceMBean.class, "com.cubeia.poker.shutdown:type=ShutdownService");
    }

    private <T> T getMBean(Class<T> interfaceClass, String mbeanName) {
        try {
            JMXServiceURL url = new JMXServiceURL(serverUrl);
            log.debug("SERVER URL: " + serverUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            // Get an MBeanServerConnection
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            // Construct the ObjectName for the mbean
            ObjectName objectName = new ObjectName(mbeanName);

            // Create a dedicated proxy for the MBean instead of going directly through the MBean server connection
            return JMX.newMBeanProxy(mbsc, objectName, interfaceClass, true);

        } catch (Exception e) {
            log.error("Failed to create StateClientRegistryMBean JMX proxy", e);
            return null;
        }
    }

}
