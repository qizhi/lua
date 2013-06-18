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
package com.cubeia.firebase.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Sql {
	
	private Sql() { }
	
    /**
     * Use database meta data to check if a table exists.
     * 
     * @param con Connection to use to the database, must not be null
     * @param table Table name to check for, will be checked case insensitive, must not be null
     * @return true if the table exists, false otherwise
     * @throws SQLException On database errors.
     */

	public static boolean tableExists(Connection con, String table) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet rs = meta.getTables(null, null, "%", null);
        try {
            while(rs.next()) {
                String test = rs.getString("TABLE_NAME");
                if(test != null && test.equalsIgnoreCase(table)) {
                    return true;
                }
            }
            return false;
        } finally {
            safeClose(rs);
        }
    }
	
    /**
     * @param rs Result to close without exception, may be null
     */

    public static  void safeClose(ResultSet rs) {
        try {
            if(rs != null) rs.close();
        } catch(SQLException e) { }
    }
    
    
    /**
     * @param conn Connection tio close without exception, may be null
     */

    public static void safeClose(Connection conn) {
        try {
            if(conn != null) conn.close();
        } catch(SQLException e) { }
    }
    
    
    /**
     * @param st Statement to close without an exception, may be null
     */

    public static void safeClose(Statement st) {
        try {
            if(st != null) st.close();
        } catch(SQLException e) { }
    }
}
