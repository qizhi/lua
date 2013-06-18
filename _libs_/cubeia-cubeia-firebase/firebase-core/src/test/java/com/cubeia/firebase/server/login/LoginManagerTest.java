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

import static org.mockito.Mockito.*;
import com.cubeia.firebase.api.action.local.LocalAction;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.server.login.trivial.TrivialLoginLocator;

import junit.framework.TestCase;

public class LoginManagerTest extends TestCase implements LocalActionHandler {

	private volatile boolean loggedIn = false;
	
	public void testHandleLoginRequest() throws Exception {
		// Setup a Login Manager using a mocked Client Local Action Handler
		LoginManagerConfiguration conf = mock(LoginManagerConfiguration.class);
		when(conf.getNumberOfThreads()).thenReturn(10);
		LoginManager manager = new LoginManagerImpl(new TrivialLoginLocator(), null, conf);
		manager.handleLoginRequest(getLogin("apan", "111"), this);
		manager.handleLoginRequest(getLogin("apan", "111"), this);
		
		// Sleep for a little while, allowing the manager to execute properly
		Thread.sleep(200);
		
		assertTrue(loggedIn);
	}
	
	
	private LoginRequestAction getLogin(String user, String pwd) {
		LoginRequestAction action = new LoginRequestAction(user, pwd, 1);
		return action;
	}


	public void handleAction(LocalAction action) {
		if (action instanceof LoginResponseAction) {
			LoginResponseAction response = (LoginResponseAction) action;
			loggedIn = response.isAccepted();
		}
	}
	
}
