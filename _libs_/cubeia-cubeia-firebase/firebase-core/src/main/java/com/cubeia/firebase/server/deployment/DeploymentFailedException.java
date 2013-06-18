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
package com.cubeia.firebase.server.deployment;

import com.cubeia.firebase.server.instance.SystemCoreException;

/**
 * Exception that will be used if a deployment failed for any
 * reason.
 * 
 * @author Fredrik
 *
 */
public class DeploymentFailedException extends SystemCoreException {

	/** Version id */
	private static final long serialVersionUID = 1L;

	public DeploymentFailedException() {
		super();
	}

	public DeploymentFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DeploymentFailedException(String msg) {
		super(msg);
	}

	public DeploymentFailedException(Throwable cause) {
		super(cause);
	}
	
	

}
