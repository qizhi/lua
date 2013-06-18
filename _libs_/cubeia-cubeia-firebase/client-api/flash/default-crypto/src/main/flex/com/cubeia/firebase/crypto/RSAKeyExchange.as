package com.cubeia.firebase.crypto
{
	import com.cubeia.firebase.crypto.KeyExchange;
	import com.hurlant.crypto.rsa.RSAKey;
	
	import flash.utils.ByteArray;

	public class RSAKeyExchange implements KeyExchange
	{
		private var rsa:RSAKey;
		
		public function RSAKeyExchange(keyLength:int)
		{
			rsa = RSAKey.generate(512, "10001");
		}
		
		public function decryptSessionKey(buffer:ByteArray):ByteArray
		{
			var target:ByteArray = new ByteArray();
			rsa.decrypt(buffer, target, buffer.length);
			target.position = 0;
			return target;
		}
		
		public function getPublicKey():String
		{
			return rsa.n.toString(16);
		}
		
	}
}