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
package com.cubeia.firebase.api.login.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.ConfigurationException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.util.Arguments;

/**
 * This login handler uses a table and username and password columns to 
 * check a login name and password. It uses exact string matching. Sub-classes 
 * may override the {@link #createStatement(Connection con)} method
 * to supply specialized SQL.
 * 
 * <p>The statement created in {@link #createStatement(Connection con)} should use
 * the username as parameter 1 and password as parameter 2, and it should select the 
 * user id as an int only.
 * 
 * @author Larsan
 * @date 2007 maj 24
 */
public class JDBCLoginHandler implements LoginHandler {
	
	private final Logger log = Logger.getLogger(getClass());

	protected final String passwordColumn;
	protected final String userColumn;
	protected final String idColumn;
	protected final String tableName;
	protected final DataSource datasource;

	/**
	 * @param ds Data source to use, must not be null
	 * @param tableName Table name, must not be null
	 * @param idColumn User id column name, must not be null
	 * @param userColumn User column name, must not be null
	 * @param passwordColumn Password column name, must not be null
	 */
	public JDBCLoginHandler(DataSource ds, String tableName, String idColumn, String userColumn, String passwordColumn) {
		Arguments.notNull(ds, "ds");
		Arguments.notNull(tableName, "tableName");
		Arguments.notNull(idColumn, "idColumn");
		Arguments.notNull(userColumn, "userColumn");
		Arguments.notNull(passwordColumn, "passwordColumn");
		this.idColumn = idColumn;
		this.userColumn = userColumn;
		this.passwordColumn = passwordColumn;
		this.tableName = tableName;
		this.datasource = ds;
	}
	
	/**
	 * @param ds Data source to use, must not be null
	 * @param conf Configuration to take values from, must not be null
	 * @throws ConfigurationException If values cannot be read from config
	 */
	public JDBCLoginHandler(DataSource ds, JDBCLoginConfig conf) {
		this(ds, conf.getTableName(), conf.getIdColumn(), conf.getUserColumn(), conf.getPasswordColumn());
	}
	
	
	// --- LOGIN HANDLER --- //
	
	public LoginResponseAction handle(LoginRequestAction request) {
		Connection con = null;
		LoginResponseAction response = null;
		try {
			con = datasource.getConnection();
			PreparedStatement pst = createStatement(con);
			pst.setString(1, request.getUser());
			pst.setString(2, request.getPassword());
			ResultSet rs = pst.executeQuery();
			if(!rs.next()) {
				response = new LoginResponseAction(false, -1);
				response.setErrorMessage(getNotFoundErrorMessage());
				response.setErrorCode(getNotFoundErrorCode());
			} else {
				int id = rs.getInt(1);
				response = new LoginResponseAction(true, id);
				response.setScreenname(request.getUser());
			}
		} catch (SQLException e) {
			response = new LoginResponseAction(false, -1);
			response.setErrorMessage(getSystemErrorMessage(e));
			response.setErrorCode(getSystemErrorCode(e));
			log.error(e);
		} finally {
			safeClose(con);
		}
		return response;
	}
	

	// --- PROTECTED METHODS --- //

	/**
	 * Create a string query to use in order to check for a user
	 * and password with the username as parameter 1 and password as
	 * parameter 2. The query should return a unique result if the user can 
	 * be found with the correct password and select the user id as an inte only. 
	 * The basic implementation of this method returns:
	 * 
	 * <pre>
	 * 		select <idColumn> from <tableName> where <userColumn> = ? and <passwordColumn> = ?;
	 * </pre>
	 * 
	 * @param con Connection to create statement for, never null
	 * @throws SQLException 
	 */
	protected PreparedStatement createStatement(Connection con) throws SQLException {
		return con.prepareStatement("select " + idColumn + " from " + tableName + " where " + userColumn + " = ? and " + passwordColumn + " = ?");
	}
	
	/**
	 * This method should return the message to send back if the sql query
	 * does not get any results. Default msg is "User not found."
	 * 
	 * @return The "user not found" error message, may be null
	 */
	protected String getNotFoundErrorMessage() {
		return "User not found.";
	}
	
	/**
	 * This method should return the error code to send back if the sql query
	 * does not get any results. Default msg is 0.
	 * 
	 * @return The "user not found" error code
	 */
	protected int getNotFoundErrorCode() {
		return 0;
	}
	
	/**
	 * This method should return the error code to send back if the sql query
	 * fails. Default msg is 0.
	 * 
	 * @param e The sql exception, never null
	 * @return The system error code
	 */
	protected int getSystemErrorCode(SQLException e) {
		return 0;
	}

	
	/**
	 * This method should return the error message to send back if the sql query
	 * fails. Default msg is "System error."
	 * 
	 * @param e The sql exception, never null
	 * @return The system error message
	 */
	protected String getSystemErrorMessage(SQLException e) {
		return "System error.";
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void safeClose(Connection con) {
		if(con == null) return;
		try {
			con.close();
		} catch(SQLException e) {
			log.error(e);
		}	
	}
}
