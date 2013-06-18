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
package com.cubeia.firebase.server.gateway.comm.jetty;

import static com.cubeia.firebase.server.gateway.comm.jetty.JettyServer.HANDSHAKE_HTTP_HEADER;

import java.net.InetSocketAddress;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

class HttpUtil {

	private HttpUtil() { }
	
	public static InetSocketAddress createSocketAddress(HttpServletRequest req) {
		String ip = req.getRemoteAddr();
		int port = req.getRemotePort();
		InetSocketAddress address = new InetSocketAddress(ip, port);
		return address;
	}
	
	public static boolean checkHandshake(HttpServletRequest request, String handShake) {
		if(handShake != null) {
			String theirs = getRequestHandshake(request);
			if(handShake.equals(theirs)) { // match on string to avoid failing on non-integer values
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public static String getRequestHandshake(HttpServletRequest request) {
		String msg = request.getHeader(HANDSHAKE_HTTP_HEADER);
		if(msg == null) {
			msg = findHandshakeCookie(request);
		}
		if(msg == null) {
			msg = request.getParameter(HANDSHAKE_HTTP_HEADER);
		}
		return msg;
	}
	
	private static String findHandshakeCookie(HttpServletRequest request) {
		for (Cookie c : request.getCookies()) {
			if(c.getName().equals(HANDSHAKE_HTTP_HEADER)) {
				return c.getValue();
			}
		}
		return null;
	}
}
