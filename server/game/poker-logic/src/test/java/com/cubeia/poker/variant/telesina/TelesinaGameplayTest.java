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

package com.cubeia.poker.variant.telesina;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.GameTypes;
import com.cubeia.poker.variant.HandFinishedListener;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Random;

import static com.cubeia.poker.action.PokerActionType.ANTE;
import static com.cubeia.poker.action.PokerActionType.BET;
import static com.cubeia.poker.action.PokerActionType.CHECK;
import static com.cubeia.poker.action.PokerActionType.DECLINE_ENTRY_BET;
import static com.cubeia.poker.action.PokerActionType.FOLD;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TelesinaGameplayTest {

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private HandFinishedListener handFinishedListener;

    private GameType telesina;

    private RakeSettings rakeSettings;

    private MockPlayer[] p;

    @Mock
    private ServerAdapter serverAdapter;

    private Random randomizer = new Random();

    @Before
    public void setup() {
        initMocks(this);
        rakeSettings = RakeSettings.createDefaultRakeSettings(BigDecimal.valueOf(0));
//        telesina = new Telesina(new TelesinaDeckFactory(), new TelesinaRoundFactory(), new TelesinaDealerButtonCalculator());
        telesina = GameTypes.createTelesina();
        telesina.addHandFinishedListener(handFinishedListener);

        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(serverAdapter.getSystemRNG()).thenReturn(randomizer);
    }

    @Test
    public void testBasicHand() {
        startHand(prepareContext(3));

        act(p[0], ANTE);
        act(p[1], ANTE);
        act(p[2], ANTE);
        telesina.timeout();

        // Initial two pocket cards dealt.
        act(CHECK);
        act(CHECK);
        act(CHECK);

        // Third card dealt
        act(CHECK);
        act(CHECK);
        act(CHECK);

        // Forth card dealt
        act(CHECK);
        act(CHECK);
        act(CHECK);

        // Fifth card dealt
        act(CHECK);
        act(CHECK);
        act(CHECK);

        // Vela card dealt
        act(BET, 100);
        act(FOLD);
        act(FOLD);

        verify(handFinishedListener).handFinished(Mockito.<HandResult>any(), eq(HandEndStatus.NORMAL));
    }

    @Test
    public void testHandCanceledIfNotEnoughPlayers() {
        startHand(prepareContext(3));

        act(p[0], ANTE);
        act(p[1], DECLINE_ENTRY_BET);
        act(p[2], DECLINE_ENTRY_BET);

        verify(handFinishedListener).handFinished(Mockito.<HandResult>any(), eq(HandEndStatus.CANCELED_TOO_FEW_PLAYERS));
    }

    private void act(MockPlayer player, PokerActionType actionType) {
        telesina.act(new PokerAction(player.getId(), actionType));
    }

    private void act(PokerActionType actionType) {
        act(actionType, 0);
    }

    private void act(PokerActionType actionType, int value) {
        PokerPlayer playerToAct = getPlayerToAct();
        telesina.act(new PokerAction(playerToAct.getId(), actionType, new BigDecimal(value)));
    }

    private PokerPlayer getPlayerToAct() {
        ArgumentCaptor<ActionRequest> captor = ArgumentCaptor.forClass(ActionRequest.class);
        verify(serverAdapter, atLeastOnce()).requestAction(captor.capture());
        int playerId = captor.getValue().getPlayerId();
        for (MockPlayer mockPlayer : p) {
            if (mockPlayer.getId() == playerId) {
                return mockPlayer;
            }
        }
        return null;
    }

    private void startHand(PokerContext context) {
        Predicate<PokerPlayer> allGood = Predicates.alwaysTrue();
        context.prepareHand(allGood);
        telesina.startHand();
    }

    private PokerContext prepareContext(int numberOfPlayers) {
        BlindsLevel level = new BlindsLevel(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(50));
        BetStrategyType betStrategy = BetStrategyType.NO_LIMIT;
        PokerSettings settings = new PokerSettings(level, betStrategy, new BigDecimal(100),new BigDecimal( 5000), new DefaultTimingProfile(), 6, rakeSettings, new Currency("EUR",2), null);
        PokerContext context = new PokerContext(settings);
        telesina.setPokerContextAndServerAdapter(context, serverAdapterHolder);
        p = TestUtils.createMockPlayers(numberOfPlayers);
        for (PokerPlayer player : p) {
            player.setHasPostedEntryBet(true);
            context.addPlayer(player);
        }
        return context;
    }


}
