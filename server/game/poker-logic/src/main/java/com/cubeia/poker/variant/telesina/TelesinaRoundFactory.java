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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.result.RevealOrderCalculator;
import com.cubeia.poker.rounds.ante.AnteRound;
import com.cubeia.poker.rounds.ante.AnteRoundHelper;
import com.cubeia.poker.rounds.betting.ActionRequestFactory;
import com.cubeia.poker.rounds.betting.BetStrategy;
import com.cubeia.poker.rounds.betting.BetStrategyFactory;
import com.cubeia.poker.rounds.betting.BettingRound;
import com.cubeia.poker.rounds.dealing.DealCommunityCardsRound;
import com.cubeia.poker.rounds.dealing.DealExposedPocketCardsRound;
import com.cubeia.poker.rounds.dealing.DealPocketCardsRound;
import com.cubeia.poker.rounds.dealing.ExposePrivateCardsRound;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.variant.telesina.hand.TelesinaPlayerToActCalculator;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Factory of Telesina game rounds.
 * The main purpose of this class is to separate round creation from the game type logic
 * to enable unit testing.
 *
 * @author w
 */
public class TelesinaRoundFactory implements Serializable {

    AnteRound createAnteRound(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        return new AnteRound(context, serverAdapterHolder, new AnteRoundHelper(context, serverAdapterHolder));
    }

    BettingRound createBettingRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, Rank lowestRank) {
        PokerSettings settings = context.getSettings();
        BigDecimal betLevel = new BigDecimal("2").multiply(context.getSettings().getAnteAmount());
        BetStrategy betStrategy = BetStrategyFactory.createBetStrategy(settings.getBetStrategyType(), betLevel);
        ActionRequestFactory actionRequestFactory = new ActionRequestFactory(betStrategy);
        TelesinaPlayerToActCalculator playerToActCalculator = new TelesinaPlayerToActCalculator(lowestRank);
        TelesinaFutureActionsCalculator futureActionsCalculator = new TelesinaFutureActionsCalculator();
//        int buttonSeatId = context.getBlindsInfo().getDealerButtonSeatId();
        return new BettingRound(context, serverAdapterHolder, playerToActCalculator, actionRequestFactory, futureActionsCalculator, betStrategy);
    }

    DealExposedPocketCardsRound createDealExposedPocketCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        return new DealExposedPocketCardsRound(context, serverAdapterHolder);
    }

    ExposePrivateCardsRound createExposePrivateCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder, RevealOrderCalculator revealOrderCalculator) {
        return new ExposePrivateCardsRound(context, serverAdapterHolder, revealOrderCalculator);
    }

    DealCommunityCardsRound createDealCommunityCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        return new DealCommunityCardsRound(context, serverAdapterHolder, 1);
    }

    DealPocketCardsRound createDealInitialCardsRound(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        return new DealPocketCardsRound(context, serverAdapterHolder, 1, 1);
    }

}
