package com.cubeia.firebase.crypto
{
	import com.cubeia.firebase.crypto.CryptoProvider;
	import com.hurlant.crypto.Crypto;
	import com.hurlant.crypto.symmetric.ICipher;
	import com.hurlant.crypto.symmetric.PKCS5;
	
	import flash.utils.ByteArray;

	public class AESProvider implements CryptoProvider
	{
		private var pad2:PKCS5 = new PKCS5(16);
		private var cipher:ICipher;
	
		public function decrypt(buffer:ByteArray):ByteArray
		{
			cipher.decrypt(buffer);
			buffer.position = 0;
			return buffer;
		}
		
		public function encrypt(buffer:ByteArray):ByteArray
		{
			cipher.encrypt(buffer);
			buffer.position = 0;
			return buffer;
		}
		
		public function setSessionKey(buffer:ByteArray):void
		{
			cipher = Crypto.getCipher("simple-aes128-cbc", buffer, pad2);
		}
		
	}
}