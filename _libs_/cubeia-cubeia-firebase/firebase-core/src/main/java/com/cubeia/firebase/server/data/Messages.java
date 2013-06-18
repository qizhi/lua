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
package com.cubeia.firebase.server.data;
/**
 * 
 * Created on 2006-sep-05
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class Messages {

    public static final String delimiter = "-------------\n";
    
   public static final String welcome = "Welcome to the poker server.\nType help for more information.";
   
   public static final String help_lobby =  "help       - Display the available list of commands. \n" +
                                            "list       - List available tables \n" +
                                            "join <id>  - Join table with id <i> \n" +
                                            "quit       - Disconnect\n";
   
   public static final String help_table =  "help - Display the available list of commands. \n" +
                                            "bet <id> <amount - Bet <amount> on table <id> \n" +
                                            "call <id> - Call table <id>\n" +
                                            "fold <id> - Fold table <id>\n";
    
   
   
   public static String box(String msg) {
       return delimiter+msg+delimiter;
   }
   
}

