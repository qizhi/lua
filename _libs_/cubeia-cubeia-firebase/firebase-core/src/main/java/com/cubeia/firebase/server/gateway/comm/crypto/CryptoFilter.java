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
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import com.cubeia.firebase.io.BinaryData;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

public class CryptoFilter extends IoFilterAdapter implements CryptoHandler {

	private static transient Logger log = Logger.getLogger(CryptoFilter.class);
	
	private String cryptoProviderClassName = "com.cubeia.firebase.server.gateway.comm.crypto.AESCryptoProvider";
	
	public static final int ENCRYPTED_DATA = 0;
	public static final int SESSION_KEY_REQUEST = 1;
	public static final int SESSION_KEY_RESPONSE = 2;

	
	public CryptoFilter() {
		super();
		log.info("CryptoFilter initialized: " + this.getClass().getName());
	}
	
	/**
	 *  Called when mina has received something on the socket
	 *  
	 *  @param nextFilter - reference to the next filter/codec in the chain
	 *  @param session - Session object
	 *  @param message - buffer from MINA, it should be a ByteBuffer otherwise
	 *                   there is problem with the filter chain.
	 */
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) {
		try {
			// message should be a ByteBuffer or there is a filter chain problem
			if (message instanceof ByteBuffer) {
				ByteBuffer in = (ByteBuffer) message;
				if ( in.remaining() < 4 ) {
					nextFilter.messageReceived(session, message);
					return;
				}
				
				// read past length
				@SuppressWarnings("unused")
				int size = in.getInt();
				
				// get classId
				int classId = BinaryData.asUnsigned(in.get());
				
				// encrypted packet?
				if ( classId == 105 ) {
					// retrieve data and crypto function
					int func = BinaryData.asUnsigned(in.get());
					int payloadLength = in.getInt();
					byte[] data = new byte[payloadLength];
					in.get(data);
					
					switch ( func ) {
						case SESSION_KEY_REQUEST :
							// key request
							EncryptedTransportPacket encryptedTransportPacket = new EncryptedTransportPacket();
							encryptedTransportPacket.func = SESSION_KEY_RESPONSE;
							// hand over to crypto handler
							encryptedTransportPacket.payload = handleKeyRequest(data, session);
							// wrap response and send it 
							StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());
							ByteBuffer response = ByteBuffer.wrap(styx.pack(encryptedTransportPacket));
							session.write(response);
							// set encryption active attribute
							session.setAttribute("encryptionActive");
							
							break;
						case ENCRYPTED_DATA :
							// decrypt data and pass it on through the filter chain 
							ByteBuffer buffer = ByteBuffer.wrap(handleEncryptedData(data, session));
							nextFilter.messageReceived(session, buffer);
							break;
					}
				} else {
					// encryption not used, pass the message on to next in chain
					in.position(0);
					nextFilter.messageReceived(session, in);
					return;
				}
			} else {
				log.warn("Dont know how to decode:" + message.getClass());
			}

		} catch (Exception e) {
			log.error("Exception in CryptoFilter");
			e.printStackTrace();
		}
	
	}

	
	
	public byte[] handleEncryptedData(byte[] data, IoSession session) throws Exception {
		return null;
	}

	public byte[] handleKeyRequest(byte[] data, IoSession session) throws Exception {
		return null;
	}

	public String getCryptoProviderClassName() {
		return cryptoProviderClassName;
	}

	public void setCryptoProviderClassName(String cryptoProviderClassName) {
		this.cryptoProviderClassName = cryptoProviderClassName;
	}
	

		

}
