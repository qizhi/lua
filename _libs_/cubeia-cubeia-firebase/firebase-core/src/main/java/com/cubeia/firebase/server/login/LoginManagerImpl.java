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
package com.cubeia.firebase.server.login;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.util.executor.JmxExecutor;


/**
 * Implementation of the login manager interface.
 * Uses a local thread pool executor to handle login requests.
 *
 * The implementation will use a local loopback 
 * (ClientLocalActionHandler) for notifying the clients.
 * 
 *  You will need to inject at least one of those services before
 *  using the LoginManager. Router has precedence over loopback.
 *  I.e. if both are set then only the Router will be used.
 *
 * @author Fredrik
 */
public class LoginManagerImpl implements LoginManager {
	
	/** Main logger */
	private transient Logger log = Logger.getLogger(this.getClass());

	/**
	 * We need to keep a separate thread pool for each type of
	 * login handler to avoid operators blocking each other.
	 */
	private static ConcurrentMap<Class<?>, JmxExecutor> executors = new ConcurrentHashMap<Class<?>, JmxExecutor>();
	
	/** Client logger */
	private transient Logger clientLog = Logger.getLogger("CLIENTS");

	private final LoginLocator locator;
	
	private State state = State.STOPPED;

	private final PostLoginProcessor proc;

	private final LoginManagerConfiguration config;

	/**
	 * Constructor.
	 * @param proc 
	 * @param con 
	 * @param registry 
	 */
	public LoginManagerImpl(LoginLocator locator, PostLoginProcessor proc, LoginManagerConfiguration config) {
		this.locator = locator;
		//locator.init(registry);
		this.proc = proc;
		this.config = config;
	}

	public void handleLoginRequest(LoginRequestAction request, LocalActionHandler loopback) {
		try {
			// Lookup the handler for the login request
			LoginHandler handler = locator.locateLoginHandler(request);
			LoginTask job = new LoginTask(request, loopback, handler);
			getExecutor(handler).submit(job);
		} catch(Exception e) {
			log.error("Failed to handle login request", e);
		}
	}
	
	/**
	 * Finds the designated executor.
	 * 
	 * @param handler
	 * @return
	 */
	private JmxExecutor getExecutor(LoginHandler handler) {
		JmxExecutor executor = executors.get(handler.getClass());
		if (executor == null) {
			synchronized (executors) {
				// Update reference 
				executor = executors.get(handler.getClass());
				// Check again that no-one beat us
				if (executor == null) {
					// Create new executor and add it to the map
					int threads = config.getNumberOfThreads();
					executor = new JmxExecutor(threads, threads, "LoginHandler-" + handler.getClass().getSimpleName());
					log.info("Created executor for " + handler.getClass().getSimpleName() + " with " + threads + " threads.");
					executors.put(handler.getClass(), executor);
				}
			}
		}
		return executor;
	}

	/**
	 * Send a response back to the client.
	 * Will use the same loopback as supplied in the handle login.
	 * 
	 * @param response
	 * @param loopback
	 */
	private void sendResponse(LoginResponseAction response, LocalActionHandler loopback) {
		
		// Log the event
		if (response.isAccepted()) {
			clientLog.info("Player logged in. PID: "+response.getPlayerid());
			log.debug("Player logged in. PID: "+response.getPlayerid());
			HitCounter.getInstance().inc("Logins");
		} else {
			clientLog.info("Player login failed. PID: "+response.getPlayerid());
			log.debug("Player logged failed. PID: "+response.getPlayerid());
		}
		
		if (loopback != null){
			// Notify the action directly on the loopback handler
			loopback.handleAction(response);
			
		} else {
			// All is lost. Log a serious error and go back into the hole you came from.
			log.fatal("There is no return channel defined for the login responses!");
		}
	}
	
	
	
	/**
	 * Executes the Login Request, i.e. will locate a proper
	 * Login Handler, get a Login Response and send it off to the
	 * recipient.
	 *
	 * @author Fredrik
	 */
	private class LoginTask implements Runnable {

		private final LoginRequestAction request;
		private final LocalActionHandler loopback;
		private final LoginHandler handler;
		
		public LoginTask(final LoginRequestAction request, LocalActionHandler loopback, LoginHandler handler) {
			this.request = request;
			this.loopback = loopback;
			this.handler = handler;
		}

		public void run() {
			try {
				LoginResponseAction response = handler.handle(request);
				checkHandleLoginProcessor(response);
				sendResponse(response, loopback);
			} catch (Throwable th) {
				log.error("Login Handler exception caught: "+th, th);
			}
		}

		private void checkHandleLoginProcessor(LoginResponseAction response) {
			if(response.isAccepted() && proc != null) {
				int pid = response.getPlayerid();
				String scn = response.getScreenname();
				proc.clientLoggedIn(pid, scn);
			}
		}	
	}



	public String getStateDescription() {
		return state.toString();
	}

	public void start() {
		state = State.STARTED;
	}

	public void stop() {
		state = State.STOPPED;
	}
	
}
