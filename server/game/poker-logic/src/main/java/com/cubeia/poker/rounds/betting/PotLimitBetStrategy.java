package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.player.PokerPlayer;

import java.math.BigDecimal;

public class PotLimitBetStrategy extends NoLimitBetStrategy {


    public PotLimitBetStrategy(BigDecimal minBet) {
        super(minBet);
    }

    @Override
    public BetStrategyType getType() {
        return BetStrategyType.FIXED_LIMIT;
    }

    @Override
    public BigDecimal getMaxBetAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        return bettingRoundContext.getPotSize().min(player.getBalance());
    }

    @Override
    public BigDecimal getMaxRaiseToAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        BigDecimal maxRaiseToAmount = super.getMaxRaiseToAmount(bettingRoundContext,player);
        BigDecimal potSizeAfterCall = getCallAmount(bettingRoundContext, player).add(bettingRoundContext.getPotSize());
        BigDecimal potLimitedRaiseToAmount = bettingRoundContext.getHighestBet().add(potSizeAfterCall);
        return potLimitedRaiseToAmount.min(maxRaiseToAmount);
    }

}
