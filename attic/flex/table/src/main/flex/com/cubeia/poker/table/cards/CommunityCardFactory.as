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

package com.cubeia.poker.table.cards
{
	import com.cubeia.games.poker.io.protocol.GameCard;
	import com.cubeia.games.poker.io.protocol.RankEnum;
	import com.cubeia.games.poker.io.protocol.SuitEnum;
	
	import mx.controls.Image;
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import flash.events.IOErrorEvent;
	
	import flash.display.Loader;
	import flash.events.Event;
	import flash.net.URLRequest;


	public class CommunityCardFactory
	{
		
		public static var cardBitmap:Bitmap = null;
		public static var loadDone:Boolean = false;
		public static var instance:CommunityCardFactory = new CommunityCardFactory();
		
		private static var publicCardImageFile:String = "assets/cards_public.png";
		
		[Bindable]
		[Embed(source="/assets/cards_public.png")]
		public var PublicCard:Class;

		
		public function CommunityCardFactory()
		{
			cardBitmap = new PublicCard();
			
		}
		

		public static function getStringRepesentation(card:GameCard):String
		{
			var suit:String;
			var rank:String;
			
			switch ( card.suit )
			{
				case SuitEnum.CLUBS :
					suit = "c";
					break;
				case SuitEnum.SPADES :
					suit = "s";
					break;
				case SuitEnum.HEARTS:
					suit = "h";
					break;
				case SuitEnum.DIAMONDS :
					suit = "d";
					break;
			}
			
			switch ( card.rank )
			{
				case RankEnum.ACE:
					rank = "A";
					break;
				case RankEnum.KING:
					rank = "K";
					break;
				case RankEnum.QUEEN:
					rank = "Q";
					break;
				case RankEnum.TEN:
					rank = "T";
					break;
				default:
					rank = (card.rank + 2).toString();
			}
			return rank+suit;
			
		}

		public function getBackCard():Bitmap
		{
			
			var cardYPos:int = 140;
			var cardXPos:int = 50 * 13;

			var rect:Rectangle = new Rectangle(cardXPos,cardYPos, 50, 70);
			var bits:BitmapData = new BitmapData(50,70);
			bits.copyPixels(cardBitmap.bitmapData, rect, new Point(0,0));
			var bm:Bitmap = new Bitmap(bits);
			
			return bm;
		}


		public function getCard(card:GameCard):Bitmap
		{
			
			var cardYPos:int = 70 * (3 - card.suit);
			var cardXPos:int;
			if ( card.rank == RankEnum.ACE )
			{
				cardXPos = 0;
			}
			 else 
			{
			 	cardXPos = 50 * (12 - card.rank);
			}
			var rect:Rectangle = new Rectangle(cardXPos,cardYPos, 50, 70);
			var bits:BitmapData = new BitmapData(50,70);
			bits.copyPixels(cardBitmap.bitmapData, rect, new Point(0,0));
			var bm:Bitmap = new Bitmap(bits);
			
			return bm;
		}


	}
}