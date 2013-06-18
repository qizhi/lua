/**
 * Copyright (C) 2009 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.cubeia.firebase.comm
{
	import flash.errors.EOFError;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.events.StatusEvent;
	import flash.net.Socket;
	import flash.system.Security;
	import flash.utils.ByteArray;
	import flash.utils.Endian;
	
	public class FirebaseSocket
	{
		// data buffer		
		private var socketBuffer:ByteArray = new ByteArray();
		
		// byte counters for read loop
		private var bytesLeft:int = 0;
		private var bytesTotal:int = 0;
		
		// class id of buffered data
		private var classId:int = 0;
		
		// Data handler
		private var dataHandler:SocketDataHandler;

		// Socket
		private var socket:Socket;

		// Port to connect to
		private var port:uint;

		// Hostname or ip-address
		private var host:String;
		
		private var handshakeSignature:uint;
		
		private var useHandshake:Boolean;
		
		/**
		 * Constructor
		 * 
		 * @param dataHandler - SocketDataHandler interface implementation 
		 * @param useHandshake - send a handshake signature in every packet
		 * @param handshakeSignature - signature to use as handshake
		 */
		public function FirebaseSocket(dataHandler:SocketDataHandler, useHandshake:Boolean = false, handshakeSignature:uint = 0):void 
		{
			this.dataHandler = dataHandler;
			this.handshakeSignature = handshakeSignature;
			this.useHandshake = useHandshake;
			createSocket();
		}
		
		/**
		 * Return true if socket is connected
		 * 
		 * @return connected
		 */
		public function get connected():Boolean
		{
			return socket.connected;
		}
		
		/**
		 * Connect to the Firebase server
		 * 
		 * @param host - host name or ip address of the Firebase server
		 * @param port - tcp port to connect to
		 * @param crossDomainPolicyServerPort - port of cross domain xmlsocket request
		 *
		 */ 
		public function connect(host:String, port:uint = 4123, crossDomainPolicyServerPort:uint = 4122):void
		{
			// create a new socket if we don't have one already
			if ( socket == null )
			{
				socket = new Socket();
			}
			
			// make sure we're not already connected
			if ( !socket.connected ) 
			{
				Security.allowDomain("*");
				var crossdomainURL:String = "xmlsocket://" + host + ":" + crossDomainPolicyServerPort;
				Security.loadPolicyFile(crossdomainURL);
				createSocket();
				socket.connect(host, port);
				this.port = port;
				this.host = host;
			} 
		}


		/**
		 * Send an array of bytes to the game server
		 * 
		 * @param buffer - bytes to send
		 */ 
		public function send(buffer:ByteArray):void
		{
			if ( socket.connected ) 
			{
				buffer.position = 0;
				socket.writeBytes(buffer);
				socket.flush();
			} 
		}

		/**
		 * Close connection
		 */
		public function close():void
		{
			deleteSocket();
		}
		
		// create a socket object and setup event listeners	
		private function createSocket():void
		{
			socket = new Socket();
			socket.addEventListener(Event.CONNECT, connectHandler);
			socket.addEventListener(Event.CLOSE, closeHandler);
			socket.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			socket.addEventListener(ProgressEvent.SOCKET_DATA, readHandler);
			socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onSecurityErrorEvent);
		}
		
		// delete socket and disable event listeners
		private function deleteSocket():void
		{
			socket.removeEventListener(Event.CONNECT, connectHandler);
			socket.removeEventListener(Event.CLOSE, closeHandler);
			socket.removeEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			socket.removeEventListener(ProgressEvent.SOCKET_DATA, readHandler);
			socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onSecurityErrorEvent);
			socket.close();
			socket = null;
		}

		
		//
		private function closeHandler(event:Event):void
		{
			deleteSocket();
			dataHandler.handleDisconnect(event);		
		}
		
		// called when data is available for read
		private function readHandler(event:ProgressEvent):void
		{
			while ( true )
			{
				if ( bytesTotal == 0 )
				{
					socketBuffer = new ByteArray();
					socketBuffer.endian = Endian.BIG_ENDIAN;
					
					// we need at least three bytes to read the length indicator + classid
					if ( socket.bytesAvailable >= 5 )
					{
						// read packet length
						bytesTotal = socket.readInt();
						bytesLeft = bytesTotal - 4;
						// put to buffer
						socketBuffer.writeInt(bytesTotal);
						
						// read classid
						classId = socket.readByte();
						bytesLeft --;
						socketBuffer.writeByte(classId);
					}
				}
	
						
				try {
					var available:int = socket.bytesAvailable < bytesLeft ? socket.bytesAvailable : bytesLeft;
					socket.readBytes(socketBuffer, bytesTotal-bytesLeft, available);
					bytesLeft -= available;
					if ( bytesLeft == 0 ) 
					{
						socketBuffer.position = 0;
						bytesTotal = 0;
						dataHandler.handleData(classId, socketBuffer);
					} 
				} catch (e:EOFError) {
						
				}
				// do we have enough data to start reading a new packet?
				if ( socket.bytesAvailable < 5 )
				{
					break;
				}
			}			
		}
		
		private function connectHandler(event:Event):void
		{
			// make sure we have big endian style when reading ints and shorts
			socket.endian = Endian.BIG_ENDIAN;
			
			// Send signature handshake if required
			if ( useHandshake ) 
			{
				socket.writeUnsignedInt(handshakeSignature);
				socket.flush();
			}
			
			dataHandler.handleConnect(event);
		}
		
		private function ioErrorHandler(event:IOErrorEvent):void
		{
			deleteSocket();
			dataHandler.handleIOError(event);		
		}
	
		private function onSecurityErrorEvent(event:SecurityErrorEvent):void 
		{
			dataHandler.handleSecurityEvent(event);
		}

		
		private function onStatusEvent(event:StatusEvent):void 
		{
			dataHandler.handleStatusEvent(event);
		}
	
	}
}