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
package com.cubeia.firebase.server.gateway.comm.crypto;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;


import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.server.gateway.comm.crypto.AESCryptoProvider;
import com.cubeia.firebase.server.gateway.comm.crypto.AESSessionKey;
import com.cubeia.firebase.server.gateway.comm.crypto.RSAKeyExchange;

/**
 * AES encryption filter
 * 
 * @author peter
 *
 */
public class AESCryptoFilter extends CryptoFilter {

	
	private static transient Logger log = Logger.getLogger(AESCryptoFilter.class);
	
	/**
	 * Handle key request
	 * 
	 * @param data - public key for encryption
	 * @param session - Apache MINA session
	 * 
	 * @return encrypted session key 
	 */
	public byte[] handleKeyRequest(byte[] data, IoSession session) throws Exception {

		log.info("Received SESSION_KEY_REQUEST");
		
		AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
		String modulus = new String(data);
		RSAKeyExchange rsaKeyExchange = new RSAKeyExchange();
								
		AESSessionKey aesSessionKey = (AESSessionKey) cryptoProvider.getSessionKey();
	    return rsaKeyExchange.getEncryptedSessionKey(aesSessionKey, modulus, "10001");
		

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
		log.info("Received ENCRYPTED_DATA");

		session.setAttribute("usecrypto");

		AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
		return cryptoProvider.decrypt(data);
	}
	
	/**
	 * Called by the mina framework when data is about to be written
	 * 
	 * @param nextFilter - next filter in chain
	 * @param session - Apache MINA session
	 * @param writeRequest - data to be written
	 */
	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

		// is encryption active?
		if ( session.containsAttribute("usecrypto") ) {
			// get crypto provider
			AESCryptoProvider cryptoProvider = (AESCryptoProvider) session.getAttribute("crypto");
			// get data to be encrypted
			ByteBuffer buffer = (ByteBuffer) writeRequest.getMessage();
			
			// create EncryptedTransportPacket
			EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
			encryptedTransportPacket.func = ENCRYPTED_DATA;
			// encrypt data
			encryptedTransportPacket.payload = cryptoProvider.encrypt(buffer.array());

			// Serialize, wrap in new WriteRequest and send to the next filter in chain
			StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());
			ByteBuffer response = ByteBuffer.wrap(styx.pack(encryptedTransportPacket));
			nextFilter.filterWrite(session, new WriteRequest(response));
		} else {
			// encryption not active, just pass the request on
			nextFilter.filterWrite(session, writeRequest);
		}
	}
}


