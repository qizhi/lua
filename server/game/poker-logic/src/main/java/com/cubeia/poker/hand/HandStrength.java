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

package com.cubeia.poker.hand;

import com.cubeia.poker.handhistory.api.BestHandType;
import com.cubeia.poker.handhistory.api.GameCard;
import com.cubeia.poker.handhistory.api.HandInfoCommon;
import com.cubeia.poker.handhistory.api.HandStrengthCommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Holds information about the hand strength of a hand.</p>
 * <p/>
 * <p>
 * <h3>Groups</h3>
 * Groups are a bit magic in the sense that they will contain different values
 * depending on the hand type. The groups are ordered information as needed
 * by the hand comparator. The groups have been designed when implementing the
 * Telesina variant so they probably a bit Telesina specific at this time.
 * </p>
 * <p/>
 * <p>Below are the various hand types and what are contained in the groups
 * for the corresponding position (i.e. index+1) where it is of interest.</p>
 * <p/>
 * <p>
 * <strong>Pair</strong>
 * <ol>
 * <li>pair rank (unsuited so hard code HEART)</li>
 * <li>kickers</li>
 * <li>suit of pair cards</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Two Pairs</strong>
 * <ol>
 * <li>high pair rank (unsuited so hard code HEART)</li>
 * <li>low pair rank (unsuited so hard code HEART)</li>
 * <li>kicker</li>
 * <li>suit of high pair</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Three of a kind</strong>
 * <ol>
 * <li>paired rank (unsuited so hard code HEART)</li>
 * <li>kickers</li>
 * <li>suit of paired cards</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Four of a kind</strong>
 * <ol>
 * <li>paired rank (unsuited so hard code HEART)</li>
 * <li>kickers</li>
 * <li>suited paired cards</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Straight</strong>
 * <ol>
 * <li>Highest card in the straight</li>
 * <li>Second highest card</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Flush</strong>
 * <ol>
 * <li>Highest ranking card</li>
 * </ol>
 * </p>
 * <p/>
 * <p>
 * <strong>Full House</strong>
 * <ol>
 * <li>three of a kind rank (unsuited so hard code HEART)</li>
 * <li>pair rank (unsuited so hard code HEART)</li>
 * <li>kicker</li>
 * <li>three of a kind with suits</li>
 * </ol>
 * </p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class HandStrength extends HandInfo {

    private final HandType type;

    /**
     * The highest rank of the cards that forms this hand type
     */
    private Rank highestRank;

    /**
     * The second highest rank of the cards that forms this hand type
     */
    private Rank secondRank;

    /**
     * Ordered list of kicker cards, highest first.
     * If the hand type is HIGH_CARD then all cards
     * from the hand will be held here as well as
     * highest rank and second rank.
     */
    private List<Card> kickerCards = new ArrayList<Card>();

    /**
     * Groups of cards used to form the hand.
     * <p/>
     * E.g. for full house:
     * groups[0] = KS KD KH
     * groups[1] = 8H 8D
     */
    private List<Card>[] groups;

    /**
     * All cards used in this hand
     */
    private List<Card> cardsUsedInHand;

    /* ----------------------------------------------------
      *
      * 	CONSTRUCTORS
      *
      * ---------------------------------------------------- */

    public HandStrength(HandType type) {
        this.type = type;
    }

    public HandStrength(HandType handType, List<Card> cardsUsedInHand, List<Card>... groups) {
        this.type = handType;
        this.cardsUsedInHand = cardsUsedInHand;
        this.groups = groups;
    }

    /* ----------------------------------------------------
      *
      * 	PUBLIC METHODS
      *
      * ---------------------------------------------------- */

    @Override
    public String toString() {
        String groupString = "";
        if (groups != null) {
            for (List<Card> group : groups) {
                groupString += "Group:" + group;
            }
        }
        return "HandStrength type[" + type + "] highestRank[" + highestRank + "] secondRank[" + secondRank + "] kickers[" + kickerCards + "] cardsUsed[" + cardsUsedInHand + "] groups[" + groupString + "]";

    }

    @Override
    public HandType getHandType() {
        return type;
    }

    public Rank getHighestRank() {
        return highestRank;
    }

    public void setHighestRank(Rank highestRank) {
        this.highestRank = highestRank;
    }

    public Rank getSecondRank() {
        return secondRank;
    }

    public void setSecondRank(Rank secondRank) {
        this.secondRank = secondRank;
    }

    public List<Card> getKickerCards() {
        return kickerCards;
    }

    public void setKickerCards(List<Card> kickerCards) {
        this.kickerCards = kickerCards;
    }

    public List<Card> getCards() {
        return cardsUsedInHand;
    }

    @Override
    public HandStrengthCommon translate() {
        int groupSize = getGroupSize();
        ArrayList<List<GameCard>> lists = new ArrayList<List<GameCard>>();
        for(int i = 0; i < groupSize; i++)
        {
            lists.add(Card.translateCards(getGroup(i)));
        }
        return new HandStrengthCommon(getHandType().translate(), Card.translateCards(getCards()),
                (getHighestRank() == null) ? null : getHighestRank().translate(),
                (getSecondRank() == null) ? null : getSecondRank().translate(),
                Card.translateCards(getKickerCards()), lists);
    }

    public void setCardsUsedInHand(List<Card> cardsUsedInHand) {
        this.cardsUsedInHand = cardsUsedInHand;
    }

    /**
     * Get a copy of the list of cards contained in the groups with given index
     * Changes to the returned list will not be reflected in the list contained
     * in the groups.
     *
     * @param index
     * @return List of card
     */
    public List<Card> getGroup(int index) {
        return new ArrayList<Card>(groups[index]);
    }

    public void setGroups(List<Card>... groups) {
        this.groups = groups;
    }

    public int getGroupSize() {
        return groups!=null ? groups.length : 0;
    }
}