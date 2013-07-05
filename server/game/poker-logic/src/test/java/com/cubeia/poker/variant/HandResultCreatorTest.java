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

package com.cubeia.poker.variant;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.rake.LinearRakeWithLimitCalculator;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.variant.telesina.hand.TelesinaHandStrengthEvaluator;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HandResultCreatorTest {

    public static final BigDecimal RAKE_FRACTION = new BigDecimal("0.04");
    public static final BigDecimal RAKE_LIMIT = new BigDecimal(500);
    public static final BigDecimal RAKE_LIMIT_HEADS_UP = new BigDecimal(150);

    TelesinaHandStrengthEvaluator hte;
    HandResultCalculator resultCalculator;
    Map<Integer, PokerPlayer> playerMap;
    HandResultCreator creator;
    List<Card> communityCards;
    PotHolder potHolder;

    @SuppressWarnings("unused")
    private Answer<Object> THROW_EXCEPTION = new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            throw new RuntimeException("Called unmocked method: " + invocation.getMethod().getName());
        }
    };
    private com.cubeia.games.poker.common.money.Currency eur = new Currency("EUR", 2);


    private void setupStuff(String velaCard) {
        hte = new TelesinaHandStrengthEvaluator(Rank.SEVEN);
        resultCalculator = new HandResultCalculator(hte);

        creator = new HandResultCreator(hte);
        playerMap = new HashMap<Integer, PokerPlayer>();
        communityCards = Card.list(velaCard);
        potHolder = new PotHolder(new LinearRakeWithLimitCalculator(
                new RakeSettings(RAKE_FRACTION, RAKE_LIMIT, RAKE_LIMIT_HEADS_UP),
                new Currency("EUR",2)));
    }

    @Test
    public void testFilterMuckingPlayers() {
        setupStuff("7S");
        PokerPlayer pp1 = mockPlayer(1, 50, false, false, new Hand("7S 8S JC QC QH"));
        String player2Hand = "7H 8D JD QS 9H";
        PokerPlayer pp2 = mockPlayer(2, 50, false, false, new Hand(player2Hand)); // pp2 wins using vela card
        playerMap.put(1, pp1);
        playerMap.put(2, pp2);

        potHolder.moveChipsToPotAndTakeBackUncalledChips(playerMap.values());

        Set<PokerPlayer> muckingPlayers = new HashSet<PokerPlayer>();
        muckingPlayers.add(pp2);
        HandResult result = creator.createHandResult(communityCards, resultCalculator, potHolder, playerMap,
                new ArrayList<Integer>(), muckingPlayers, eur);
        List<RatedPlayerHand> ratedPlayerHands = result.getPlayerHands();
        assertEquals(1, ratedPlayerHands.size());
    }

    @Test
    public void testRakeWithManyDecimals() {
        setupStuff("7S");
        Currency btc = new Currency("BTC", 6);
        potHolder = new PotHolder(new LinearRakeWithLimitCalculator(
                new RakeSettings(RAKE_FRACTION, RAKE_LIMIT, RAKE_LIMIT_HEADS_UP), btc));
        potHolder.callOrRaise();

        PokerPlayer pp1 = mockPlayer(1, "0.000059", false, false, new Hand("7S 8S JC QC QH"));
        String player2Hand = "7H 8D JD QS 9H";
        PokerPlayer pp2 = mockPlayer(2, "0.000059", false, false, new Hand(player2Hand)); // pp2 wins using vela card
        PokerPlayer pp3 = mockPlayer(3, "0.000059", false, false, new Hand("7S 8S JC QC QH"));
        playerMap.put(1, pp1);
        playerMap.put(2, pp2);
        playerMap.put(3, pp3);

        potHolder.moveChipsToPotAndTakeBackUncalledChips(playerMap.values());

        Set<PokerPlayer> muckingPlayers = new HashSet<PokerPlayer>();
        HandResult result = creator.createHandResult(communityCards, resultCalculator, potHolder, playerMap, new ArrayList<Integer>(), muckingPlayers, btc);
        BigDecimal totalRake = result.getRakeContributionByPlayer(pp1).add(result.getRakeContributionByPlayer(pp2)).add(result.getRakeContributionByPlayer(pp3));
        assertThat(totalRake, is(result.getTotalRake()));
    }

    @Test
    public void testCreateHandResultTelesinaStyle() {
        setupStuff("TS");
        PokerPlayer pp1 = mockPlayer(1, "50.00", false, false, new Hand("7S 8S JC QC QH"));
        PokerPlayer pp2 = mockPlayer(2, "50.00", false, false, new Hand("7H 8D JD QS 9H")); // pp2 wins using vela card
        playerMap.put(1, pp1);
        playerMap.put(2, pp2);
        potHolder.moveChipsToPotAndTakeBackUncalledChips(playerMap.values());

        Set<PokerPlayer> muckingPlayers = new HashSet<PokerPlayer>(playerMap.values());

        HandResult result = creator.createHandResult(communityCards, resultCalculator, potHolder, playerMap, new ArrayList<Integer>(), muckingPlayers,eur);

        assertNotNull(result);

        Map<Integer, BigDecimal> resultsSimplified = new HashMap<Integer, BigDecimal>();
        for (Entry<PokerPlayer, Result> entry : result.getResults().entrySet()) {
            resultsSimplified.put(entry.getKey().getId(), entry.getValue().getNetResult());
        }

        assertEquals(new BigDecimal("-50.00"), resultsSimplified.get(1));
        assertEquals(new BigDecimal("50.00"), resultsSimplified.get(2));
    }


    @Test
    public void testCreateHandResultForMultiPot4PlayerHand() {
        setupStuff("TS");
        PokerPlayer pp1 = mockPlayer(1, "4.56", true, false, new Hand("TD TC TH 8D 9D"));
        PokerPlayer pp2 = mockPlayer(2, "16.12", false, false, new Hand("JC JC JC QS KH"));
        PokerPlayer pp3 = mockPlayer(3, "16.12", false, false, new Hand("7H 8D JD QS AH"));
        PokerPlayer pp4 = mockPlayer(4, "1.00", false, true, new Hand("7H 8D JD QS 9H"));


        playerMap.put(1, pp1);
        playerMap.put(2, pp2);
        playerMap.put(3, pp3);
        playerMap.put(4, pp4);
        potHolder.moveChipsToPotAndTakeBackUncalledChips(playerMap.values());
        potHolder.callOrRaise();

        Set<PokerPlayer> muckingPlayers = new HashSet<PokerPlayer>(playerMap.values());

        HandResult result = creator.createHandResult(communityCards, resultCalculator, potHolder, playerMap, new ArrayList<Integer>(), muckingPlayers,eur);

        assertNotNull(result);

        BigDecimal resultTotalRake = result.getTotalRake();
        BigDecimal resultTotalNet = BigDecimal.ZERO;
        BigDecimal totalBets = BigDecimal.ZERO;
        BigDecimal totalWins = BigDecimal.ZERO;

        for (Result res : result.getResults().values()) {
            resultTotalNet =  resultTotalNet.add(res.getNetResult());
            totalBets = totalBets.add(res.getBets());
            totalWins = totalWins.add(res.getWinningsIncludingOwnBets());
        }

        BigDecimal expectedTotalRake = new BigDecimal("1.50");

        assertEquals(expectedTotalRake, resultTotalRake);
        assertEquals(expectedTotalRake, totalBets.subtract(totalWins));
        assertEquals(expectedTotalRake.negate(), resultTotalNet);

        BigDecimal totalContributedRake = BigDecimal.ZERO;
        for (PokerPlayer player : playerMap.values()) {
            totalContributedRake = totalContributedRake .add(result.getRakeContributionByPlayer(player));
        }

        assertEquals(expectedTotalRake, totalContributedRake);
    }

    @Test
    public void testCreateHandResultPairs() {
        setupStuff("7S");
        PokerPlayer pp1 = mockPlayer(1, "50.00", false, false, new Hand("8S 8C JC TD QH"));
        PokerPlayer pp2 = mockPlayer(2, "50.00", false, false, new Hand("TH TD QD 9S JH")); // pp2 wins with higher pair
        playerMap.put(1, pp1);
        playerMap.put(2, pp2);
        potHolder.moveChipsToPotAndTakeBackUncalledChips(playerMap.values());

        Set<PokerPlayer> muckingPlayers = new HashSet<PokerPlayer>(playerMap.values());

        HandResult result = creator.createHandResult(communityCards, resultCalculator, potHolder, playerMap, new ArrayList<Integer>(), muckingPlayers,eur);

        System.out.println("HANDS: " + result.getPlayerHands());

        assertNotNull(result);

        Map<Integer, BigDecimal> resultsSimplified = new HashMap<Integer, BigDecimal>();
        for (Entry<PokerPlayer, Result> entry : result.getResults().entrySet()) {
            resultsSimplified.put(entry.getKey().getId(), entry.getValue().getNetResult());
        }

        assertEquals(new BigDecimal("-50.00"), resultsSimplified.get(1));
        assertEquals(new BigDecimal("50.00"), resultsSimplified.get(2));
    }

    @Test
    public void testCreatePotTransitionsByResults() {
        HandResultCreator creator = new HandResultCreator(null);

        Map<PokerPlayer, Result> playerResults = new HashMap<PokerPlayer, Result>();

        Pot pot0 = mock(Pot.class);
        Pot pot1 = mock(Pot.class);
        Pot pot2 = mock(Pot.class);

        PokerPlayer player1 = mock(PokerPlayer.class);
        Map<Pot, BigDecimal> winningsByPot1 = new HashMap<Pot, BigDecimal>();
        winningsByPot1.put(pot0, bd(20));
        winningsByPot1.put(pot1, bd(30));
        winningsByPot1.put(pot2, bd(50));
        Result result1 = new Result(bd(100), bd(10), winningsByPot1);

        PokerPlayer player2 = mock(PokerPlayer.class);
        Result result2 = new Result(bd(0), bd(10), new HashMap<Pot, BigDecimal>());

        playerResults.put(player1, result1);
        playerResults.put(player2, result2);

        Collection<PotTransition> potTrans = creator.createPotTransitionsByResults(playerResults);

        assertThat(potTrans.size(), is(3));
        assertThat(potTrans, JUnitMatchers.hasItem(new PotTransition(player1, pot0, bd(20))));
        assertThat(potTrans, JUnitMatchers.hasItem(new PotTransition(player1, pot1, bd(30))));
        assertThat(potTrans, JUnitMatchers.hasItem(new PotTransition(player1, pot2, bd(50))));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }
    private PokerPlayer mockPlayer(int playerId, long betStack, boolean allIn, boolean folded, Hand pocketCards) {
        return mockPlayer(playerId,""+betStack,allIn,folded,pocketCards);
    }
    private PokerPlayer mockPlayer(int playerId, String betStack, boolean allIn, boolean folded, Hand pocketCards) {
        PokerPlayer pp = mock(PokerPlayer.class);
        doReturn(playerId).when(pp).getId();
        doReturn(new BigDecimal(betStack)).when(pp).getBetStack();
        doReturn(allIn).when(pp).isAllIn();
        doReturn(folded).when(pp).hasFolded();
        doReturn(pocketCards).when(pp).getPocketCards();


        doReturn("Player" + playerId).when(pp).toString();

        return pp;
    }
}
