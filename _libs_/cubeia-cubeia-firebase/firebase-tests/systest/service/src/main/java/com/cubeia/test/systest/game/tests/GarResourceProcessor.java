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
package com.cubeia.test.systest.game.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.GarResourceRequestPacket;
import com.cubeia.test.systest.io.protocol.GarResourceResponsePacket;
import com.cubeia.test.systest.io.protocol.Enums.ResponseStatus;

public class GarResourceProcessor extends ProcessorBase {
	
	public void doTest(ServiceContext con, ServiceRouter router, ProtocolObject gamePacket, List<Attribute> attributes) {
		GarResourceRequestPacket packet = (GarResourceRequestPacket) gamePacket;
		GarResourceResponsePacket resp = new GarResourceResponsePacket(ResponseStatus.OK, packet.seq);
		resp.status = doTest(con);
		router.dispatchToPlayer(packet.playerId, new ClientServiceAction(packet.playerId, packet.seq, toBytes(resp)));
	}
	
	// --- PRIVATE METHODS --- //

	private ResponseStatus doTest(ServiceContext con) {
		try {
			ResourceLocator loc = con.getResourceLocator();
			InputStream stream = loc.openResource("/doc/resource.txt");
			if(stream == null) {
				Logger.getLogger(getClass()).error("Could not find resource!");
				return ResponseStatus.FAILED; // EARLY RETURN
			} else {
				if(!toString(stream).equals("verificationstring")) {
					Logger.getLogger(getClass()).error("Failed to read string!");
					return ResponseStatus.FAILED; // EARLY RETURN
				}
			}
			stream = loc.openResource("c:\\AUTOEXEC.BAT");
			if(stream != null) {
				Logger.getLogger(getClass()).error("Managed to read system file!");
				return ResponseStatus.FAILED; // EARLY RETURN
			}
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to read resource!", e);
			return ResponseStatus.FAILED;
		}
		return ResponseStatus.OK;
	}
	
	private String toString(InputStream stream) throws IOException {
		StringBuilder b = new StringBuilder();
		Reader r = new InputStreamReader(stream);
		int ch;
		while((ch = r.read()) != -1) {
			b.append((char)ch);
		}
		r.close();
		return b.toString();
	}
}
