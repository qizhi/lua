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

package com.cubeia.backoffice.web;

import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.auth.AuthenticationManager;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 */

@Component("wicketApplication")
public class BackofficeApplication extends AuthenticatedWebApplication {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;
    
	/**
	 * Constructor
	 */
	public BackofficeApplication() {
		 
	}

	@Override
	protected void init() {
	    super.init();
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
        getMarkupSettings().setStripWicketTags(true);
//        setRequestCycleProvider(new IRequestCycleProvider() {
//            @Override
//            public RequestCycle get(RequestCycleContext ctx) {
//                // TODO Auto-generated method stub
//                return new BackofficeRequestCycle(BackofficeApplication.this, (WebRequest) request, response);
//            }
//        });
        
        getRequestCycleListeners().add(new BackofficeRequestCycleListener());
	}
	
	@Override
	public Session newSession(Request request, Response response) {
	    Session session = super.newSession(request, response);
	    session.setLocale(Locale.ENGLISH);
        return session;
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
        return Home.class;
	}

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return BackofficeSignInPage.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return BackofficeAuthSession.class;
    }
    
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    
//    @Override
//    public RequestCycle newRequestCycle(Request request, Response response) {
//        return new BackofficeRequestCycle(this, (WebRequest) request, response);
//    }
    
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        log.debug("injecting auth mgr");
        this.authenticationManager = authenticationManager;
    }
}
