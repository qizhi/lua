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
 * aBigDecimal with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.poker.player;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.hand.Suit;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultPokerPlayerTest {

    @Test
    public void testClearHand() {
        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        player.addPocketCard(new Card(Rank.ACE, Suit.CLUBS), false);
        player.addPocketCard(new Card(Rank.TWO, Suit.CLUBS), true);

        assertThat(player.getPocketCards().getCards().isEmpty(), is(false));
        assertThat(player.getPublicPocketCards().isEmpty(), is(false));
        player.clearHand();
        assertThat(player.getPocketCards().getCards().isEmpty(), is(true));
        assertThat(player.getPublicPocketCards().isEmpty(), is(true));
    }

    @Test
    public void testBalance() {
        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        BigDecimal balance = new BigDecimal(10000);
        player.setBalance(balance);
        assertThat(player.getBalance(), is(balance));

        BigDecimal bet = new BigDecimal(100);
        player.addBet(bet);
        assertThat(player.getBalance(), is(balance.subtract(bet)));
    }

    @Test
    public void testCommitPendingAmount() {
        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        BigDecimal balance = new BigDecimal(10000);
        player.setBalance(balance);

        boolean hadPendingBalance = player.commitBalanceNotInHand(new BigDecimal(1000));
        assertThat(hadPendingBalance, is(false));

        BigDecimal pendingBalance = new BigDecimal(333);
        player.addNotInHandAmount(pendingBalance);

        assertThat(player.getBalanceNotInHand(), is(pendingBalance));
        assertThat(player.getBalance(), is(balance));

        hadPendingBalance = player.commitBalanceNotInHand(new BigDecimal(10000000));
        assertThat(hadPendingBalance, is(true));
        assertThat(player.getBalanceNotInHand(), is(BigDecimal.ZERO));
        assertThat(player.getBalance(), is(balance.add(pendingBalance)));
    }

    @Test
    public void testCommitPendingAmountWithMaxLevel1() {
        BigDecimal balance = new BigDecimal(100l);
        BigDecimal pending = new BigDecimal(200l);
        BigDecimal maxBuyIn = new BigDecimal(1000l);

        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        player.setBalance(balance);
        player.addNotInHandAmount(pending);
        boolean hadPending = player.commitBalanceNotInHand(maxBuyIn);

        assertThat(hadPending, is(true));
        assertThat(player.getBalanceNotInHand(), is(BigDecimal.ZERO));
        assertThat(player.getBalance(), is(balance.add(pending)));
    }

    @Test
    public void testCommitPendingAmountWithMaxLevel2() {
        BigDecimal balance = new BigDecimal(100l);
        BigDecimal pending = new BigDecimal(200l);
        BigDecimal maxBuyIn = new BigDecimal(120l);

        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        player.setBalance(balance);
        player.addNotInHandAmount(pending);
        boolean balanceUpdated = player.commitBalanceNotInHand(maxBuyIn);

        assertThat(balanceUpdated, is(true));
        assertThat(player.getBalanceNotInHand(), is(balance.add(pending).subtract(maxBuyIn)));
        assertThat(player.getBalance(), is(maxBuyIn));
    }

    @Test
    public void testCommitPendingAmountWithMaxLevel3() {
        BigDecimal balance = new BigDecimal(100l);
        BigDecimal pending = new BigDecimal(200l);
        BigDecimal maxBuyIn = new BigDecimal(50l);

        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        player.setBalance(balance);
        player.addNotInHandAmount(pending);
        boolean balanceUpdated = player.commitBalanceNotInHand(maxBuyIn);

        assertThat(balanceUpdated, is(false));
        assertThat(player.getBalanceNotInHand(), is(pending));
        assertThat(player.getBalance(), is(balance));
    }

    @Test
    public void testCommitPendingAmountWithMaxLevel4() {
        BigDecimal balance = new BigDecimal(200l); // player went all in
        BigDecimal pending = new BigDecimal(200l); // player brought max in after going all in
        BigDecimal maxBuyIn = new BigDecimal(200l);

        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);
        player.setBalance(balance);
        player.addNotInHandAmount(pending);
        boolean balanceUpdated = player.commitBalanceNotInHand(maxBuyIn);

        assertThat(balanceUpdated, is(false));
        assertThat(player.getBalanceNotInHand(), is(pending));
        assertThat(player.getBalance(), is(balance));
    }

    @Test
    public void testAddPendingAmount() {
        DefaultPokerPlayer player = new DefaultPokerPlayer(1337);

        assertThat(player.getBalanceNotInHand(), is(BigDecimal.ZERO));

        BigDecimal pendingAmount = new BigDecimal(333);
        player.addNotInHandAmount(pendingAmount);
        assertThat(player.getBalanceNotInHand(), is(pendingAmount));

        player.addNotInHandAmount(pendingAmount);
        assertThat(player.getBalanceNotInHand(), is(pendingAmount.multiply(new BigDecimal(2))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResetPlayerBeforeNewHand() {
        DefaultPokerPlayer player = new DefaultPokerPlayer(3434);
        ActionRequest oldActionRequest = new ActionRequest();
        player.setActionRequest(oldActionRequest);

        player.pocketCards = mock(Hand.class);
        player.publicPocketCards = mock(Set.class);
        player.privatePocketCards = mock(Set.class);

        player.setExposingPocketCards(true);
        player.setHasFolded(true);
        player.setHasActed(true);

        player.resetBeforeNewHand();

        assertThat(player.hasActed(), is(false));
        assertThat(player.hasFolded(), is(false));
        assertThat(player.isExposingPocketCards(), is(false));
        assertThat(player.getActionRequest(), not(sameInstance(oldActionRequest)));

        verify(player.pocketCards).clear();
        verify(player.publicPocketCards).clear();
        verify(player.privatePocketCards).clear();
    }
}
