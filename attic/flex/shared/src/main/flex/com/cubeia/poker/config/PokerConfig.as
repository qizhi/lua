/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.config
{
	import com.cubeia.poker.event.PokerEventDispatcher;
	import com.cubeia.poker.event.StyleChangedEvent;
	
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.TimerEvent;
	import flash.geom.Point;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	
	/**
	 * Configuration parameters for Cubeia Poker
	 * 
	 * This class holds all parsed config parameters from parameter line and server side
	 * config document. Server side will always override local parameters
	 */
	public class PokerConfig
	{
		private static var _instance:PokerConfig = null;
		
		private var configService:URLLoader;
		
		public var configLoaded:Boolean = false;
		
		// auto connect to host
		public var autoConnect:Boolean = true;
		// poker host address
		public var pokerHost:String = "127.0.0.1";
		// poker host port
		public var pokerPort:String = "4123";
		// port of firebase cross domain policy server
		public var crossDomainPort:String = "4122";
		// default style name
		public var defaultStyleName:String = "sunnight";
		// use handshake
		public var useHandshake:Boolean = false;
		// handshake value
		public var handshake:uint = 0;

		// table configuraitions
		private var _tableLayouts:Array = new Array();
		
		[Bindable]
		public static var allowChangeStyle:Boolean = true;
		// default style names
		[Bindable]
		public static var styles:Array;
		
		private var settings:XML;
		/**
		 * Constructor
		 * 
		 * Since we can't have private constructors, it's hard to implement singletons
		 * we throw an error if there is more than one instance
		 */
		public function PokerConfig()
		{
			if ( _instance != null ) {
				throw new Error("PokerConfig should be used as a singleton");
			}
			
			var request:URLRequest = new URLRequest("settings.xml");
			configService = new URLLoader();
			configService.addEventListener(Event.COMPLETE, onConfigLoaded);
			configService.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			
			try 
			{
				configService.load(request);
			}
			catch (error:ArgumentError)
			{
				trace("An ArgumentError has occurred.");
			}
			catch (error:SecurityError)
			{
				trace("A SecurityError has occurred.");
			}
			
		}
		
		public function getTableConfig(capacity:int):TableConfig {
			for each ( var tableConfig:TableConfig in _tableLayouts ) {
				if ( tableConfig.numberOfSeats == capacity ) {
					return tableConfig;
				}
			}
			return null;
		}
			
		
		private function ioErrorHandler(event:IOErrorEvent):void {
			Alert.show("An IO error has occurred: " + event.text);
		}
		
		public static function getInstance():PokerConfig
		{
			if ( _instance == null ) {
				_instance = new PokerConfig();
			}
			return _instance;
		} 
		
		private function onConfigLoaded(event:Event):void
		{
			var i:int;
			
			settings = XML(event.target.data);
			
			pokerHost = settings.host.@name;
			pokerPort = settings.host.@port;
			
			_instance.defaultStyleName = settings.defaultStyle.@name;
			
			var styleList:XMLList = settings.styles.style;
			
			styles = new Array(styleList.length());
			for ( i = 0; i < styleList.length(); i ++ ) {
				styles[i] = styleList[i].@name;
			}
			
			
			var tableTypes:XMLList = settings.Tables.Table;
			for ( i = 0; i < tableTypes.length(); i ++ )
			{
				var numberOfSeats:int = parseInt(tableTypes[i].@seats);
				var tableConfig:TableConfig = new TableConfig(numberOfSeats);
				var seatList:XMLList = tableTypes[i].Seat;
				for ( var j:int = 0; j < seatList.length(); j ++ )
				{
					var seatConfig:SeatConfig = new SeatConfig();
					seatConfig.index = parseInt(seatList[j].@index);
					seatConfig.position = new Point(parseInt(seatList[j].@x), parseInt(seatList[j].@y));
					seatConfig.buttonPosition = new Point(parseInt(seatList[j].@buttonPosX), parseInt(seatList[j].@buttonPosY));
					seatConfig.chipstackPosition = new Point(parseInt(seatList[j].@csPosX), parseInt(seatList[j].@csPosY));
					tableConfig.addSeatConfig(seatConfig.index, seatConfig);
				}
				_tableLayouts.push(tableConfig);
			}
			configService.removeEventListener(Event.COMPLETE, onConfigLoaded);
			configService.removeEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			
			configLoaded = true;
		}

	}
}