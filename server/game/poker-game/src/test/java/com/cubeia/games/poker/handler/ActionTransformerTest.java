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

package com.cubeia.games.poker.handler;

import com.cubeia.games.poker.io.protocol.*;
import com.cubeia.games.poker.io.protocol.Enums.ActionType;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.hand.*;
import com.cubeia.poker.model.PlayerHand;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotTransition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cubeia.poker.hand.HandType.ROYAL_STRAIGHT_FLUSH;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionTransformerTest {

    private ActionTransformer actionTransformer;

    @Before
    public void setup() {
        actionTransformer = new ActionTransformer();

    }

    @Test
    public void testCreateHandEndPacket() {
        Hand hand1 = new Hand("As Ks");
        Hand hand2 = new Hand("Td Tc");

        Hand community = new Hand("Qs Js Ts 4d 2c");
        hand1.addCards(community.getCards());
        hand2.addCards(community.getCards());

        addIdsToCards(hand1);
        addIdsToCards(hand2);

        List<RatedPlayerHand> hands = new ArrayList<RatedPlayerHand>();
        hands.add(new RatedPlayerHand(new PlayerHand(11, hand1), new HandStrength(HandType.HIGH_CARD), Card.list("As Ks Qs Js Ts")));
        hands.add(new RatedPlayerHand(new PlayerHand(22, hand2), new HandStrength(HandType.HIGH_CARD), Card.list("Qs Js Ts Td Tc")));

        PotTransfers potTransfers = new PotTransfers();
        HandEnd end = actionTransformer.createHandEndPacket(hands, potTransfers, Arrays.asList(1, 2, 10));

        Assert.assertEquals(2, end.hands.size());
        Assert.assertNotSame("Two High", end.hands.get(0).handType.name());
        Assert.assertNotSame("Two High", end.hands.get(1).handType.name());

        assertThat(end.potTransfers, is(potTransfers));
        assertThat(end.playerIdRevealOrder, is(new int[]{1, 2, 10}));
    }

    private void addIdsToCards(Hand hand) {
        CardIdGenerator idGen = new IndexCardIdGenerator();
        List<Card> oldCards = hand.getCards();
        hand.clear();
        hand.addCards(idGen.copyAndAssignIds(oldCards));
    }

    @Test
    public void testCreatePrivateVisibleCards() {
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(0, "AS"));
        cards.add(new Card(1, "AS"));
        DealPrivateCards privateCards = actionTransformer.createPrivateCardsPacket(1, cards, false);
        Assert.assertEquals(2, privateCards.cards.size());
        CardToDeal dealtCard = privateCards.cards.get(0);
        Assert.assertEquals(1, dealtCard.player);
        Assert.assertEquals(Enums.Rank.ACE, dealtCard.card.rank);
        Assert.assertEquals(Enums.Suit.SPADES, dealtCard.card.suit);
    }

    @Test
    public void testCreatePrivateHiddenCards() {
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(0, "AS"));
        cards.add(new Card(1, "AH"));
        DealPrivateCards privateCards = actionTransformer.createPrivateCardsPacket(1, cards, true);
        Assert.assertEquals(2, privateCards.cards.size());
        CardToDeal dealtCard = privateCards.cards.get(0);
        Assert.assertEquals(1, dealtCard.player);
        Assert.assertEquals(Enums.Rank.HIDDEN, dealtCard.card.rank);
        Assert.assertEquals(Enums.Suit.HIDDEN, dealtCard.card.suit);
        dealtCard = privateCards.cards.get(1);
        Assert.assertEquals(1, dealtCard.player);
        Assert.assertEquals(Enums.Rank.HIDDEN, dealtCard.card.rank);
        Assert.assertEquals(Enums.Suit.HIDDEN, dealtCard.card.suit);
    }

    @Test
    public void testTransformActionTypeToPokerActionType() {
        assertThat("wrong number of action types, something broken?", ActionType.values().length, is(14));
        assertThat(actionTransformer.transform(ActionType.FOLD), is(PokerActionType.FOLD));
        assertThat(actionTransformer.transform(ActionType.CHECK), is(PokerActionType.CHECK));
        assertThat(actionTransformer.transform(ActionType.CALL), is(PokerActionType.CALL));
        assertThat(actionTransformer.transform(ActionType.BET), is(PokerActionType.BET));
        assertThat(actionTransformer.transform(ActionType.BIG_BLIND), is(PokerActionType.BIG_BLIND));
        assertThat(actionTransformer.transform(ActionType.SMALL_BLIND), is(PokerActionType.SMALL_BLIND));
        assertThat(actionTransformer.transform(ActionType.RAISE), is(PokerActionType.RAISE));
        assertThat(actionTransformer.transform(ActionType.DECLINE_ENTRY_BET), is(PokerActionType.DECLINE_ENTRY_BET));
        assertThat(actionTransformer.transform(ActionType.ANTE), is(PokerActionType.ANTE));
        assertThat(actionTransformer.transform(ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND), is(PokerActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND));
        assertThat(actionTransformer.transform(ActionType.DEAD_SMALL_BLIND), is(PokerActionType.DEAD_SMALL_BLIND));

        // sanity check
        for (ActionType at : ActionType.values()) {
            actionTransformer.transform(at);
        }
    }

    @Test
    public void testRankConversion() {
        assertThat(Enums.Rank.values().length, is(14));
        assertThat(Rank.values().length, is(13));

        assertThat(Enums.Rank.ACE, is(actionTransformer.convertRankToProtocolEnum(Rank.ACE)));
        assertThat(Enums.Rank.TWO, is(actionTransformer.convertRankToProtocolEnum(Rank.TWO)));
        assertThat(Enums.Rank.THREE, is(actionTransformer.convertRankToProtocolEnum(Rank.THREE)));
        assertThat(Enums.Rank.FOUR, is(actionTransformer.convertRankToProtocolEnum(Rank.FOUR)));
        assertThat(Enums.Rank.FIVE, is(actionTransformer.convertRankToProtocolEnum(Rank.FIVE)));
        assertThat(Enums.Rank.SIX, is(actionTransformer.convertRankToProtocolEnum(Rank.SIX)));
        assertThat(Enums.Rank.SEVEN, is(actionTransformer.convertRankToProtocolEnum(Rank.SEVEN)));
        assertThat(Enums.Rank.EIGHT, is(actionTransformer.convertRankToProtocolEnum(Rank.EIGHT)));
        assertThat(Enums.Rank.NINE, is(actionTransformer.convertRankToProtocolEnum(Rank.NINE)));
        assertThat(Enums.Rank.TEN, is(actionTransformer.convertRankToProtocolEnum(Rank.TEN)));
        assertThat(Enums.Rank.JACK, is(actionTransformer.convertRankToProtocolEnum(Rank.JACK)));
        assertThat(Enums.Rank.QUEEN, is(actionTransformer.convertRankToProtocolEnum(Rank.QUEEN)));
        assertThat(Enums.Rank.KING, is(actionTransformer.convertRankToProtocolEnum(Rank.KING)));
    }

    @Test
    public void testSuitConversion() {
        assertThat(Enums.Suit.values().length, is(4 + 1));
        assertThat(Suit.values().length, is(4));

        assertThat(Enums.Suit.CLUBS, is(actionTransformer.convertSuitToProtocolEnum(Suit.CLUBS)));
        assertThat(Enums.Suit.DIAMONDS, is(actionTransformer.convertSuitToProtocolEnum(Suit.DIAMONDS)));
        assertThat(Enums.Suit.HEARTS, is(actionTransformer.convertSuitToProtocolEnum(Suit.HEARTS)));
        assertThat(Enums.Suit.SPADES, is(actionTransformer.convertSuitToProtocolEnum(Suit.SPADES)));
    }

    @Test
    public void testHandTypeConvertaion() {
        assertThat(Enums.HandType.values().length, is(11));
        assertThat(HandType.values().length, is(11));

        assertThat(Enums.HandType.FLUSH, is(actionTransformer.convertHandTypeToEnum(HandType.FLUSH)));
        assertThat(Enums.HandType.FOUR_OF_A_KIND, is(actionTransformer.convertHandTypeToEnum(HandType.FOUR_OF_A_KIND)));
        assertThat(Enums.HandType.FULL_HOUSE, is(actionTransformer.convertHandTypeToEnum(HandType.FULL_HOUSE)));
        assertThat(Enums.HandType.HIGH_CARD, is(actionTransformer.convertHandTypeToEnum(HandType.HIGH_CARD)));
        assertThat(Enums.HandType.PAIR, is(actionTransformer.convertHandTypeToEnum(HandType.PAIR)));
        assertThat(Enums.HandType.STRAIGHT, is(actionTransformer.convertHandTypeToEnum(HandType.STRAIGHT)));
        assertThat(Enums.HandType.STRAIGHT_FLUSH, is(actionTransformer.convertHandTypeToEnum(HandType.STRAIGHT_FLUSH)));
        assertThat(Enums.HandType.THREE_OF_A_KIND, is(actionTransformer.convertHandTypeToEnum(HandType.THREE_OF_A_KIND)));
        assertThat(Enums.HandType.TWO_PAIR, is(actionTransformer.convertHandTypeToEnum(HandType.TWO_PAIRS)));
        assertThat(Enums.HandType.ROYAL_STRAIGHT_FLUSH, is(actionTransformer.convertHandTypeToEnum(HandType.ROYAL_STRAIGHT_FLUSH)));
        assertThat(Enums.HandType.UNKNOWN, is(actionTransformer.convertHandTypeToEnum(HandType.NOT_RANKED)));

    }

    @Test
    public void testCreatePlayerAction() {
        assertThat("wrong number of poker action types, something broken?", PokerActionType.values().length, is(14));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.FOLD).type, is(ActionType.FOLD));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.CHECK).type, is(ActionType.CHECK));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.CALL).type, is(ActionType.CALL));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.BET).type, is(ActionType.BET));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.BIG_BLIND).type, is(ActionType.BIG_BLIND));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.SMALL_BLIND).type, is(ActionType.SMALL_BLIND));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.RAISE).type, is(ActionType.RAISE));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.DECLINE_ENTRY_BET).type, is(ActionType.DECLINE_ENTRY_BET));
        assertThat(actionTransformer.createPlayerAction(PokerActionType.ANTE).type, is(ActionType.ANTE));

        // sanity check
        for (PokerActionType pat : PokerActionType.values()) {
            actionTransformer.createPlayerAction(pat);
        }
    }

    @Test
    public void testCreatePotTransferPacket() {
        PokerPlayer player = mock(PokerPlayer.class);
        int playerId = 333;
        when(player.getId()).thenReturn(playerId);
        Pot pot = mock(Pot.class);
        int potId = 23;
        when(pot.getId()).thenReturn(potId);
        BigDecimal amount = new BigDecimal(3434);

        PotTransition potTransition = new PotTransition(player, pot, amount);
        PotTransfer potTransferPacket = actionTransformer.createPotTransferPacket(potTransition);
        assertThat(potTransferPacket.amount, is(amount.toPlainString()));
        assertThat(potTransferPacket.playerId, is(playerId));
        assertThat(potTransferPacket.potId, is((byte) potId));
    }

    @Test
    public void testCreateBestHandPacket() {
        List<Card> cardsInHand = asList(new Card(1, "5H"), new Card(2, "JC"));
        BestHand createBestHandPacket = actionTransformer.createBestHandPacket(234, ROYAL_STRAIGHT_FLUSH, cardsInHand);
        assertThat(createBestHandPacket.handType, is(Enums.HandType.ROYAL_STRAIGHT_FLUSH));
        assertThat(createBestHandPacket.cards.size(), is(2));
        assertThat(createBestHandPacket.player, is(234));
    }

    @Test
    public void testConvertCards() {
        List<Card> cardsInHand = asList(new Card(1, "5H"), new Card(2, "JC"));
        List<GameCard> cards = actionTransformer.convertCards(cardsInHand);
        assertThat(cards.size(), is(2));

        GameCard card1 = cards.get(0);
        assertThat(card1.cardId, is(1));
        assertThat(card1.rank, is(Enums.Rank.FIVE));
        assertThat(card1.suit, is(Enums.Suit.HEARTS));

        GameCard card2 = cards.get(1);
        assertThat(card2.cardId, is(2));
        assertThat(card2.rank, is(Enums.Rank.JACK));
        assertThat(card2.suit, is(Enums.Suit.CLUBS));
    }

}
