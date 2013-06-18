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

package com.cubeia.poker.variant;

import com.cubeia.poker.hand.DeckProvider;
import com.cubeia.poker.hand.HandTypeEvaluator;
import com.cubeia.poker.hand.StandardDeckProvider;
import com.cubeia.poker.rounds.RoundCreator;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class PokerGameBuilder {

    private List<RoundCreator> rounds;

    private DeckProvider deckProvider = new StandardDeckProvider();

    private HandTypeEvaluator handEvaluator = new TexasHoldemHandCalculator();

    public GenericPokerGame build() {
        return new GenericPokerGame(rounds, deckProvider, handEvaluator);
    }

    public PokerGameBuilder withRounds(RoundCreator ... roundCreators) {
        this.rounds = newArrayList(roundCreators);
        return this;
    }

    public PokerGameBuilder withDeckProvider(DeckProvider deckProvider) {
        this.deckProvider = deckProvider;
        return this;
    }

    public PokerGameBuilder withHandEvaluator(HandTypeEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
        return this;
    }
}
