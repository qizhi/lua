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
package com.cubeia.firebase.api.service.sysstate;

public class SystemStateConstants {
	
	public static final String TABLE_ROOT_NODE = "table";
	public static final String TABLE_ROOT_FQN = "/"+TABLE_ROOT_NODE+"/";
	public static final String TABLE_GAMEID_MAP_FQN = "/_tableGameId";
	public static final String TABLE_IDGENERATION_FQN = "/_tableIdGeneration";
	public static final String CHAT_CHANNEL_ROOT_FQN = "/chat/";
	public static final String GAME_ROOT_FQN = "/game/";
	
	public static String CLIENT_TABLES = "/table";
	public static String CLIENT_WATCHING = "/watch";
	
	public static final String TOURNAMENT_ROOT_NODE = "tournament";
	public static final String TOURNAMENT_ROOT_FQN = "/"+TOURNAMENT_ROOT_NODE+"/";
    public static final String TOURNAMENT_TABLE_ROOT_FQN = "/tournament_tables/";
    public static final String MTT_META_ROOT_FQN = "/_mtt/";
    public static final String MTT_IDGENERATION_FQN = "/_mttIdGeneration";
    public static final String MTT_TABLEMAP_ROOT_FQN = MTT_META_ROOT_FQN + "tables/";
    
    public static final String MTT_PLAYERREG_ROOT_FQN = MTT_META_ROOT_FQN + "registry/";
    public static final String MTT_PLAYERREG_PLAYER_FQN = MTT_PLAYERREG_ROOT_FQN + "player/";
    public static final String MTT_PLAYERREG_MTT_FQN = MTT_PLAYERREG_ROOT_FQN + "mtt/";
	
	public static final String CLIENT_ROOT_FQN = "/client/";
	
}
