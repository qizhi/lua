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
package com.cubeia.backoffice.users.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.cubeia.backoffice.users.Configuration;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.manager.UserManager;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.FacebookXmlRestClient;
import com.google.code.facebookapi.IFacebookRestClient;
import com.google.code.facebookapi.ProfileField;

@Component
public class FacebookMigrationAdapter implements UserMigrationAdapter {

	private static Logger log = LoggerFactory.getLogger(FacebookMigrationAdapter.class);
	private Configuration configuration;
	
	private final static String FB_AVATAR_ATTRIBUTE_KEY = "FB_AVATAR";
	
	private UserManager userManager;
	
	@Override
	public boolean applicable(Long operatorId) {
		return Boolean.parseBoolean(configuration.getOperatorProperty("user.service.migration.facebook.active", operatorId));
	}

	@Override
	public Long authenticateAndMigrateUser(String userName, String password,
			Long operatorId) {
		authenticateUser(userName, password, operatorId);		
		User user = new User();		
		
		try {
			// Update user from fb
			IFacebookRestClient<?> fbClient = new FacebookXmlRestClient(getFbKey(operatorId), getFbSecret(operatorId), password);
			EnumSet<ProfileField> fields = EnumSet.of(
					ProfileField.PROXIED_EMAIL, 
					ProfileField.FIRST_NAME,
					ProfileField.LAST_NAME, 
					ProfileField.PIC_SQUARE);
			Collection<Long> users = new ArrayList<Long>();
			users.add(fbClient.users_getLoggedInUser());

			// Get fb information
			Document fbDoc = (Document) fbClient.users_getInfo(users, fields);
			String email = fbDoc.getElementsByTagName(ProfileField.PROXIED_EMAIL.fieldName()).item(0).getTextContent();
			String firstName = fbDoc.getElementsByTagName(ProfileField.FIRST_NAME.fieldName()).item(0).getTextContent();
			String lastName = fbDoc.getElementsByTagName(ProfileField.LAST_NAME.fieldName()).item(0).getTextContent();
			String fbAvatar = fbDoc.getElementsByTagName(ProfileField.PIC_SQUARE.fieldName()).item(0).getTextContent();
						
			user.setUserName(userName);
			user.getInformation().setEmail(email);
			user.getInformation().setFirstName(firstName);
			user.getInformation().setLastName(lastName);
			user.setOperatorId(operatorId);
			user.setExternalId(userName);
			
			user.addAttribute(FB_AVATAR_ATTRIBUTE_KEY, fbAvatar);
			
			userManager.createUser(user);
		}catch (Exception e) {
			log.error("Failed to migrate.",e);
		}
		
		return user.getId();
	}

	@Override
	public Long authenticateUser(String userName, String password, Long operatorId) {
		IFacebookRestClient<?> client = new FacebookJsonRestClient(getFbKey(operatorId), getFbSecret(operatorId), password);
		try {
			Long fbId = client.users_getLoggedInUser();
			User loggedInUser = userManager.getUserByExternalId(""+fbId, operatorId);
			return loggedInUser == null ? null : loggedInUser.getId();
		} catch (FacebookException e) {
			log.error("Failed to authenticate",e);
			return null;
		}
	}

	@Override
	public boolean useLocalAuthentication() {		
		return false;
	}

	@Autowired
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@Autowired
	public void setConfiguration(Configuration config) {
		this.configuration = config;
	}
	
	public String getFbKey(Long operatorId) {
		return configuration.getOperatorProperty("user.service.migration.facebook.key", operatorId);
	}
	
	public String getFbSecret(Long operatorId) {
		return configuration.getOperatorProperty("user.service.migration.facebook.secret", operatorId);
	}
		
}
