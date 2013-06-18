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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RSAKeyExchange implements KeyExchange {

	private static final String ALGORITHM="RSA";
	/**
	 * return an encrypted session key as a byte array
	 * @param sessionKey
	 * @param publicKey
	 * @return encrypted key
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public byte[] getEncryptedSessionKey(SessionKey sessionKey, PublicKey publicKey) throws Exception {
		
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				
		return cipher.doFinal(sessionKey.getSessionKey().getEncoded());
		
	}

	/**
	 * return an encrypted session key as a byte array
	 * @param sessionKey
	 * @param modulus - public key modulus in in hex formatted string
	 * @param exponent - public key exponent in in hex formatted string 
	 * @return encrypted key
	 * @throws Exception 
	 */
	public byte[] getEncryptedSessionKey(SessionKey sessionKey, String modulus, String exponent) throws Exception {
		
		BigInteger keyModulus = new BigInteger(modulus, 16);
		BigInteger keyExponent = new BigInteger(exponent, 16);
	
		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(keyModulus, keyExponent);
		
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		RSAPublicKey rsapublicKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);

		return getEncryptedSessionKey(sessionKey, rsapublicKey);
	}

}
