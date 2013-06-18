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
package com.cubeia.service.crossdomain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;

/**
 * This service starts an TCP server on a configurable port in order to
 * serve flash cross domain requests. The policy file to serve should be...
 * 
 * <pre>
 *     conf/crossdomain.xml
 * </pre>
 * 
 * ... and if not found, the server will default to an "accept all" policy. This
 * service is configured in a properties file called...
 * 
 * <pre>
 *    conf/crossdomain.props
 * </pre>
 * 
 * ... with the following available properties (shown with default values):
 * 
 * <ul>
 * 	<li>CrossDomainPolicyFile - File to serve, defaults to "conf/crossdomain.xml"</li>
 *  <li>CrossDomainServicePort - Port to serve file from, defaults to 4122</li>
 *  <li>UseMinaLoggingFilter - Set to "true" to log all requests, defauolts to "false"</li>
 * </ul>
 * 
 * @author Lars J. Nilsson
 */
public class FlashCrossDomainService implements FlashCrossDomainServiceContract, Service {

	private Logger log = Logger.getLogger(this.getClass());
	/*------------------------------------------------
	
	 SERVICE BEAN Methods
	 
	 The service lifespan is defined by the methods 
	 below. They will be called in the order they appear.
	
	-------------------------------------------------*/
	/** Start mina server */
	public void start() {
		// create server instance
		flashCrossDomainPolicyServer = new FlashCrossDomainServer();
		String crossDomainFileName = "conf/crossdomain.xml";
		File crossDomainPolicyFile = null;
		String crossDomainServicePort = "4122";
		String useMinaLogging = "false";
		
		try {
			// configuration properties file 
			File configFile = new File(configDirectory, "crossdomain.props");

			// load and set properties
			FileInputStream inStream = new FileInputStream(configFile);
			Properties props = new Properties();
			props.load(inStream);
			crossDomainFileName = props.getProperty("CrossDomainPolicyFile");
			crossDomainPolicyFile = new File(configDirectory, crossDomainFileName);
			crossDomainServicePort = props.getProperty("CrossDomainServicePort");
			useMinaLogging = props.getProperty("UseMinaLoggingFilter");
			// start server
			
		} catch (FileNotFoundException e1) {
			log.warn("Unable to open config file, using defaults");
		} catch (IOException e) {
			log.warn("Unable to load properties from config file, using defaults");
			crossDomainPolicyFile = null;
		}
		try {
			flashCrossDomainPolicyServer.start(crossDomainPolicyFile, Integer.parseInt(crossDomainServicePort), useMinaLogging.equalsIgnoreCase("true"));
		} catch (IOException e) {
			log.error("Unable to start cross domain policy service");
			e.printStackTrace();
		}
	}
	/** Nothing to do */
	public void stop() {
		
	}
	
	/** Nothing to do */
	public void destroy() {}
	

	private FlashCrossDomainServer flashCrossDomainPolicyServer;
	private File configDirectory;

	/**
	 * Init service
	 * @param context
	 * @throws SystemException
	 */
	public void init(ServiceContext context) throws SystemException {
		
		configDirectory = context.getServerConfigDirectory();
		System.out.println("ServerConfigFirectory:" + configDirectory.getAbsolutePath() );
		
	}

	
}
