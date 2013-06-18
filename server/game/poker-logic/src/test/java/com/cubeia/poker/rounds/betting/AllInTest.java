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

import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.pot.RakeCalculator;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.*;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.texasholdem.TexasHoldemFutureActionsCalculator;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AllInTest extends TestCase  {

    private BettingRound round;

    private PokerContext context;

    @Mock
    private PokerSettings settings;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private RakeCalculator rakeCalculator;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initMocks(this);

        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(settings.getTiming()).thenReturn(new DefaultTimingProfile());
        context = new PokerContext(settings);
        context.setPotHolder(new PotHolder(rakeCalculator));
    }

    public void testAllInBet() {
        // NOTE: This implies no limit betting.
        MockPlayer[] p = TestUtils.createMockPlayers(2, 500);
        addPlayers(p);

        DefaultPlayerToActCalculator playerToActCalculator = new DefaultPlayerToActCalculator(0);
        NoLimitBetStrategy betStrategy = new NoLimitBetStrategy(BigDecimal.ZERO);
        ActionRequestFactory actionRequestFactory = new ActionRequestFactory(betStrategy);
        TexasHoldemFutureActionsCalculator futureActionsCalculator = new TexasHoldemFutureActionsCalculator(betStrategy.getType());
        round = new BettingRound(context, serverAdapterHolder, playerToActCalculator, actionRequestFactory, futureActionsCalculator, betStrategy);

        ArgumentCaptor<ActionRequest> captor = ArgumentCaptor.forClass(ActionRequest.class);
        verify(serverAdapter).requestAction(captor.capture());
        PossibleAction bet = captor.getValue().getOption(PokerActionType.BET);
        assertEquals(bd(500), bet.getMaxAmount());
        assertEquals(bd(500), p[1].getBalance());
        act(p[1], PokerActionType.BET, bet.getMaxAmount());
        assertEquals(bd(0), p[1].getBalance());
        assertTrue(p[1].isAllIn());
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    private void addPlayers(MockPlayer[] players) {
        for (PokerPlayer player : players) {
            context.addPlayer(player);
            context.getCurrentHandSeatingMap().put(player.getSeatId(), player);
            context.getCurrentHandPlayerMap().put(player.getId(), player);
        }
    }

    // HELPERS
    private void act(MockPlayer player, PokerActionType action, BigDecimal amount) {
        PokerAction a = new PokerAction(player.getId(), action);
        a.setBetAmount(amount);
        round.act(a);
    }

}
