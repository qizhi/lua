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

package com.cubeia.games.poker.adapter;

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.hand.Suit;
import com.cubeia.poker.handhistory.api.GameCard;
import com.cubeia.poker.handhistory.api.PlayerAction;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class HandHistoryTranslatorTest {

    @Test
    public void checkRankMatch() {
        for (Rank r : Rank.values()) {
            GameCard.Rank gcr = GameCard.Rank.values()[r.ordinal()];
            assertEquals(r.name(), gcr.name());
        }
    }

    @Test
    public void checkSuitMatch() {
        for (Suit s : Suit.values()) {
            GameCard.Suit gcs = GameCard.Suit.values()[s.ordinal()];
            assertEquals(s.name(), gcs.name());
        }
    }

    @Test
    public void checkActionTypeMatch() {
        for (PokerActionType t : PokerActionType.values()) {
            PlayerAction.Type pat = PlayerAction.Type.values()[t.ordinal()];
            assertEquals("PokerActionType " + t.name() + " does not have same name as protocol enum type " + pat.name(), t.name(), pat.name());
        }
    }
}
