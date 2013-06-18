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

package com.cubeia.backoffice.users.integrations.gravatar;

import java.security.MessageDigest;

import org.apache.log4j.Logger;

import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.integrations.UserServicePluginAdapter;

/**
 * Plugin that checks for email and inserts a Gravatar URL attribute if found. 
 * 
 * @author Fredrik
 */
public class GravatarService extends UserServicePluginAdapter {

	private static String GRAVATAR_RESOURCE = "http://www.gravatar.com/avatar/";
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void beforeCreate(User user) {
		UserInformation userInformation = user.getUserInformation();
		if (userInformation != null) {
			String email = userInformation.getEmail();
			if (email != null) {
				String lowerCase = email.toLowerCase();
				MessageDigest digest;
				try {
					digest = java.security.MessageDigest.getInstance("MD5");
					digest.update(lowerCase.getBytes());
					String hash = new String(digest.digest(), "UTF-8");
					String url = GRAVATAR_RESOURCE+hash;
					user.getAttributes().put("gravatar", url);		
				} catch (Exception e) {
					log.error("Could not create Gravatar URL", e);
				}
			}
		}
	}
}
