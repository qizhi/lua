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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.player.PokerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import static com.cubeia.poker.action.PokerActionType.*;

public class ActionRequestFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(ActionRequestFactory.class);

    private final BetStrategy betStrategy;

    public ActionRequestFactory(BetStrategy betStrategy) {
        this.betStrategy = betStrategy;
    }

    public ActionRequest createFoldCallRaiseActionRequest(BettingRoundContext context, PokerPlayer p) {
        PossibleAction fold = new PossibleAction(FOLD, BigDecimal.ZERO);
        PossibleAction call = new PossibleAction(CALL, betStrategy.getCallAmount(context, p));
        PossibleAction raise = new PossibleAction(RAISE, betStrategy.getMinRaiseToAmount(context, p), betStrategy.getMaxRaiseToAmount(context, p));

        ActionRequest request = new ActionRequest();
        if (raise.getMinAmount().compareTo(BigDecimal.ZERO) > 0 && p.canRaise()) {
            request.setOptions(Arrays.asList(fold, call, raise));
        } else {
            request.setOptions(Arrays.asList(fold, call));
        }
        request.setPlayerId(p.getId());
        return request;
    }

    public ActionRequest createFoldCheckBetActionRequest(BettingRoundContext bettingRoundContext, PokerPlayer p) {
        PossibleAction fold = new PossibleAction(FOLD, BigDecimal.ZERO);
        PossibleAction check = new PossibleAction(PokerActionType.CHECK, BigDecimal.ZERO);
        PossibleAction bet = new PossibleAction(PokerActionType.BET, betStrategy.getMinBetAmount(bettingRoundContext, p),
                betStrategy.getMaxBetAmount(bettingRoundContext, p));
        ActionRequest request = new ActionRequest();
        request.setOptions(Arrays.asList(fold, check, bet));
        request.setPlayerId(p.getId());
        return request;
    }

}
