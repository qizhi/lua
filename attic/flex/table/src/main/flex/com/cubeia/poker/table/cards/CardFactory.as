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
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.Loader;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import flash.net.URLRequest;
	
	import mx.controls.Alert;
	import mx.events.CalendarLayoutChangeEvent;


	
	public class CardFactory
	{

		[Bindable]
		[Embed(source="/assets/pocket_card.png")]
		public var HiddenCard:Class;

		[Bindable]
		[Embed(source="/assets/cards_private.png")]
		public var PrivateCards:Class;

		
		public static const instance:CardFactory = new CardFactory();;
		
		public static var cardBitmap:Bitmap;
		
		
		public function getCardImage(wrappedGameCard:WrappedGameCard):Bitmap
		{
			if ( wrappedGameCard.hidden == true ) {
				return getHiddenCard();
			} else {
				return getPrivateCard(wrappedGameCard);
			}
		}
		
		public function CardFactory() {
			cardBitmap = new PrivateCards();
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


		
		public function getHiddenCard():Bitmap {
			return new HiddenCard();
		}
			

		public function getPrivateCard(card:GameCard):Bitmap
		{
			var cardYPos:int = 43 * card.suit;
			var cardXPos:int;
			if ( card.rank == RankEnum.ACE )
			{
				cardXPos = 0;
			}
			 else 
			{
			 	cardXPos = 29 * (12 - card.rank);
			}
			var rect:Rectangle = new Rectangle(cardXPos,cardYPos, 29, 43);
			var bits:BitmapData = new BitmapData(29,43);
			bits.copyPixels(cardBitmap.bitmapData, rect, new Point(0,0));
			var bm:Bitmap = new Bitmap(bits);
			
			return bm;
		}


	}
}