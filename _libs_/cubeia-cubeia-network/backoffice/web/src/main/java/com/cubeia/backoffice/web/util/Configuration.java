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

package com.cubeia.backoffice.web.util;

import java.io.IOException;
import java.util.Properties;

import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;

public class Configuration {
	
	private final Logger log = Logger.getLogger(getClass());

	private Properties defaultProperties;	
	private Properties overrideProperties;
	
	@Autowired
    private UserServiceClient userService;
	
	@Autowired
    private WalletServiceClient walletService;

    @Autowired
    private OperatorServiceClient operatorService;
	
	public void init() throws IOException {
		if(overrideProperties != null) {
			log.debug("Overriding default properties with: " + overrideProperties);
			defaultProperties.putAll(overrideProperties);
		}
		userService.setBaseUrl(getProperty("user.service.url"));
		walletService.setBaseUrl(getProperty("wallet.service.url"));
        operatorService.setBaseUrl(getProperty("operator.service.url"));

	}

	public String getProperty(String key) {
		return defaultProperties.getProperty(key);
	}
	
	public String getOperatorProperty(String key, Long operatorId) {
		String value = getProperty(key + ".operator." + operatorId);
		if(value == null){
			value = getProperty(key);
		}
		return value;
	}

	public boolean isPasswordEncryptionEnabled(){
		return Boolean.parseBoolean(getProperty("user.service.password.encryption.enabled"));
	}
	
	public Properties getDefaultProperties() {
		return defaultProperties;
	}
	
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}
	
	public Properties getOverrideProperties() {
		return overrideProperties;
	}
	
	public void setOverrideProperties(Properties overrideProperties) {
		this.overrideProperties = overrideProperties;
	}
}
