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
package com.cubeia.poker.variant.texasholdem;

import com.cubeia.poker.hand.*;
import com.cubeia.poker.handhistory.api.HandStrengthCommon;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TexasHoldemHandCalculatorTest {

    private TexasHoldemHandCalculator calculator;

    @Before
    public void setup() {
        calculator = new TexasHoldemHandCalculator();
    }

    @Test
    public void testGetBestHandInfoForPocketCards() throws Exception {
        HandInfo info = calculator.getBestHandInfo(new Hand("7S 8S"));
        assertEquals(HandType.HIGH_CARD, info.getHandType());
    }

    @Test
    public void testGetBestHandInfoForFullBoard() throws Exception {
        HandInfo info = calculator.getBestHandInfo(new Hand("7S 8S 2S JC QD 4S 5S"));
        assertEquals(HandType.FLUSH, info.getHandType());
    }

    @Test
    public void testCheckTranslate() {
        Hand hand = new Hand("KC QC JC TC 9C");
        HandStrength hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.STRAIGHT_FLUSH));
        assertThat(hs.getCards().size(), is(5));

        HandStrengthCommon translate = hs.translate();


        hand = new Hand("AC 2C 4C 3C 5C");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.STRAIGHT_FLUSH));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();


        hand = new Hand("KC QC JC TC 8C");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.FLUSH));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC QC JC TC 9H");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.STRAIGHT));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC KH KD TC TH");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.FULL_HOUSE));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC KH KD 9C TH");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.THREE_OF_A_KIND));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC KH 9D 9C TH");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.TWO_PAIRS));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC KH 8D 9C 2H");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.PAIR));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();

        hand = new Hand("KC 3H 7D 4C TH");
        hs = calculator.getHandStrength(hand);
        assertThat(hs.getHandType(), is(HandType.HIGH_CARD));
        assertThat(hs.getCards().size(), is(5));

        translate = hs.translate();




    }




}
