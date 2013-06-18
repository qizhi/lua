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
package com.cubeia.firebase.server.deployment.datasource;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.service.datasource.TxType;

public interface DatasourceDeployment {

	public static final String NON_TX_PROPERTY = "NON-TX";
	public static final String TX_TYPE_KEY = "tx-type";
	public static final String DRIVER_KEY = "driver";
	public static final String USER_KEY = "user";
	public static final String PASSWORD_KEY = "password";
	public static final String URL_KEY = "url";
	// public static final String BLOCK_TIMEOUT_KEY = "block-timeout";
	public static final String VALIDATION_STM_KEY = "validation-statement";
	public static final String TIMEOUT_KEY = "ttl";
	public static final String MAX_SIZE_KEY = "max-pool-size";
	public static final String MIN_SIZE_KEY = "min-pool-size";
	public static final String POOL_SIZE_KEY = "pool-size";
	public static final String XA_DATA_SOURCE_KEY = "xa-data-source";
	public static final String CHECKOUT_TIMEOUT_KEY = "checkout-timeout";
	
	public static final int POOL_SIZE_DEF = 5;
	public static final int BLOCK_TIMEOUT_DEF = 5000; // MILLLIS
	public static final int TIMEOUT_DEF = 240; // SECONDS
	public static final int MAX_SIZE_DEF = 10;
	public static final int MIN_SIZE_DEF = 2;
	public static final int CHECKOUT_TIMEOUT_DEF = 60 * 1000; // MILLIS

	public abstract String getArtifactName();

	public abstract String getArtifactVersion();

	/**
	 * Read the definition from a datasource file and create a datasource.
	 * 
	 * @param resource
	 * @param registry 
	 * @throws IOException
	 */
	public abstract void configure(DeploymentResource resource,
			ServiceRegistry registry) throws DeploymentFailedException,
			IOException;

	public abstract Properties getProperties();

	public abstract TxType getTxType();

	/**
	 * Get the datasource as created from the deployment descriptor.
	 * @return
	 */
	public abstract DataSource getDatasource();

}