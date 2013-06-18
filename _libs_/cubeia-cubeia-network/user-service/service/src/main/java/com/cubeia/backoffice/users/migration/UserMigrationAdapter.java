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

/**
 * Migration Adapter Interface
 * 
 * Implement this to provide 3rd party integration and migration.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface UserMigrationAdapter {
	
	
	/**
	 * Check if this migrator should be used for the given operator id.
	 * 
	 * @param operatorId the id of the operator to check if this adapter is applicable for
	 * @return true if the migrator should be used.
	 */
	boolean applicable(Long operatorId);
	
	
	/**
	 * If true is returned, then the User Service will try and authenticate as usual
	 * if the User exists in the database. If the User does not exist, then the authenticate and 
	 * migrate method will be called.
	 * 
	 * In short;
	 * 
	 * return true  AND user exists     -> Regular authentication. No further calls to this interface will be made.
	 * return true  AND no user exists  -> Remote authentication & migration.  authenticateAndMigrateUser will be called.
	 * return false AND user exists     -> Remote authentication. authenticateUser will be called.
	 * return false AND no user exists  -> Remote authentication & migration.  authenticateAndMigrateUser will be called.
	 * 
	 * @return true or false as per above schema
	 */
	boolean useLocalAuthentication();
	
	/**
	 * Authenticate towards 3rd party.
	 * 
	 * @param userName
	 * @param password
	 * @param operatorId
	 * 
	 * @return User id
	 */
	public Long authenticateUser(String userName, String password, Long operatorId);
	
	/**
	 * Authenticate towards 3rd party and perform a migration (i.e. store the User details in the db).
	 * 
	 * @param userName
	 * @param password
	 * @param operatorId
	 * 
	 * @return User id
	 */
	public Long authenticateAndMigrateUser(String userName, String password, Long operatorId);
	
	
}
