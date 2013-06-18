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
package com.cubeia.firebase.clients.java.connector;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * Encrypt and decrypt data using the AES algorithm
 * @author peter
 *
 */
public class AESCryptoProvider implements CryptoProvider {

	private SessionKey sessionKey;
	
	public AESCryptoProvider() {
	}
	
	public static final int NUMBEROFBITS = 128;
	
	
	/**
	 * decrypt a byte buffer using symmetric session key
	 * 
	 * @return decrypted data
	 */
	public byte[] decrypt(byte[] encryptedData) throws GeneralSecurityException {
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		final int BLOCK_SIZE = cipher.getBlockSize();

		byte[] iv = new byte[16];
		System.arraycopy(encryptedData, 0, iv, 0, BLOCK_SIZE);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		
		if (sessionKey == null) {
			throw new RuntimeException("SESSION KEY IS NULL, CANNOT DECRYPT PACKET.");
		}
		
	    cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);
	    
	    return cipher.doFinal(encryptedData, BLOCK_SIZE, encryptedData.length - BLOCK_SIZE);
	}

	/**
	 * encrypt a byte area
	 * 
	 * @return encrypted data
	 */
	public byte[] encrypt(byte[] clearTextData) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		final int BLOCK_SIZE = cipher.getBlockSize();

		byte[] iv = new byte[16];
		
		System.arraycopy(clearTextData, 0, iv, 0, clearTextData.length < BLOCK_SIZE ? clearTextData.length : BLOCK_SIZE );
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		
	    cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
	    
	    int bytesRequired = cipher.getOutputSize(clearTextData.length) + BLOCK_SIZE;
	    byte[] output = new byte[bytesRequired];
	    System.arraycopy(iv, 0, output, 0, BLOCK_SIZE);
	    cipher.doFinal(clearTextData, 0, clearTextData.length, output, BLOCK_SIZE);//, BLOCK_SIZE, clearTextData.length - BLOCK_SIZE);
	     
	    return output;
	}

	
	/**
	 * Set session key
	 */
	public void setSessionKey(SessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}

	public SessionKey getSessionKey() {
		return sessionKey;
	}

	public void createSessionKey() throws GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

}
