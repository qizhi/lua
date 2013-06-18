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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.server.gateway.comm.crypto.AESCryptoProvider;
import com.cubeia.firebase.server.gateway.comm.crypto.AESSessionKey;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoConstants;
import com.cubeia.firebase.server.gateway.comm.crypto.RSAKeyExchange;
import com.cubeia.firebase.server.gateway.comm.mina.MServer;


/**
 * Protocol decoder for the firebase wireprotocol.
 * 
 * @author Fredrik
 *
 */
public class StyxDecoder extends CumulativeProtocolDecoder {
	
	private static final Logger log = Logger.getLogger(StyxDecoder.class);
	
	/** Why create a new one when we can have one to rule them all */
	private static StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());

	private final boolean forceEncryption;
	
	public StyxDecoder(boolean forceEncryption) {
		this.forceEncryption = forceEncryption;
	}

	/**
	 * Decodes binary or protocol-specific content into higher-level message objects. 
	 * 
	 * MINA invokes decode(IoSession, ByteBuffer, ProtocolDecoderOutput)  
	 * method with read data, and then the decoder implementation puts decoded messages 
	 * into ProtocolDecoderOutput.
	 * 
	 */
	public boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		List<ProtocolObject> packets = new LinkedList<ProtocolObject>();
		boolean enough = true;
		int payload = 0;
		// Find out if we have enough data for a full packet
		if (in.remaining() >= 4) {
			in.mark();
			payload = in.getInt();
			if ( payload > MServer.maxPacketSize ) {
				in.clear();
				throw new IOException("Packet too large  size=" + payload + " from ip-address:" + session.getRemoteAddress());
			}
			in.reset();
			if (in.remaining() < payload) {
				enough = false;
			}
		} else {
			enough = false;
		}
		
		if (enough) {
			ProtocolObject packet = null;
			// verify packet size?
			if ( MServer.verifyPacketSize ) {
				// make a copy of the buffer so we don't consume left overs
				byte[] newBuffer = new byte[payload];
				in.buf().get(newBuffer);
				packet = decrypt(session, styx.unpack(ByteBuffer.wrap(newBuffer).buf()));
			} else {
				packet = decrypt(session, styx.unpack(in.buf()));
			}
			if (packet != null) {
				packets.add(packet);
	        }
		}
		
		out.write(packets);
		
		// If we return true this method will be called again
		// If we have more data remaining and last data was a packet then rerun
		boolean cont = in.remaining() > 0 && enough;
		return cont;
	}
	
	/**
	 * Decrypts the given packet, if it is encrypted and returns a unencrypted packet
	 * 
	 * Note that null may be returned in case the packet should be dropped.
	 * 
	 * @param session
	 * @param packet
	 * @return
	 * @throws Exception 
	 */
	private ProtocolObject decrypt(IoSession session, ProtocolObject packet) throws Exception {
		if (packet instanceof EncryptedTransportPacket) {			
			if(isEncryptionEnabled(session)) {
				EncryptedTransportPacket encrypted = (EncryptedTransportPacket) packet;
				switch (encrypted.func) {
					case CryptoConstants.SESSION_KEY_REQUEST :
						EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
						encryptedTransportPacket.func = CryptoConstants.SESSION_KEY_RESPONSE;
						// hand over to crypto handler
						encryptedTransportPacket.payload = handleKeyRequest(encrypted.payload, session);
						// wrap response and send it 
						ByteBuffer response = ByteBuffer.wrap(styx.pack(encryptedTransportPacket));
						session.write(response);
						// set encryption active attribute
						session.setAttribute("encryptionActive");
						packet = null;
						break;
					case CryptoConstants.ENCRYPTED_DATA :
						// decrypt data and pass it on through the filter chain
						byte[] decrypted = handleEncryptedData(encrypted.payload, session);
						packet = styx.unpack(java.nio.ByteBuffer.wrap(decrypted));
						break;
				}
				return packet;
			} else {
				EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
				encryptedTransportPacket.func = CryptoConstants.ILLEGAL_PACKET;
				encryptedTransportPacket.payload = new byte[0];
				// wrap response and send it 
				ByteBuffer response = ByteBuffer.wrap(styx.pack(encryptedTransportPacket));
				session.write(response);
				if(!session.containsAttribute("cryptoWarn")) {
					session.setAttribute("cryptoWarn");
					log.info("Session for remote address " + session.getRemoteAddress() + " received encryption packet, but encryption is not enabled in the cluster properties.");
				}
				return null;
			}
		} else {
			if(forceEncryption) {
				EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
				encryptedTransportPacket.func = CryptoConstants.ENCRYPTION_MANDATORY;
				encryptedTransportPacket.payload = new byte[0];
				// wrap response and send it 
				ByteBuffer response = ByteBuffer.wrap(styx.pack(encryptedTransportPacket));
				session.write(response);
				if(!session.containsAttribute("cryptoMandWarn")) {
					session.setAttribute("cryptoMandWarn");
					log.info("Session for remote address " + session.getRemoteAddress() + " received un-encrypted packet (" + packet.getClass().getName() + "), but encryption is mandatory in the cluster properties.");
				}
				return null;
			} else {
				return packet;
			}
		}
	}
	
	private boolean isEncryptionEnabled(IoSession session) {
		return session.getAttribute("crypto") != null;
	}

	/**
	 * Decrypt incoming data
	 * 
	 * @param data - incoming data
	 * @param session - Apache MINA session
	 * 
	 * @return clear text data
	 */
	public byte[] handleEncryptedData(byte[] data, IoSession session) throws Exception {
		log.trace("Received ENCRYPTED_DATA");
		session.setAttribute("usecrypto");
		AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
		return cryptoProvider.decrypt(data);
	}
	
	/**
	 * Handle key request
	 * 
	 * @param data - public key for encryption
	 * @param session - Apache MINA session
	 * 
	 * @return encrypted session key 
	 */
	public byte[] handleKeyRequest(byte[] data, IoSession session) throws Exception {
		log.trace("Received SESSION_KEY_REQUEST");
		session.setAttribute("usecrypto");
		AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
		String modulus = new String(data);
		RSAKeyExchange rsaKeyExchange = new RSAKeyExchange();
								
		AESSessionKey aesSessionKey = (AESSessionKey) cryptoProvider.getSessionKey();
	    return rsaKeyExchange.getEncryptedSessionKey(aesSessionKey, modulus, "10001");
	}	

	/**
	 * Release all resources related with this decoder.
	 */
	public void dispose(IoSession session) throws Exception {}
	
	/**
	 * Invoked when the specified session is closed. This method is useful 
	 * when you deal with the protocol which doesn't specify the length of a 
	 * message such as HTTP response without content-length header. 
	 * 
	 * Implement this method to process the remaining data that 
	 * decode(IoSession, ByteBuffer, ProtocolDecoderOutput)  
	 * method didn't process completely.
	 * 
	 */
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {}

}
