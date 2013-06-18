package com.cubeia.poker.rounds.betting;


import com.cubeia.poker.player.PokerPlayer;
import junit.framework.TestCase;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PotLimitBetStrategyTest extends TestCase {

    private PotLimitBetStrategy strategy;

    private BettingRoundContext context;

    private PokerPlayer player;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = mock(BettingRoundContext.class);
        player = mock(PokerPlayer.class);
        strategy = new PotLimitBetStrategy(bd(50));
    }

    public void testGetMinAmount() {
        when(player.getBalance()).thenReturn(bd(100));
        BigDecimal minAmount = strategy.getMinBetAmount(context,player);
        assertEquals(new BigDecimal(50),minAmount);
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    public void testGetMaxAmount() {
        when(player.getBalance()).thenReturn(bd(100));
        when(context.getPotSize()).thenReturn(bd(80));
        BigDecimal maxAmount = strategy.getMaxBetAmount(context,player);
        assertEquals(bd(80),maxAmount);
    }

    public void testGetMaxAmountPotGreaterThanStack() {
        when(player.getBalance()).thenReturn(bd(100));
        when(context.getPotSize()).thenReturn(bd(200));
        BigDecimal maxAmount = strategy.getMaxBetAmount(context,player);
        assertEquals(bd(100),maxAmount);
    }

    public void testGetMaxRaiseToAmount() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(10));
        when(context.getHighestBet()).thenReturn(bd(20));
        when(context.getPotSize()).thenReturn(bd(30));
        assertEquals(bd(60),strategy.getMaxRaiseToAmount(context,player));

        when(player.getBalance()).thenReturn(bd(1000));
        when(player.getBetStack()).thenReturn(bd(4));
        when(context.getHighestBet()).thenReturn(bd(8));
        when(context.getPotSize()).thenReturn(bd(16));
        assertEquals(bd(28), strategy.getMaxRaiseToAmount(context,player));

    }

    //10 + 10
    //20 + 40 + ?
    public void testGetMaxRaiseToAmountAllIn() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(20));
        when(context.getHighestBet()).thenReturn(bd(40));
        when(context.getPotSize()).thenReturn(bd(80));
        assertEquals(bd(120),strategy.getMaxRaiseToAmount(context,player));
    }
    //pot 10 pre-flop
    //balance 40 bet 20
    //raise to 40
    //balance 20 bet stack 20 max raise = 0
    public void testGetMaxRaiseWhenNoMoneyToRaiseWith() {
        when(player.getBalance()).thenReturn(bd(20));
        when(player.getBetStack()).thenReturn(bd(20));
        when(context.getHighestBet()).thenReturn(bd(40));
        when(context.getPotSize()).thenReturn(bd(70));
        assertEquals(bd(0),strategy.getMaxRaiseToAmount(context,player));
    }

}
