package com.cubeia.poker.handhistory.api;

import java.util.List;

public class HandStrengthCommon extends HandInfoCommon {

    private static final long serialVersionUID = 2078626142291415227L;

    private BestHandType handType;

    /**
     * The highest rank of the cards that forms this hand type
     */
    private GameCard.Rank highestRank;

    /**
     * The second highest rank of the cards that forms this hand type
     */
    private GameCard.Rank secondRank;

    /**
     * Ordered list of kicker cards, highest first.
     * If the hand type is HIGH_CARD then all cards
     * from the hand will be held here as well as
     * highest rank and second rank.
     */
    private List<GameCard> kickerCards;

    /**
     * Groups of cards used to form the hand.
     * <p/>
     * E.g. for full house:
     * groups[0] = KS KD KH
     * groups[1] = 8H 8D
     */
    private List<List<GameCard>> groups;

    /**
     * All cards used in this hand
     */
    private List<GameCard> cardsUsedInHand;

    public HandStrengthCommon() {
    }

    public HandStrengthCommon(BestHandType handType, List<GameCard> cardsUsedInHand, GameCard.Rank highestRank,
                              GameCard.Rank secondRank, List<GameCard> kickerCards, List<List<GameCard>> groups) {
        this.handType = handType;
        this.cardsUsedInHand = cardsUsedInHand;
        this.highestRank = highestRank;
        this.secondRank = secondRank;
        this.kickerCards = kickerCards;
        this.groups = groups;
    }

    public BestHandType getHandType() {
        return handType;
    }

    public void setHandType(BestHandType handType) {
        this.handType = handType;
    }

    public GameCard.Rank getHighestRank() {
        return highestRank;
    }

    public void setHighestRank(GameCard.Rank highestRank) {
        this.highestRank = highestRank;
    }

    public GameCard.Rank getSecondRank() {
        return secondRank;
    }

    public void setSecondRank(GameCard.Rank secondRank) {
        this.secondRank = secondRank;
    }

    public List<GameCard> getKickerCards() {
        return kickerCards;
    }

    public void setKickerCards(List<GameCard> kickerCards) {
        this.kickerCards = kickerCards;
    }

    public List<List<GameCard>> getGroups() {
        return groups;
    }

    public void setGroups(List<List<GameCard>> groups) {
        this.groups = groups;
    }

    public List<GameCard> getCardsUsedInHand() {
        return cardsUsedInHand;
    }

    public void setCardsUsedInHand(List<GameCard> cardsUsedInHand) {
        this.cardsUsedInHand = cardsUsedInHand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HandStrengthCommon that = (HandStrengthCommon) o;

        if (cardsUsedInHand != null ? !cardsUsedInHand.equals(that.cardsUsedInHand) : that.cardsUsedInHand != null)
            return false;
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
        if (handType != that.handType) return false;
        if (highestRank != that.highestRank) return false;
        if (kickerCards != null ? !kickerCards.equals(that.kickerCards) : that.kickerCards != null) return false;
        if (secondRank != that.secondRank) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = handType != null ? handType.hashCode() : 0;
        result = 31 * result + (highestRank != null ? highestRank.hashCode() : 0);
        result = 31 * result + (secondRank != null ? secondRank.hashCode() : 0);
        result = 31 * result + (kickerCards != null ? kickerCards.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (cardsUsedInHand != null ? cardsUsedInHand.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HandStrengthCommon{" +
                "handType=" + handType +
                ", highestRank=" + highestRank +
                ", secondRank=" + secondRank +
                ", kickerCards=" + kickerCards +
                ", groups=" + groups +
                ", cardsUsedInHand=" + cardsUsedInHand +
                '}';
    }
}