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
package com.cubeia.firebase.test.common;

public class Constants {
	
	public static final boolean USE_SSL = System.getProperty("firebase.systest.ssl", "false").equals("true");
	public static final boolean USE_NAIVE_SSL = System.getProperty("firebase.systest.naive-ssl", "false").equals("true");
	public static final boolean USE_NATIVE = System.getProperty("firebase.systest.native", "false").equals("true");
	
	public static final String COMETD_PATH = System.getProperty("firebase.systest.cometd.path", "/cometd");
	// public static final String HTTP_PATH = System.getProperty("firebase.systest.http.path", "/http");
	public static final String SOCKET_PATH = System.getProperty("firebase.systest.websocket.path", "/socket");
	
	public static final ConnectorType DEFAULT_CONNECTOR;
	
	static {
		String s = System.getProperty("firebase.systest.connector-type", "SOCKET");
		DEFAULT_CONNECTOR = ConnectorType.valueOf(s);
		if(DEFAULT_CONNECTOR == null) {
			throw new RuntimeException("unknown connector type: " + s);
		}
	}
}
