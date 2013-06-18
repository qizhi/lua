package com.cubeia.firebase.events
{
	import com.cubeia.firebase.io.ProtocolObject;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.TimerEvent;
	import flash.utils.Timer;

	public class SendLaterEvent extends  Event
	{
		public static const SEND_LATER:String 	= "_fb_send_later";
		
		/**
		 * constructor
		 */
		public function SendLaterEvent(receivedPacket:ProtocolObject = null)
		{
			super(SEND_LATER);
			if ( receivedPacket != null ) {
				_object = receivedPacket;
			}
			
		}
		
	
		/**
		 * Set object
		 * 
		 * @param ProtocolObject
		 */
		public function setObject(object:ProtocolObject):void
		{
			_object = object;
		}
		
		/**
		 * Retrieve object
		 * 
		 * @return ProtocolObject
		 */
		public function getObject():ProtocolObject
		{
			return _object;
		}
		
		
		private var _object:ProtocolObject;
	}
}