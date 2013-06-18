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
	
	public class WrappedGameCard extends GameCard
	{
		public var hidden:Boolean = false;
		
		public function WrappedGameCard(card:GameCard)
		{
			if ( card != null ) {
				rank = card.rank;
				suit = card.suit;
				cardId = card.cardId;
			}
			if (rank == RankEnum.HIDDEN && suit == SuitEnum.HIDDEN) {
				hidden = true;
			}
			super();
		}
	}
}