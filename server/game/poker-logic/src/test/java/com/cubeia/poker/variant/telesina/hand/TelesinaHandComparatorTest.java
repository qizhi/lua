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

package com.cubeia.poker.variant.telesina.hand;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.Rank;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TelesinaHandComparatorTest {


    private TelesinaHandComparator createComparatorByLowestRank(Rank lowestRank, int playersInHand) {
        TelesinaHandStrengthEvaluator evaluator = new TelesinaHandStrengthEvaluator(lowestRank);
        return new TelesinaHandComparator(evaluator, playersInHand);
    }

    @Test
    public void testShortHandsOneCard() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // high card wins
        assertTrue(comp.compare(new Hand("AS"), new Hand("KC")) > 0);
        assertTrue(comp.compare(new Hand("KS"), new Hand("8C")) > 0);
        assertTrue(comp.compare(new Hand("KS"), new Hand("AC")) < 0);
        assertTrue(comp.compare(new Hand("TS"), new Hand("QC")) < 0);

        // suit breaks ties
        assertTrue(comp.compare(new Hand("TS"), new Hand("TC")) < 0);
        assertTrue(comp.compare(new Hand("9H"), new Hand("9S")) > 0);

        // equal cards are equal
        assertTrue(comp.compare(new Hand("TS"), new Hand("TS")) == 0);
    }

    @Test
    public void testShortHandsTwoCards() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // high card wins
        assertTrue(comp.compare(new Hand("AS TD"), new Hand("KH QH")) > 0);

        // high card suit wins
        assertTrue(comp.compare(new Hand("AH TD"), new Hand("AS KS")) > 0);

        // second card kicks
        assertTrue(comp.compare(new Hand("AS QD"), new Hand("AS 7H")) > 0);

        // second card suit wins in the end
        assertTrue(comp.compare(new Hand("AS QD"), new Hand("AS QC")) > 0);

        // a pair wins
        assertTrue(comp.compare(new Hand("8S 8H"), new Hand("AS QC")) > 0);
        assertTrue(comp.compare(new Hand("TS TC"), new Hand("8S 8H")) > 0);

        // best suited pair kicks
        assertTrue(comp.compare(new Hand("8S 8H"), new Hand("8C 8D")) > 0);
    }

    @Test
    public void testShortHandsThreeCards() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // three of a kinds found
        assertTrue(comp.compare(new Hand("8S 8H 8D"), new Hand("AS AC QC")) > 0);

        // best suited pair kicks over "kicker"
        assertTrue(comp.compare(new Hand("8S 8H AH"), new Hand("8C 8D QC")) > 0);
    }

    @Test
    public void testShortHandsFourCards() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // three of a kind beats two pair
        assertTrue(comp.compare(new Hand("8S 8H 8D 7C"), new Hand("AS AC QC QH")) > 0);

        // two pair beats one pair
        assertTrue(comp.compare(new Hand("AS AC QC QH"), new Hand("8S 8H 9D 7C")) > 0);

        // regular two pair cases
        assertTrue(comp.compare(new Hand("AS AH QC QH"), new Hand("KC KD QS QD")) > 0);
        assertTrue(comp.compare(new Hand("AS AH QC QH"), new Hand("AC AD TS TD")) > 0);

        // high suit kicks two pair
        assertTrue(comp.compare(new Hand("AS AH QC QH"), new Hand("AC AD QS QD")) > 0);

        // one pair found
        assertTrue(comp.compare(new Hand("AS AC QC JH"), new Hand("7S 8H 9D TC")) > 0);

        // kicker beats 4-card straight flush
        assertTrue(comp.compare(new Hand("AS QD JS 9H"), new Hand("8C 9C TC JC")) > 0);
    }

    @Test
    public void testHighCard() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // highest rank wins
        assertTrue(comp.compare(new Hand("AS KC QH JH 7S"), new Hand("KC QH JH TD 7S")) > 0);
        assertTrue(comp.compare(new Hand("KS 9C 8H JH 7S"), new Hand("QS 9C 8H JH 7S")) > 0);

        // suit decides if highest card share rank
        assertTrue(comp.compare(new Hand("KH TS 9S 8S 7S"), new Hand("KD QS JS TS 8S")) > 0);
        assertTrue(comp.compare(new Hand("AC TS 9S 8S 7S"), new Hand("AS QS JD TS 9S")) > 0);

        // if highest card shared second card is inspected
        assertTrue(comp.compare(new Hand("KH QS 9S 8S 7S"), new Hand("KH JS TS 9S 8D")) > 0);
        assertTrue(comp.compare(new Hand("AC TD 9S 8S 7S"), new Hand("AC TS 9S 8S 7S")) > 0);

        // highest high card hand found in six card hand
        assertTrue(comp.compare(new Hand("7S 8D 9H TD QS KS"), new Hand("QD JC TH 9D 7C")) > 0);
        assertTrue(comp.compare(new Hand("AC QD TD 9S 8S 7S"), new Hand("AC QS 9S 8S 7S")) > 0);
    }

    @Test
    public void testPairs() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // pair beats high card
        assertTrue(comp.compare(new Hand("8S 8C TH 9H 7S"), new Hand("AS KC QH JH 7S")) > 0);

        // different pairs
        assertTrue(comp.compare(new Hand("QS QC 9S 8H 7S"), new Hand("KS KC 9S 8H 7S")) < 0);
        assertTrue(comp.compare(new Hand("KS KC 9S 8H 7S"), new Hand("QS QC 9S 8H 7S")) > 0);

        assertTrue(comp.compare(new Hand("AS AC KS 8H 7S"), new Hand("KD KC QS 8H 7S")) > 0);
        assertTrue(comp.compare(new Hand("KD KC QS 8H 7S"), new Hand("AS AC KS 8H 7S")) < 0);

        assertTrue(comp.compare(new Hand("AS AC KS 8H 7S"), new Hand("7D 7C KS 8H 9S")) > 0);
        assertTrue(comp.compare(new Hand("7D 7C KS 8H 9S"), new Hand("AS AC KS 8H 7S")) < 0);

        // kicker decides
        assertTrue(comp.compare(new Hand("AS AC KS 8H 7S"), new Hand("AD AH QS 8H 7S")) > 0);
        assertTrue(comp.compare(new Hand("AD AH QS 8H 7S"), new Hand("AS AC KS 8H 7S")) < 0);

        // kickers are ranked hearts, diamonds, clubs, spades
        assertTrue(comp.compare(new Hand("AS AC KH 8S 7S"), new Hand("AD AH KD QH JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KH 8S 7S"), new Hand("AD AH KC QH JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KH 8S 7S"), new Hand("AD AH KS QH JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KD 8S 7S"), new Hand("AD AH KC QH JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KD 8S 7S"), new Hand("AD AH KS QH JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KC 8S 7S"), new Hand("AD AH KS QH JS")) > 0);

        // second kicker used
        assertTrue(comp.compare(new Hand("AS AC KH QH 7S"), new Hand("AD AH KH JH 7S")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KH QH 7S"), new Hand("AD AH KH QS JH")) > 0);

    }

    @Test
    public void testPairs2() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // kicker decides
        assertTrue(comp.compare(new Hand("AS AC KS 8H 7S"), new Hand("AD AH QS 8H 7S")) > 0);

    }


    @Test
    public void testTwoPairs() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // two pairs is better than one pair
        assertTrue(comp.compare(new Hand("8S 8D 9S 9D TC"), new Hand("AS AC KH QH JH")) > 0);

        // two pairs is better than high card
        assertTrue(comp.compare(new Hand("8S 8D 9S 9D TC"), new Hand("AS KH QS JD 9H")) > 0);

        // highest pair wins
        assertTrue(comp.compare(new Hand("AS AC 7S 7C JD"), new Hand("KS KC QS QC JD")) > 0);
        assertTrue(comp.compare(new Hand("AS AC 7S 7C JD"), new Hand("8S 8C 7H 7D JD")) > 0);
        assertTrue(comp.compare(new Hand("TS TC 7S 7C JD"), new Hand("8S 8C 7H 7D JD")) > 0);

        // second pair decides if first share rank
        assertTrue(comp.compare(new Hand("AS AC KS KC JD"), new Hand("AH AD QS QC JD")) > 0);
        assertTrue(comp.compare(new Hand("AS AC QS QC JD"), new Hand("AH AD 7S 7C JD")) > 0);

        // kicker decides if both pairs share rank
        // kicker rank has presidence
        assertTrue(comp.compare(new Hand("AS AC KS KC JD"), new Hand("AH AD KH KD 6D")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KS KC QD"), new Hand("AH AD KH KD 7H")) > 0);

        // then kicker suit follows
        assertTrue(comp.compare(new Hand("AS AC KS KC JD"), new Hand("AH AD KH KD JS")) > 0);
        assertTrue(comp.compare(new Hand("AS AC KS KC TC"), new Hand("AH AD KH KD TS")) > 0);

        // finally if kicker is equal best suit in high pair decides
        assertTrue(comp.compare(new Hand("AH AD KS KC JD"), new Hand("AS AC KH KD JD")) > 0);
        assertTrue(comp.compare(new Hand("AD AC KS KC JD"), new Hand("AH AS KH KD JD")) < 0);

        // two pairs found in 6 card hand
        assertTrue(comp.compare(new Hand("JC 8S 8D 9S 9D TC"), new Hand("AS AC KH QH JH")) > 0);

        // best two pairs found in six card hand
        assertTrue(comp.compare(new Hand("8C 8D 9C 9D KC KD"), new Hand("QC QD JC JD 7S")) > 0);

        // best two pairs with best kicker
        assertTrue(comp.compare(new Hand("8H 8S 9C 9D KC KD"), new Hand("8D 9C 9D KC KD")) > 0);
    }

    @Test
    public void testTwoPair2() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);
        assertTrue(comp.compare(new Hand("AH AD KS KC JD"), new Hand("AS AC KH KD JD")) > 0);
    }

    @Test
    public void testThreeOfAKind() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // three of a kind beats two pair
        assertTrue(comp.compare(new Hand("AS AC AD 7S 8D"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("9S 9C 9D 7S 8D"), new Hand("AS AC KH KS JH")) > 0);

        // three of a kind beats pair
        assertTrue(comp.compare(new Hand("AS AC AD 7S 8D"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("9S 9C 9D 7S 8D"), new Hand("AS AC KH QS JH")) > 0);

        // three of a kind beats high card
        assertTrue(comp.compare(new Hand("9S 9C 9D 7S 8D"), new Hand("AS KH QS JD 9H")) > 0);

        // higher rank three of a kind wins
        assertTrue(comp.compare(new Hand("AS AC AD 7S 8D"), new Hand("QS QC QD 7S 8D")) > 0);
        assertTrue(comp.compare(new Hand("9S 9C 9D 7S 8D"), new Hand("7S 7C 7D AS KD")) > 0);

        // three of a kind found in 6 card hand
        assertTrue(comp.compare(new Hand("TD 9S 9C 8D 7S 9D"), new Hand("7S 7C 7D AS KD")) > 0);
    }

    @Test
    public void testStraight() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // straight beats three of a kind
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("KS KC 7D AS KD")) > 0);

        // straight beats two pair
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AS AC KH KS JH")) > 0);

        // straight beats pair
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AS AC KH QS JH")) > 0);

        // straight beats high card
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AS KH QS JD 9H")) > 0);

        // card order not significant
        assertTrue(comp.compare(new Hand("9H 7D 8C TS JH"), new Hand("KS KC 7D AS KD")) > 0);
        assertTrue(comp.compare(new Hand("7D 8C 9H JH TS"), new Hand("KS KC 7D AS KD")) > 0);

        // ace may be low in straight
        assertTrue(comp.compare(new Hand("AC 7D 8C 9H TS"), new Hand("KS KC 7D AS KD")) > 0);

        // straight found in six card hand
        assertTrue(comp.compare(new Hand("JC 9H 7D 8C TS JH"), new Hand("KS KC 7D AS KD")) > 0);
        assertTrue(comp.compare(new Hand("KS 7D 8C 9H JH TS"), new Hand("KS KC 7D AS KD")) > 0);
        assertTrue(comp.compare(new Hand("AS 7D 8C 9H QH TS"), new Hand("KS KC 7D AS KD")) > 0);

        // high card rank wins
        assertTrue(comp.compare(new Hand("8C 9H TS JH QD"), new Hand("7D 8C 9H TS JH")) > 0);
        assertTrue(comp.compare(new Hand("TS JH QD KS AH"), new Hand("9H TS JH QD KS")) > 0);

        // low ace straight conts as low
        assertTrue(comp.compare(new Hand("7D 8C 9H TS JH"), new Hand("AC 7D 8C 9H TS")) > 0);

        // equal rank straights use kicker suit
        assertTrue(comp.compare(new Hand("TS JS QS KS AH"), new Hand("TH JH QH KH AC")) > 0);
        assertTrue(comp.compare(new Hand("9S TS JS QS KH"), new Hand("9H TH JH QH KC")) > 0);

        // low ace is not kicker
        assertTrue(comp.compare(new Hand("AS 7S 8S 9S TH"), new Hand("AH 7H 8H 9H TC")) > 0);

        // equal kicker delegates to second kicker
        assertTrue(comp.compare(new Hand("TS JS QS KH AD"), new Hand("TH JH QH KC AD")) > 0);

        // strippedness of deck decides if ace may be low
        comp = createComparatorByLowestRank(Rank.NINE, 3);
        assertTrue(comp.compare(new Hand("AC 9H TS JD QH"), new Hand("KS KC 7D AS KD")) > 0);

        comp = createComparatorByLowestRank(Rank.FIVE, 3);
        assertTrue(comp.compare(new Hand("AC 5H 6S 7D 8H"), new Hand("KS KC 7D AS KD")) > 0);
        assertTrue(comp.compare(new Hand("AC 6H 7S 8D 9H"), new Hand("KS KC 7D AS KD")) < 0);
        assertTrue(comp.compare(new Hand("AC 9H TS JD QH"), new Hand("KS KC 7D AS KD")) < 0);
    }

    @Test
    public void testFullHouse() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // full house beats straight
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("TS JS QS KH AD")) > 0);

        // full house beats three of a kind
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("KS KC 7D AS KD")) > 0);

        // full house beats two pair
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("AS AC KH KS JH")) > 0);

        // full house beats pair
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("AS AC KH QS JH")) > 0);

        // full house beats high card
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("AS KH QS JD 9H")) > 0);

        // full house found in six card hand
        assertTrue(comp.compare(new Hand("8D 7H 8H 8C 9D 9S"), new Hand("TS JS QS KH AD")) > 0);

        // high ranking three of a kind wins
        assertTrue(comp.compare(new Hand("8D 8H 8C 9D 9S"), new Hand("7D 7H 7C AD AS")) > 0);
        assertTrue(comp.compare(new Hand("AD AH AC 9D 9S"), new Hand("KD KH KC 9C 9H")) > 0);
    }

    @Test
    public void testFlush() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // flush beats full house
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("KD KH KC 9C 9H")) > 0);

        // flush beats straight
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("TS JS QS KH AD")) > 0);

        // flush beats three of a kind
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("KS KC 7D AS KD")) > 0);

        // flush beats two pair
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("AS AC KH KS JH")) > 0);

        // flush beats pair
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("AS AC KH QS JH")) > 0);

        // flush beats high card
        assertTrue(comp.compare(new Hand("8D TD QD KD AD"), new Hand("AS KH QS JD 9H")) > 0);

        // flush suit decides winner
        assertTrue(comp.compare(new Hand("8H TH JH QH KH"), new Hand("8D JD QD KD AD")) > 0);
        assertTrue(comp.compare(new Hand("8D TD JD QD KD"), new Hand("8C JC QC KC AC")) > 0);

        // flush chosen over straight in six card hand
        assertTrue(comp.compare(new Hand("7D 8C 9D TD JD QD"), new Hand("AS AD AH KS KH")) > 0);

        // if same suit flush high card decides
        comp = createComparatorByLowestRank(Rank.FIVE, 3);
        assertTrue(comp.compare(new Hand("5D 6D 7D JD AD"), new Hand("8D 9D TD QD KD")) > 0);

        // if both hands have same highest card (and second, third and fourth highest), the next highest should decide.
        assertTrue(comp.compare(new Hand("5D 6D 7D JD AD"), new Hand("4D 6D 7D JD AD")) > 0);

        // ... with second card if highest shared
        assertTrue(comp.compare(new Hand("5D 6D 7D KD AD"), new Hand("8D 9D TD QD AD")) > 0);
    }

    @Test
    public void testFourOfAKind() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // four of a kind beats flush
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("7D 8D 9D KD AD")) > 0);

        // four of a kind beats full house
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("KD KH KC 9C 9H")) > 0);

        // four of a kind beats straight
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("TS JS QS KH AD")) > 0);

        // four of a kind beats three of a kind
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("KS KC 7D AS KD")) > 0);

        // four of a kind beats two pair
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("AS AC KH KS JH")) > 0);

        // four of a kind beats pair
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("AS AC KH QS JH")) > 0);

        // four of a kind beats high card
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("AS KH QS JD 9H")) > 0);

        // higher rank four of a kind wins
        assertTrue(comp.compare(new Hand("9D 9S 9C 9H TS"), new Hand("7D 7S 7C 7H TC")) > 0);
        assertTrue(comp.compare(new Hand("AD AS AC AH TS"), new Hand("KD KS KC KH TC")) > 0);

        // four of a kind found in six card hand
        assertTrue(comp.compare(new Hand("9D 9S 8C 9C 9H TS"), new Hand("7D 8D 9D KD AD")) > 0);
    }

    @Test
    public void testStraightFlush() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        // straight flush beats four of a kind
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AD AS AC AH TC")) > 0);

        // straight flush beats flush
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("7D 8D 9D KD AD")) > 0);

        // straight flush beats full house
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("KD KH KC 9C 9H")) > 0);

        // straight flush beats straight
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("TS JS QS KH AD")) > 0);

        // straight flush beats three of a kind
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("KS KC 7D AS KD")) > 0);

        // straight flush beats two pair
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AS AC KH KS JH")) > 0);
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AS AC KH KS JH")) > 0);

        // straight flush beats pair
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AS AC KH QS JH")) > 0);
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AS AC KH QS JH")) > 0);

        // straight flush beats high card
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS"), new Hand("AS KH QS JD 9H")) > 0);

        // straight flush found in six card hand
        assertTrue(comp.compare(new Hand("7S 8S 9S TS JS KS"), new Hand("AD AS AC AH TC")) > 0);
        assertTrue(comp.compare(new Hand("7S 8S 9S TS TC JS"), new Hand("AD AS AC AH TC")) > 0);

        // high card rank wins
        assertTrue(comp.compare(new Hand("8S 9S TS JS QS"), new Hand("7H 8H 9H TH JH")) > 0);
        assertTrue(comp.compare(new Hand("TS JS QS KS AS"), new Hand("7H 8H 9H TH JH")) > 0);

        // low ace straight conts as low
        assertTrue(comp.compare(new Hand("AS 7S 8S 9S TS"), new Hand("8H 9H TH JH QH")) > 0);

        // equal rank straights use kicker suit
        assertTrue(comp.compare(new Hand("8D 9D TD JD QD"), new Hand("8S 9S TS JS QS")) > 0);
        assertTrue(comp.compare(new Hand("8H 9H TH JH QH"), new Hand("8D 9D TD JD QD")) > 0);

        // strippedness of deck decides if ace may be low
        comp = createComparatorByLowestRank(Rank.FIVE, 3);
        assertTrue(comp.compare(new Hand("AS 5S 6S 7S 8S"), new Hand("AD AS AC AH TC")) > 0);

        comp = createComparatorByLowestRank(Rank.NINE, 3);
        assertTrue(comp.compare(new Hand("AS 9S TS JS QS"), new Hand("AD AS AC AH TC")) > 0);
    }

    @Test
    public void testStraightFlushLowestBeatsHighestHeadsUp() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 2);

        Hand highestHearts = new Hand("TH JH QH KH AH");
        Hand lowestSpades = new Hand("AS 7S 8S 9S TS");
        Hand highestClubs = new Hand("TC JC QC KC AC");

        assertThat(comp.compare(lowestSpades, highestHearts) > 0, is(true));
        assertThat(comp.compare(highestHearts, lowestSpades) < 0, is(true));
        assertThat(comp.compare(lowestSpades, lowestSpades) == 0, is(true));
        assertThat(comp.compare(highestHearts, highestHearts) == 0, is(true));

        assertThat(comp.compare(highestHearts, highestClubs) > 0, is(true));
        assertThat(comp.compare(highestClubs, lowestSpades) > 0, is(true));


        // strippedness of deck decides if ace may be low
        comp = createComparatorByLowestRank(Rank.FIVE, 2);
        lowestSpades = new Hand("AS 5S 6S 7S 8S");
        assertThat(comp.compare(lowestSpades, highestHearts) > 0, is(true));
        assertThat(comp.compare(highestHearts, lowestSpades) < 0, is(true));

        assertThat(comp.compare(highestHearts, highestClubs) > 0, is(true));
        assertThat(comp.compare(highestClubs, lowestSpades) > 0, is(true));

        comp = createComparatorByLowestRank(Rank.NINE, 2);
        lowestSpades = new Hand("AS 9S TS JS QS");
        assertThat(comp.compare(lowestSpades, highestHearts) > 0, is(true));
        assertThat(comp.compare(highestHearts, lowestSpades) < 0, is(true));

        assertThat(comp.compare(highestHearts, highestClubs) > 0, is(true));
        assertThat(comp.compare(highestClubs, lowestSpades) > 0, is(true));
    }

    @Test
    public void testStraightFlushLowestDontBeatHighestWhenNotHeadsUp() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        Hand highestHearts = new Hand("TH JH QH KH AH");
        Hand lowestSpades = new Hand("AS 7S 8S 9S TS");
        Hand highestClubs = new Hand("TC JC QC KC AC");

        assertThat(comp.compare(lowestSpades, highestHearts) > 0, is(false));
        assertThat(comp.compare(highestHearts, lowestSpades) < 0, is(false));
        assertThat(comp.compare(highestHearts, highestClubs) > 0, is(true));
        assertThat(comp.compare(highestClubs, lowestSpades) > 0, is(true));
    }

    @Test
    public void testEqualHandsAreEqual() {
        TelesinaHandComparator comp = createComparatorByLowestRank(Rank.SEVEN, 3);

        assertEquals(0, comp.compare(new Hand("7S 8S 9C TC TS"), new Hand("7S 8S 9C TC TS")));

        // order disregarded
        assertEquals(0, comp.compare(new Hand("7S 8S 9C TC TS"), new Hand("9C TC TS 7S 8S")));

        // best hand found in >5 card hands
        assertEquals(0, comp.compare(new Hand("QS 8S 9C TC TS"), new Hand("QS 8S 9C TC TS 7D")));
        assertEquals(0, comp.compare(new Hand("QS 8S 9C TC TS"), new Hand("7D QS 8S 9C TC TS")));
        assertEquals(0, comp.compare(new Hand("QS 8S 9C TC TS 7D"), new Hand("QS 8S 9C TC TS 7D")));

        // best hand found in competition with similar hands
        assertEquals(0, comp.compare(new Hand("KD KC KS JH TH"), new Hand("KD KC KS JH TH 8C 7D")));
    }
}
