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
package com.cubeia.firebase.server.gateway;

import java.io.File;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Inheritance;
import com.cubeia.firebase.api.server.conf.Property;
import com.cubeia.firebase.server.node.NodeConfig;

@Configurated(inheritance=Inheritance.ALLOW)
public interface GatewayConfig extends NodeConfig {
	
	/**
	 * @return The number of threads for the scheduler, defaults to 4
	 */
	@Property(defaultValue="16") 
	public int getEventDaemonThreads();
	
	/**
	 * @return The connection timeout for clients in millis, default to 60 secs
	 */
	@Property(defaultValue="60000") 
	public long getClientReconnectTimeout();
	
	/**
	 * @return The number of connector threads in the underlying IO layer, defaults to 4
	 */
	@Property(defaultValue="4")
	public int getAcceptorIoThreads();

	/**
	 * @return Max frequency in millis, or -1 for no limit
	 */
	public long getLocalPacketMaxFixedAccessFrequency();
	
	/**
	 * @return The interval length in millis, or -1 for disabling
	 */
	public long getLocalPacketIntervalAccessFrequencyLength();
	
	/**
	 * @return The number of accesses within the interval, or -1 for disabling
	 */
	public int getLocalPacketIntervalAccessFrequency();
	
	/**
	 * @return True to enable HTTP and WebSocket access, or false for disabling
	 */
	@Property(defaultValue="true") 
	public boolean enableHttpServer();
	
	/**
	 * @return True to disable static HTTP content, or false for to enable
	 */
	@Property(defaultValue="false") 
	public boolean disableStaticHttpContent();
	
	/**
	 * @return The directory from which to serve static content, defaults to "game/web"
	 */
	@Property(defaultValue="game/web")
	public File getStaticWebDirectory();

	/**
	 * @return True to allow static web content directory listing, defaults to "false"
	 */
	@Property(defaultValue="false")
	public boolean allowStaticWebDirectoryListing();

	/**
	 * @return The max size for JSON text messages (from web socket server), defaults to 512000
	 */
	@Property(defaultValue="512000")
	public long getJsonMaxTextMessageSize();
	
	/**
	 * @return The max idle time for the websockets, defaults to 300000 (5 min)
	 */
	@Property(defaultValue="300000")
	public long getWebSocketMaxIdleTimeout();
		
	/**
	 * @return The time in milliseconds the server will hold a long poll before releasing it, defaults to 30000
	 */
	@Property(defaultValue="30000")
	public long getCometPollTimeout();
	
	/**
	 * @return The time in milliseconds without a comet poll request after which the server regards the connection as idle, defaults to 5000
	 */
	@Property(defaultValue="5000")
	public long getCometIdleTimeout();
	
	/**
	 * @return True if the cross-origin filter should be enabled, false otherwise
	 */
	@Property(defaultValue="true")
	public boolean enableHttpCrossOriginFilter();
	
}
