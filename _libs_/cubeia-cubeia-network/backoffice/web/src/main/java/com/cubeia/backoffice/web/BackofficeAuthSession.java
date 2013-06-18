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

import java.util.ArrayList;
import java.util.Set;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.auth.AuthenticationManager;
import com.cubeia.backoffice.auth.BackofficeRole;

public class BackofficeAuthSession extends AuthenticatedWebSession {
    private static final long serialVersionUID = 1L;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private String userName;
    private Roles roles;

    public BackofficeAuthSession(Request request) {
        super(request);
    }

    @Override
    public boolean authenticate(String userName, String password) {
        AuthenticationManager authManager = ((BackofficeApplication) getApplication()).getAuthenticationManager();
        
        if (!authManager.authenticate(userName, password)) {
            log.debug("authentication failure: {}/{}", userName, password);
            return false;
        } else {
            this.userName = userName;
            Set<BackofficeRole> rolesForUser = authManager.getRolesForUser(userName);
            ArrayList<String> rolesList = new ArrayList<String>();
            for (BackofficeRole r : rolesForUser) {
                rolesList.add(r.name());
            }
            roles = new Roles(rolesList.toArray(new String[0]));
            
            return true;
        }
    }

    @Override
    public Roles getRoles() {
        return roles;
    }
    
    public String getUserName() {
        return userName;
    }
}
