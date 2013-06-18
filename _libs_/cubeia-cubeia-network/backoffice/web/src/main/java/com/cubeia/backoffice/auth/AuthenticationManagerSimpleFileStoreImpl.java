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

package com.cubeia.backoffice.auth;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component("authenticationManager")
public class AuthenticationManagerSimpleFileStoreImpl implements AuthenticationManager {
    private static final String DEFAULT_BACKOFFICE_USER_LIST_XML = "backoffice-user-list-default.xml";
    private static final String OVERRIDE_BACKOFFICE_USER_LIST_XML = "backoffice-user-list.xml";
    private Logger log = LoggerFactory.getLogger(AuthenticationManagerSimpleFileStoreImpl.class);
    private Map<String, BackofficeUserDTO> userMap = new HashMap<String, BackofficeUserDTO>();
    
    public AuthenticationManagerSimpleFileStoreImpl() {
        try {
            JAXBContext ctx = JAXBContext.newInstance(
                BackofficeUserDTO.class, 
                BackofficeUserListDTO.class);
            
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            
            InputStream is = null;
            
            try {
            	is = new ClassPathResource(OVERRIDE_BACKOFFICE_USER_LIST_XML).getInputStream();
            	log.debug("Using {} for admin authentication.", OVERRIDE_BACKOFFICE_USER_LIST_XML);
            } catch (Exception e) {// Ignore            	
            }
            
            if(is == null){
            	log.debug(OVERRIDE_BACKOFFICE_USER_LIST_XML + " not found, falling back to " + DEFAULT_BACKOFFICE_USER_LIST_XML);
	            try {
	            	is = new ClassPathResource(DEFAULT_BACKOFFICE_USER_LIST_XML).getInputStream();
	            	log.debug("Using {} for admin authentication.", DEFAULT_BACKOFFICE_USER_LIST_XML);
	            } catch (Exception e) {
	            	log.error("No user definition file was found.");
	            }
            }
            
            if (is == null) {
                throw new RuntimeException("resource not found: " + DEFAULT_BACKOFFICE_USER_LIST_XML);
            }           
            
            BackofficeUserListDTO userList = (BackofficeUserListDTO) unmarshaller.unmarshal(is);
            for (BackofficeUserDTO u : userList.getUsers()) {
                log.debug("Found user: {}/{}", u.getUserName(), u.getPassword());
                userMap.put(u.getUserName(), u);
            }
            
            log.debug("read {} users from file store", userMap.size());
        } catch (Exception e) {
            log.error("error reading user list", e);
            throw new RuntimeException("error reading user list", e);
        }
    }

    @Override
    public boolean authenticate(String userName, String password) {
        BackofficeUserDTO u = userMap.get(userName);
        if (u == null) {
            return false;
        } else {
            return u.getPassword().equals(password);
        }
    }

    @Override
    public Set<BackofficeRole> getRolesForUser(String userName) {
        BackofficeUserDTO u = userMap.get(userName);
        if (u == null) {
            return null;
        } else {
            return new HashSet<BackofficeRole>(u.getRoles());
        }
    }
}
