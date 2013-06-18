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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.users.Configuration;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.manager.UserManager;

@Component
public class MySQLMigrationAdapter implements UserMigrationAdapter{

	private static Logger log = LoggerFactory.getLogger(MySQLMigrationAdapter.class);

	private Configuration configuration;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private UserManager userManager;

	private JdbcTemplate jdbc;

	private BasicDataSource dataSource;

	public MySQLMigrationAdapter() {}

	@Override
	public boolean applicable(Long operatorId) {
		return checkActive();
	}

	private boolean checkActive() {
		return Boolean.parseBoolean(configuration.getProperty("user.service.migration.mysql.active"));
	}

	@Override
	public Long authenticateAndMigrateUser(String userName, String password, Long operatorId) {
		log.info("Authenticate & Migrate user["+userName+"], password["+password+"], operator["+operatorId+"]");
		Long externalUserId = doRemoteAuthentication(userName, password, operatorId);

		if (externalUserId == null) {
			// Authentication failed. Return null
			return null;
		}

		// Authentication ok, migrate user.
		User user = new User();		
		try {
			user.setUserName(userName);
			user.setOperatorId(operatorId);
			user.setExternalId(userName);
			user.addAttribute("migration.type", "MySQL");
			user.addAttribute("migration.date", dateFormat.format(new Date()));
			user.addAttribute("migration.id", externalUserId+"");

			userManager.createUser(user);
			log.info("Migrated user created: "+user);

			userManager.updatePassword(user.getId(), password);

		}catch (Exception e) {
			log.error("Failed to migrate.",e);
		}

		return user.getId();
	}

	
	@Override
	public Long authenticateUser(String userName, String password, Long operatorId) {
		log.info("Remote authentication for "+userName);
		Long externalUserId = doRemoteAuthentication(userName, password, operatorId);
		if (externalUserId != null) {
			log.info("External user id: "+externalUserId);
			User user = userManager.getUserByExternalId(userName, operatorId);
			String xUserId = user.getAttributeValue("migration.id");
			if (xUserId.equals(externalUserId+"")) {
				log.info("External id matches, so return local id. ExternalId["+externalUserId+"] -> NetworkId["+user.getId()+"]");
				// Update password locally (so we have the latest in case we want to switch login model)
				userManager.updatePassword(user.getId(), password);
				return user.getId();
			} else {
				log.info("User was authenticated, but the id's differ. ExternalId["+externalUserId+"] != Network.ExternalId["+xUserId+"] for NetworkId["+user.getId()+"]");
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the legacy MySQL user id.
	 */
	private Long doRemoteAuthentication(String userName, String password, Long operatorId) {
		log.info("Authenticate user["+userName+"], password["+password+"], operator["+operatorId+"]");
		try {
			// Limit to 2 to avoid overloading the user database while still check if we get > 1 results (which is a fail)
			String query = "SELECT * FROM "+get("table")+" WHERE "+get("column.user")+" LIKE '"+userName+"' LIMIT 2";
			List<Map<String, Object>> results = jdbc.queryForList(query);

			if (results.size() == 0) {
				log.info("Authentication failed for ["+userManager+"]. No user found in legacy MySQL that matches query: "+query);
				return null;
			} else if (results.size() > 1) {
				log.info("Authentication failed for ["+userManager+"]. More than one user found in legacy MySQL that matches query: "+query);
				return null;
			} else {
				return checkCredentials(userName, password, results); 
			}

		} catch (Exception e) {
			log.error("Failed to authenticate user. Error message: "+e.getMessage());
			return null;
		}
	}

	private Long checkCredentials(String userName, String password, List<Map<String, Object>> results) {
		// String dbUsername = (String)results.get(0).get(get("column.user"));
		String dbPassword = (String)results.get(0).get(get("column.password"));
		Integer dbId = (Integer)results.get(0).get(get("column.id"));

		if (checkPassword(password, dbPassword)) {
			log.info("Authentication successful for ["+userManager+"]");
			return new Long(dbId);
		} else {
			log.info("Authentication failed for ["+userManager+"]. Bad password");
			return null;
		}

	}

	private void createDataSource() {
		dataSource = new BasicDataSource();
		dataSource.setUrl(get("url"));
		dataSource.setDriverClassName(get("driver"));
		dataSource.setUsername(get("username"));
		dataSource.setPassword(get("password"));
		dataSource.setMaxActive(10);
		dataSource.setMaxIdle(5);
		dataSource.setInitialSize(5);
		dataSource.setValidationQuery("SELECT 1");
	}

	private String get(String key) {
		return configuration.getProperty("user.service.migration.mysql."+key);
	}
	
	private boolean checkPassword(String password, String dbPassword) {
		if (Boolean.parseBoolean(get("md5"))) {
			try {
				String md5 = md5(password);
				log.info("DB md5["+dbPassword+"], Java md5["+md5+"]");
				return dbPassword.equals(md5);
			} catch (Exception e) {
				log.warn("Failed to check MD5 hash of password.", e);
				return false;
			}

		} else {
			return password.equals(dbPassword);
		}
	}

	@Override
	public boolean useLocalAuthentication() {	
		Boolean allowLocalAuth = Boolean.parseBoolean(get("auth.local"));
		log.info("Use local authentication: "+allowLocalAuth);
		return allowLocalAuth;
	}

	@Autowired
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@Autowired
	public void setConfiguration(Configuration config) {
		this.configuration = config;
		if (checkActive()) {
			log.info(" ********* Init MySQL Migration Datasource");
			createDataSource();
			jdbc = new JdbcTemplate(dataSource);
		} else {
			log.info(" ********* MySQL Migration disabled");
		} 
	}

	protected String md5(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] bytes = md.digest(password.getBytes("UTF-8"));
		String md5 = new BigInteger(1, bytes).toString(16); 
		return md5;
	}

}
