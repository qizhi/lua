package com.cubeia.poker.rounds.betting;

public enum BettingRoundName {
    NOTHING(false), PRE_FLOP(false), FLOP(false), TURN(true), RIVER(true);

    private boolean doubleBetRound;

    BettingRoundName(boolean doubleBetRound) {
        this.doubleBetRound = doubleBetRound;
    }

    public BettingRoundName next() {
        if (ordinal() + 1 == BettingRoundName.values().length) {
            throw new IllegalArgumentException("Cannot get round after " + this);
        }
        return BettingRoundName.values()[ordinal() + 1];
    }

    public boolean isDoubleBetRound() {
        return doubleBetRound;
    }
}
