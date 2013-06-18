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
package com.cubeia.firebase.server.gateway.comm.mina.protocol;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.PingPacket;
import com.cubeia.firebase.server.gateway.comm.crypto.AESCryptoProvider;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoConstants;

public class StyxEncoder implements ProtocolEncoder {

	private static transient Logger log = Logger.getLogger(StyxEncoder.class);
	
	/** Making static to avoid creating new all the time */
	private static StyxSerializer styx = new StyxSerializer(null);
	
	/**
	 * Encodes higher-level message objects into binary or protocol-specific data. 
	 * MINA invokes encode(IoSession, Object, ProtocolEncoderOutput)  
	 * method with message which is popped from the session write queue, and then 
	 * the encoder implementation puts encoded ByteBuffers into ProtocolEncoderOutput.
	 */
	/*
	 * TODO: Currently only protocol objects are encrypted, should we look at the
	 * byte arrays as well?
	 */
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if (message instanceof ProtocolObject) {
			ProtocolObject packet = (ProtocolObject)message;
			
			// If crypto is in use, encrypt packet.
			// Somewhat of a hack: don't encrypt ping packets /LJN
			if (session.containsAttribute("usecrypto") && !(packet instanceof PingPacket)) {
				packet = encrypt(session, packet);
			}			
			
        	ByteBuffer packed = styx.pack(packet);
        	
        	// MINA uses it's own cute little ByteBuffer implementation
        	out.write(org.apache.mina.common.ByteBuffer.wrap(packed));
        	
		} else if (message instanceof org.apache.mina.common.ByteBuffer ){
			// this is a message from another filter, encryption anyone?
			out.write((org.apache.mina.common.ByteBuffer) message);
			
		} else if (message instanceof ByteBuffer ){
		    ByteBuffer data = (ByteBuffer)message;
		    out.write(org.apache.mina.common.ByteBuffer.wrap(data));
		} else if (message instanceof byte[]) {
			out.write(org.apache.mina.common.ByteBuffer.wrap((byte[])message));
		} else {
			// this is also a message from another filter
			// log and try to send it
			log.warn("Unknown message object encountered: "+message);
			out.write((org.apache.mina.common.ByteBuffer) message);
		}
	}
	
	private ProtocolObject encrypt(IoSession session, ProtocolObject packet) throws Exception {
		// get crypto provider
		AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
		// get data to be encrypted
		ByteBuffer buffer = styx.pack(packet);
		
		// create EncryptedTransportPacket
		EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
		encryptedTransportPacket.func = CryptoConstants.ENCRYPTED_DATA;
		// encrypt data
		encryptedTransportPacket.payload = cryptoProvider.encrypt(buffer.array());
		
		return encryptedTransportPacket;
	}
	
	/**
	 * Release all resources related with this encoder.
	 */
	public void dispose(IoSession session) throws Exception {}
	

}
