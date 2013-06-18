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
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.texasholdem.TexasHoldemFutureActionsCalculator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BettingRoundTimeoutTest {

    @Mock
    private PokerContext context;
    @Mock
    private PlayerToActCalculator playerToActCalculator;
    @Mock
    private PokerPlayer player;
    @Mock
    private ActionRequest actionRequest;
    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    @Mock
    private ServerAdapter serverAdapter;

    private BettingRound round;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        NoLimitBetStrategy betStrategy = new NoLimitBetStrategy(new BigDecimal(10));
        ActionRequestFactory actionRequestFactory = new ActionRequestFactory(betStrategy);
        TexasHoldemFutureActionsCalculator futureActionsCalculator = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);
        round = new BettingRound(context, serverAdapterHolder, playerToActCalculator, actionRequestFactory, futureActionsCalculator, betStrategy);
    }

    @Test
    public void testMakeDefaultActionAndThenSitOutOnTimeout() {
        int playerId = 1334;
        round.playerToAct = playerId;
        when(context.getPlayerInCurrentHand(playerId)).thenReturn(player);
        when(player.getId()).thenReturn(playerId);
        when(actionRequest.matches(Mockito.any(PokerAction.class))).thenReturn(true);
        when(player.getActionRequest()).thenReturn(actionRequest);

        round.timeout();

        verify(serverAdapter).notifyActionPerformed(Mockito.any(PokerAction.class), Mockito.eq(player));
        verify(serverAdapter).notifyPlayerBalance(player);
        verify(player).setSittingOutNextHand(true);
        verify(player).setHasFolded(true);

    }



}
