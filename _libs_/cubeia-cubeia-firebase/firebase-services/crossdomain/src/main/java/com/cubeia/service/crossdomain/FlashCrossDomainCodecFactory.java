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
package com.cubeia.service.crossdomain;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Codec factory for Flash xml socket protocol 
 * @author peter
 *
 */
public class FlashCrossDomainCodecFactory implements ProtocolCodecFactory {

	public ProtocolEncoder getEncoder() throws Exception {
		return new ProtocolEncoderAdapter() {
			/**
			 * encode to flash xml socket format
			 * i.e. convert to string and send a terminsating null byte
			 */
			public void encode( IoSession session, Object message, ProtocolEncoderOutput out ) throws Exception {
				
				byte nullByte = 0;
				// convert to  string
				CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
				String outputString = message.toString();

				// copy string + null byte to buffer
				ByteBuffer buf = ByteBuffer.allocate(outputString.length()+1);
				buf.putString(outputString, encoder);
				buf.put(nullByte);
				buf.flip();
				// write buffer
				out.write(buf);
			}
		};
	}

	public ProtocolDecoder getDecoder() throws Exception {
		return new CumulativeProtocolDecoder() {
			/**
			 * decode input sent from flash player xml socket
			 */
			protected boolean doDecode( IoSession session, ByteBuffer in, ProtocolDecoderOutput out ) throws Exception {
				int byteCount = 0;
				
				while ( in.hasRemaining() ) {
					byte inByte = in.get();
					// check for terminating null byte
					if ( inByte == 0 ) {
						// create string from byte array 
						String xdomainRequest = new String(in.array(), 0, byteCount);
						// write string 
						out.write(xdomainRequest);
						// flush in buffer
						while ( in.hasRemaining() ) {
							inByte = in.get();
						}
						return true;
					}
					byteCount ++;
				}
				
				return false;
			}
		};
	}
}
