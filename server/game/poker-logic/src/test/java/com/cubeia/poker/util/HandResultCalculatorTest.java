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

package com.cubeia.poker.util;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.model.PlayerHand;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.rake.LinearRakeWithLimitCalculator;
import com.cubeia.poker.result.HandResultCalculator;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class HandResultCalculatorTest extends TestCase {

    private static final Logger log = Logger.getLogger(HandResultCalculatorTest.class);

    private Map<Integer, PokerPlayer> players;

    HandResultCalculator calc = new HandResultCalculator(new TexasHoldemHandCalculator());

    private ArrayList<PlayerHand> hands;
    private LinearRakeWithLimitCalculator rakeCalculator;

    private BigDecimal rakeFraction;

    private BigDecimal player1Bets = new BigDecimal("0.10");
    private BigDecimal player2Bets = new BigDecimal("0.20");
    private BigDecimal player3Bets = new BigDecimal("0.40");
    private Currency eur = new Currency("EUR",2);

    @Override
    protected void setUp() throws Exception {
        // All players has 100 and bets 10, 20 and 40 respectively
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(new BigDecimal("1.00"));
        p1.addBet(player1Bets);
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(new BigDecimal("1.00"));
        p2.addBet(player2Bets);
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(new BigDecimal("1.00"));
        p3.addBet(player3Bets);

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        hands = new ArrayList<PlayerHand>();

        String community = "Ac Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("As Ad " + community))); // Best Hand - 3 Aces
        hands.add(new PlayerHand(2, new Hand("2s 7d " + community)));
        hands.add(new PlayerHand(3, new Hand("3s 8d " + community)));

        rakeFraction = new BigDecimal("0.1");
        rakeCalculator = new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(rakeFraction),eur);
    }


    public void testSimpleCaseWithRake() {
        PotHolder potHolder = new PotHolder(rakeCalculator);
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(1, potHolder.getNumberOfPots());
        assertEquals(bd("0.50"), potHolder.getPotSize(0));
        Pot pot0 = potHolder.getPot(0);
        BigDecimal pot0Rake = new BigDecimal("0.05"); // remember that one of the players overbet the others and took back some of the bets

        BigDecimal p1stake = pot0.getPotContributors().get(players.get(1));
        assertEquals(bd("0.10"), p1stake);
        BigDecimal p2stake = pot0.getPotContributors().get(players.get(2));
        assertEquals(bd("0.20"), p2stake);
        BigDecimal p3stake = pot0.getPotContributors().get(players.get(3));
        assertEquals(bd("0.20"), p3stake);

        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players, eur);

        Result result1 = playerResults.get(players.get(1));
        assertThat(result1.getNetResult(), is(new BigDecimal("0.50").subtract(player1Bets).subtract(pot0Rake)));
        assertThat(result1.getWinningsIncludingOwnBets(), is(new BigDecimal("0.50").subtract(pot0Rake)));

        assertThat(result1.getWinningsByPot().size(), is(1));
        assertThat(result1.getWinningsByPot().get(potHolder.getActivePot()), is(new BigDecimal("0.50").subtract(pot0Rake)));

        assertEquals(3, playerResults.size());

        Result result2 = playerResults.get(players.get(2));
        assertThat(result2.getNetResult(), is(player2Bets.negate()));
        assertThat(result2.getWinningsByPot().isEmpty(), is(true));
        Result result3 = playerResults.get(players.get(3));
        assertThat(result3.getNetResult(), is(player2Bets.negate())); // remember that player 3 only bet player 2's bet in the end since he took some back
        assertThat(result3.getWinningsByPot().isEmpty(), is(true));
    }


    public void testGetWinnersWithRake() {
        hands = new ArrayList<PlayerHand>();
        String community = "Ac Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("As Ad " + community))); // SPLIT HAND - 3 Aces
        hands.add(new PlayerHand(2, new Hand("As Ad " + community))); // SPLIT HAND - 3 Aces
        hands.add(new PlayerHand(3, new Hand("3s 8d " + community)));

        PotHolder potHolder = new PotHolder(rakeCalculator);
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertThat(potHolder.getNumberOfPots(), is(1));
        BigDecimal pot0Size = potHolder.getPotSize(0);
        assertThat(pot0Size, is(new BigDecimal("0.50"))); // one of the players had an over bet so player 3 actually only bet what player 2 did.
        Pot pot0 = potHolder.getPot(0);
        BigDecimal pot0Rake = new BigDecimal("0.1").multiply(pot0Size).setScale(2);

        BigDecimal p1stake = pot0.getPotContributors().get(players.get(1));
        assertEquals(bd("0.10"), p1stake);
        BigDecimal p2stake = pot0.getPotContributors().get(players.get(2));
        assertEquals(bd("0.20"), p2stake);
        BigDecimal p3stake = pot0.getPotContributors().get(players.get(3));
        assertEquals(bd("0.20"), p3stake);

        assertEquals(1, potHolder.getNumberOfPots());

        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players,eur);

        Result result1 = playerResults.get(players.get(1));
        BigDecimal pot0WinningShare = bd("0.50").subtract(pot0Rake).divide(bd("2"),2,ROUND_DOWN);
        assertThat(result1.getNetResult(), is(pot0WinningShare.subtract(player1Bets).add(bd("0.01")))); // This guy gets the rounded cent.
        assertThat(result1.getWinningsIncludingOwnBets(), is(pot0WinningShare.add(new BigDecimal("0.01"))));
        assertThat(result1.getWinningsByPot().size(), is(1));
        assertThat(result1.getWinningsByPot().get(pot0), is(pot0WinningShare.add(bd("0.01"))));

        Result result2 = playerResults.get(players.get(2));
        assertThat(result2.getNetResult(), is(pot0WinningShare.subtract(player2Bets)));
        assertThat(result2.getWinningsIncludingOwnBets(), is(pot0WinningShare));

        assertThat(result2.getWinningsByPot().size(), is(1));
        assertThat(result2.getWinningsByPot().get(pot0), is(pot0WinningShare));

        Result result3 = playerResults.get(players.get(3));
        assertEquals(bd("-0.20"), result3.getNetResult());
        assertEquals(bd("0.00"), result3.getWinningsIncludingOwnBets());
        assertThat(result3.getWinningsByPot().size(), is(0));

        assertThat(playerResults.size(), is(3));
    }

    public void testTwoPairs() {
        // All players have 100 and bet 10 each.
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(bd("1.00"));
        p1.addBet(bd("0.10"));
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(bd("1.00"));
        p2.addBet(bd("0.10"));
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(bd("1.00"));
        p3.addBet(bd("0.10"));

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        hands = new ArrayList<PlayerHand>();
        String community = "Kc 7s 3s 3c 7h";
        hands.add(new PlayerHand(1, new Hand("9d Js " + community)));
        hands.add(new PlayerHand(2, new Hand("As 8h " + community))); // Winner because the Ace kicker plays.
        hands.add(new PlayerHand(3, new Hand("Qs 6h " + community)));

        PotHolder potHolder = new PotHolder(new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(ZERO),eur));
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(1, potHolder.getNumberOfPots());
        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players,eur);
        Result result1 = playerResults.get(players.get(1));
        Result result2 = playerResults.get(players.get(2));
        Result result3 = playerResults.get(players.get(3));
        log.debug("r1: " + result1);
        log.debug("r2: " + result2);
        log.debug("r3: " + result3);
        assertThat(result2.getNetResult(), is(bd("0.20")));
    }

    public void testMultiplePotsWithRake() {
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(bd("1.00"));
        p1.addBet(bd("0.80"));
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(bd("1.00"));
        p2.addBet(bd("0.80"));
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(bd("0.40"));
        p3.addBet(bd("0.40"));

        assertTrue(p3.isAllIn());

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        PotHolder potHolder = new PotHolder(rakeCalculator);
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(2, potHolder.getNumberOfPots());

        hands = new ArrayList<PlayerHand>();
        String community = " Ac Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("Ks 8d" + community))); // Second best hand - 2 Kings
        hands.add(new PlayerHand(2, new Hand("2s 7d" + community)));
        hands.add(new PlayerHand(3, new Hand("As Ad" + community))); // Best Hand - 3 Aces

        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players,eur);

        assertEquals(3, playerResults.size());

        Pot pot0 = potHolder.getPot(0);
        assertThat(pot0.getPotSize(), is(bd("1.20")));


        Result result1 = playerResults.get(players.get(1));
        assertThat(result1.getNetResult(), is(bd("-0.08"))); //80L - 8 - 80
        assertThat(result1.getWinningsIncludingOwnBets(), is(bd("0.72"))); //80 - 8L

        assertThat(result1.getWinningsByPot().size(), is(1));
        assertThat(result1.getWinningsByPot().get(potHolder.getPot(1)), is(bd("0.72"))); //80L - 8L


        Result result2 = playerResults.get(players.get(2));
        assertEquals(bd("-0.80"), result2.getNetResult());
        assertEquals(bd("0.00"), result2.getWinningsIncludingOwnBets());
        assertThat(result2.getWinningsByPot().isEmpty(), is(true));


        Result result3 = playerResults.get(players.get(3));
        assertThat(result3.getNetResult(), is(bd("0.68"))); //120L - 12L - 40L
        assertThat(result3.getWinningsIncludingOwnBets(), is(bd("1.08"))); //120L - 12L
        assertThat(result3.getWinningsByPot().size(), is(1));
        assertThat(result3.getWinningsByPot().get(pot0), is(bd("1.08"))); //120L - 12L
    }
    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
    private BigDecimal bd(long l) {
        return new BigDecimal(l);
    }

    public void testRoundingErrorsWhenSplitPot() {
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(bd(100));
        p1.addBet(bd(80));
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(bd(100));
        p2.addBet(bd(80));
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(bd(35));
        p3.addBet(bd(35));

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        PotHolder potHolder = new PotHolder(rakeCalculator);
        potHolder.callOrRaise();
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        hands = new ArrayList<PlayerHand>();
        String community = " 2c Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("As Ad" + community))); // Best Hand
        hands.add(new PlayerHand(2, new Hand("Ac Ah" + community))); // Best Hand - split
        hands.add(new PlayerHand(3, new Hand("Ks 8d" + community)));

        RakeInfoContainer totalRake = potHolder.calculateRake();
        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, totalRake, players,eur);

        Result result1 = playerResults.get(players.get(1));
        Result result2 = playerResults.get(players.get(2));
        Result result3 = playerResults.get(players.get(3));

        BigDecimal netResultSum = result1.getNetResult().add(result2.getNetResult()).add(result3.getNetResult());
        assertThat(netResultSum.add(totalRake.getTotalRake()), is(bd("0.00")));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    public void testMultiplePotsNoRake() {
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(bd("100.00"));
        p1.addBet(bd("80.00"));
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(bd("100.00"));
        p2.addBet(bd("80.00"));
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(bd("40.00"));
        p3.addBet(bd("40.00"));

        assertTrue(p3.isAllIn());

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        PotHolder potHolder = new PotHolder(new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(ZERO),eur));
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(2, potHolder.getNumberOfPots());

        hands = new ArrayList<PlayerHand>();
        String community = " Ac Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("Ks 8d" + community))); // Second best hand - 2 Kings
        hands.add(new PlayerHand(2, new Hand("2s 7d" + community)));
        hands.add(new PlayerHand(3, new Hand("As Ad" + community))); // Best Hand - 3 Aces

        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players,eur);

        assertEquals(3, playerResults.size());

        Result result1 = playerResults.get(players.get(1));
        assertEquals(bd("0.00"), result1.getNetResult());
        assertEquals(bd("80.00"), result1.getWinningsIncludingOwnBets());
        assertThat(result1.getWinningsByPot().size(), is(1));
        assertThat(result1.getWinningsByPot().get(potHolder.getPot(1)), is(bd("80.00")));


        Result result2 = playerResults.get(players.get(2));
        assertEquals(bd("-80.00"), result2.getNetResult());
        assertEquals(bd("0.00"), result2.getWinningsIncludingOwnBets());
        assertThat(result2.getWinningsByPot().isEmpty(), is(true));


        Result result3 = playerResults.get(players.get(3));
        assertEquals(bd("80.00"), result3.getNetResult());
        assertEquals(bd("120.00"), result3.getWinningsIncludingOwnBets());
        assertThat(result3.getWinningsByPot().size(), is(1));
        assertThat(result3.getWinningsByPot().get(potHolder.getPot(0)), is(bd("120.00")));

        assertEquals(bd("0.00"), result1.getNetResult().add(result2.getNetResult()).add(result3.getNetResult()));
    }


    public void testMultipleBetsNoRake() {
        players = new HashMap<Integer, PokerPlayer>();
        PokerPlayer p1 = new DefaultPokerPlayer(1);
        p1.addChips(bd(100));
        p1.addBet(bd(10));
        PokerPlayer p2 = new DefaultPokerPlayer(2);
        p2.addChips(bd(100));
        p2.addBet(bd(20));
        PokerPlayer p3 = new DefaultPokerPlayer(3);
        p3.addChips(bd(100));
        p3.addBet(bd(40)); // this is a overbet. This player will be returned the difference between p3 and p2's bet

        players.put(1, p1);
        players.put(2, p2);
        players.put(3, p3);

        hands = new ArrayList<PlayerHand>();
        String community = " Ac Kc Qd 6h Th";
        hands.add(new PlayerHand(1, new Hand("As Ad" + community))); // Best Hand - 3 Aces
        hands.add(new PlayerHand(2, new Hand("2s 7d" + community)));
        hands.add(new PlayerHand(3, new Hand("3s 8d" + community)));

        PotHolder potHolder = new PotHolder(new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(ZERO),eur));
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(1, potHolder.getNumberOfPots());
        assertEquals(bd(50), potHolder.getPotSize(0)); // remember the overbet

        // do another bet. This time without overbets
        p1.addBet(bd(10));
        p2.addBet(bd(20));
        p3.addBet(bd(20));
        potHolder.moveChipsToPotAndTakeBackUncalledChips(players.values());

        assertEquals(1, potHolder.getNumberOfPots());
        assertEquals(bd(100), potHolder.getPotSize(0)); // the total is 100
        BigDecimal p1stake = potHolder.getActivePot().getPotContributors().get(players.get(1));
        assertEquals(bd(20), p1stake);
        BigDecimal p2stake = potHolder.getActivePot().getPotContributors().get(players.get(2));
        assertEquals(bd(40), p2stake);
        BigDecimal p3stake = potHolder.getActivePot().getPotContributors().get(players.get(3));
        assertEquals(bd(40), p3stake);

        Map<PokerPlayer, Result> playerResults = calc.getPlayerResults(hands, potHolder, potHolder.calculateRake(), players,eur);

        Result result1 = playerResults.get(players.get(1));
        assertEquals(bd("80.00"), result1.getNetResult()); // the two other players betted 20+20 each
        assertEquals(bd("100.00"), result1.getWinningsIncludingOwnBets()); // the other players betted 20+20 each and player 1 only bet 10+10
        assertThat(result1.getWinningsByPot().size(), is(1));
        assertThat(result1.getWinningsByPot().get(potHolder.getPot(0)), is(bd("100.00")));

        assertEquals(3, playerResults.size());

    }
}
