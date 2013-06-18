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
package com.cubeia.backoffice.operator.service.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;

/**
 * Check if we have any default parameters configured in a property file for new operators.
 * 
 * @author Fredrik
 */
@Component
public class OperatorDefaultParams {

	private static String DEFAULT_PROPERTIES = "operator-default-values.properties";

	Logger log = LoggerFactory.getLogger(getClass());

	public void addDefaultConfiguration(Map<OperatorConfigParameter, String> config) {
		Properties props = load();
		if (props != null) {
			for (Object key : props.keySet()) {
				OperatorConfigParameter param = OperatorConfigParameter.valueOf(key.toString());
				String value = props.getProperty(key.toString());
				config.put(param, value);
			}
		} else {
			log.info("No default operator configuration specified by operator-default-values.properties");
		}
	}

	private Properties load() {
		Properties props = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
		if (inputStream != null) {
			try {
				props.load(inputStream);
				log.info("Operator Default Values are set as: "+props);
				return props;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}



}
