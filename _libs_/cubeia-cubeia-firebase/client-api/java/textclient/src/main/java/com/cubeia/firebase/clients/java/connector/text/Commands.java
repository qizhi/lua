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
package com.cubeia.firebase.clients.java.connector.text;

public class Commands {
    
	
	/* --------- FIREBASE COMMANDS -----------*/
	
	public static final String HELP = "help";
    public static final String EXIT = "exit";
    public static final String VERSION = "version";
    public static final String GAME_VERSION = "gversion"; // gversion <GID>
    public static final String LOGIN = "login";         // login <user> <pwd>
    public static final String LOGOUT = "logout";       // logout (true)
    public static final String WATCH_TABLE = "watch";   // watch <TID>
    public static final String STOP_WATCH_TABLE = "unwatch";   // unwatch <TID>
    public static final String SIT_AT_TABLE = "join";   // seat <TID> <seat#> <buyin>
    public static final String LEAVE_TABLE = "leave";   // leave <TID>
    public static final String JOIN_CHAT_CHANNEL = "joinchat";       // joinchat <CID>
    public static final String LEAVE_CHAT_CHANNEL = "leavechat";       // leavechat <CID>
    public static final String CHANNEL_CHAT = "chat";       // chat <CID> <msg>
    public static final String TABLE_CHAT = "say";       // say <TID> <msg>
    public static final String PROBE = "probe";
    public static final String SERVICE = "service";
    public static final String PLAYER_QUERY = "pq";  // pq <PID>
    public static final String TABLE_QUERY = "tq";  // tq <PID>
    public static final String LOCAL_SERVICE = "loc"; // Send string to local service <string>
    public static final String CREATE_TABLE = "create"; // Request table creation
    public static final String INVITE = "invite"; // Invite players to a table
    public static final String SYSTEM_INFO = "sysinfo"; // Invite players to a table
    
    // Lobby Commands
    public static final String LIST = "list";
    public static final String FILTERED_JOIN_TABLE = "fjoin";   // fjoin <FQN> (params)
    public static final String CANCEL_FILTERED_JOIN_TABLE = "cfjoin";   // fjoin <FQN> (params)
    public static final String SUBSCRIBE = "sub";
    public static final String UNSUBSCRIBE = "unsub";
    public static final String SUBSCRIBE_OBJECT = "subo";
    public static final String UNSUBSCRIBE_OBJECT = "unsubo";

    // MTT Commands
    public static final String MTT_REG = "mttreg";
    public static final String MTT_UNREG = "mttunreg";

}