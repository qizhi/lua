/**
 * Copyright (C) 2009 Cubeia Ltd <info@cubeia.com>
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

 package com.cubeia.firebase.comm
{
	import com.cubeia.firebase.events.SendLaterEvent;
	import com.cubeia.firebase.io.ProtocolObject;
	
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	public class SendLater extends EventDispatcher
	{
			
		private var packet:ProtocolObject;
		
		public function SendLater(_packet:ProtocolObject = null)
		{
			super(null);
			if ( _packet != null ) {
				packet = _packet;
			}
		}
		
		private function onTimer(event:TimerEvent):void
		{
			dispatchEvent(new SendLaterEvent(packet));
		}
		
		public function schedule(_delay:int = 500):void
		{
			var timer:Timer = new Timer(500,1);
			timer.delay = _delay;
			timer.addEventListener(TimerEvent.TIMER, onTimer);
			timer.start();
		}
	}
}



