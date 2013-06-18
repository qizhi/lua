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
package com.cubeia.firebase.server;

public class Constants {
	
	public static boolean FORCE_SAR_REDEPLOY = System.getProperty("com.cubeia.firebase.service.forceSarExpansion", "true").equals("true");
	
	public static final boolean IN_ECLIPSE = System.getProperty("eclipse", "false").equals("true");

	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String FIREBASE_HOME = System.getProperty("FIREBASE_HOME", USER_DIR);
	
    public static final String GAME_QUEUE_PREFIX = "gameGame";
    public static final String GAME_TOPIC_NAME = "gameClientBroadcast";
    public static final String CHAT_TOPIC_NAME = "chatClientBroadcast";

	public static final String CLUSTER_CONFIG_SERVICE_NS = "ns://www.cubeia.com/firebase/config/service/cluster";
	public static final String SERVER_CONFIG_SERVICE_NS = "ns://www.cubeia.com/firebase/config/service/server";
	public static final String CONNECTION_SERVICE_NS = "ns://www.cubeia.com/firebase/clusterConnection/service";
	public static final String GOS_SERVICE_NS = "ns://www.cubeia.com/firebase/space/treecache/service";
	public static final String DEPMAN_SERVICE_NS = "ns://www.game.com/depman/service"; 
    
	/** Regex to find deployment definitions */
	public static final String DATASOURCE_DEF_TAIL = "-ds.xml";
	public static final String DATASOURCE_DEF_REGEX = "..*."+DATASOURCE_DEF_TAIL;
	
	public static final String GAME_ARCHIVE_DEF_TAIL = ".gar";
	public static final String GAME_ARCHIVE_DEF_REGEX = "..*"+GAME_ARCHIVE_DEF_TAIL;
	
	public static final String MTT_ARCHIVE_DEF_TAIL = ".tar";
	public static final String MTT_ARCHIVE_DEF_REGEX = "..*"+MTT_ARCHIVE_DEF_TAIL;
	
	public static final String GAR_ACTIVATION_CONFIG_DEF_TAIL = "-ga.xml";
	public static final String GAR_ACTIVATION_CONFIG_DEF_REGEX = "..*."+GAR_ACTIVATION_CONFIG_DEF_TAIL;
	
	public static final String TAR_ACTIVATION_CONFIG_DEF_TAIL = "-ta.xml";
	public static final String TAR_ACTIVATION_CONFIG_DEF_REGEX = "..*."+TAR_ACTIVATION_CONFIG_DEF_TAIL;
	
	public static final String PERSISTENCE_DEF_TAIL = ".par";
	public static final String PERSISTENCE_DEF_REGEX = "..*."+PERSISTENCE_DEF_TAIL;
	
	public static final String TABLE_CREATION_COMMAND_CHANNEL = "tableCreationChannel";
	public static final String MTT_CREATION_COMMAND_CHANNEL = "mttCreationChannel";
	
	public static final String NODE_LIFETIME_COMMAND_CHANNEL = "nodeLifeChannel";
	public static final String DEFAULT_COMMAND_CHANNEL = "defaultChannel";
	
	/** Deployment constants */
	// Time between checking for updated deployments in ms 
	public static final long DEPLOY_SCAN_REFRESH_PERIOD = 300;

	public static final String SERVER_ID_KEY = "serverId";



}
