/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.rounds.discard;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundCreator;

import java.io.Serializable;

public class DiscardRoundCreator implements RoundCreator, Serializable {

    private final int cardsToDiscard;

    public DiscardRoundCreator(int cardsToDiscard) {
        this.cardsToDiscard = cardsToDiscard;
    }

    @Override
    public Round create(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        int dealerSeatId = context.getBlindsInfo().getDealerButtonSeatId();
        return new DiscardRound(context, serverAdapterHolder, new DiscardOrderCalculator(dealerSeatId), cardsToDiscard, true);
    }
}
