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

import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Map;

public class HandResultBatchFactory {

    @Service
    @VisibleForTesting
    protected PokerConfigurationService configService;

    private final Logger log = Logger.getLogger(getClass()); 

    public BatchHandRequest createAndValidateBatchHandRequest(HandResult handResult, String handId, TableId tableId) {
        BigDecimal totalBet = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalRake = BigDecimal.ZERO;
        Currency currency = handResult.getCurrency();
        BatchHandRequest bhr = new BatchHandRequest(handId, tableId, new Money(handResult.getTotalRake(), currency));
        for (Map.Entry<PokerPlayer, Result> resultEntry : handResult.getResults().entrySet()) {
            PokerPlayerImpl player = (PokerPlayerImpl) resultEntry.getKey();
            Result result = resultEntry.getValue();
            Money bets = new Money(result.getWinningsIncludingOwnBets().subtract(result.getNetResult()),currency);
            Money wins = new Money(result.getWinningsIncludingOwnBets(),currency);
            Money rake = new Money(handResult.getRakeContributionByPlayer(player),currency);
            Money net = new Money(result.getNetResult(),currency);
            Money startingBalanceMoney = new Money(player.getStartingBalance(),currency);
            log.debug("Result for player " + player.getId() + " -> Bets: " + bets + "; Wins: " + wins + "; Rake: " + rake + "; Net: " + net);
            com.cubeia.backend.cashgame.dto.HandResult hr = new com.cubeia.backend.cashgame.dto.HandResult(
                    player.getPlayerSessionId(), bets, wins, rake, player.getSeatId(), player.getOperatorId(), startingBalanceMoney); // TODO Add initial balance? // FIXME: Operator ID is hard coded to 0 in PokerPlayer!
            bhr.addHandResult(hr);
            totalBet = totalBet.add(bets.getAmount());
            totalNet = totalNet.add(net.getAmount());
            totalRake = totalRake.add(rake.getAmount());
        }
        if ((totalNet.add(totalRake)).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Unbalanced hand result ((" + totalNet + " + " + totalRake + ") != 0); Total bet: " + totalBet + "; " + handResult);
        }
        return bhr;
    }

}
