package com.cubeia.game.poker.bot.ai.random;

import com.cubeia.firebase.bot.ai.AbstractAI;
import com.cubeia.game.poker.bot.Strategy;
import com.cubeia.game.poker.bot.ai.GameState;
import com.cubeia.game.poker.bot.ai.PokerAI;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.PlayerAction;
import com.cubeia.games.poker.io.protocol.RequestAction;

import java.math.BigDecimal;
import java.util.Random;

public class RandomAI implements PokerAI {

    private static Random rng = new Random();

    private final Strategy strategy = new Strategy();

    private AbstractAI bot;

    public RandomAI() {
    }

    @Override
    public void setBot(AbstractAI bot) {
        this.bot = bot;
    }

    @Override
    public PerformAction onActionRequest(RequestAction request, GameState state) {
        PerformAction response = new PerformAction();
        response.seq = request.seq;
        response.player = bot.getBot().getPid();

        PlayerAction playerAction = strategy.getAction(request.allowedActions);
        response.action = playerAction;
        BigDecimal betAmount = getRandomBetAmount(playerAction);
        response.betAmount = betAmount.toPlainString();

        // Sanity check
        if (betAmount.compareTo(new BigDecimal(playerAction.maxAmount)) > 0) {
            bot.getBot().logWarn("I am betting too much. Max[" + playerAction.maxAmount + "] myBet[" + response.betAmount + "]");
        }

        return response;
    }

    private BigDecimal getRandomBetAmount(PlayerAction playerAction) {
        BigDecimal maxAmount = new BigDecimal(playerAction.maxAmount);
        if (maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal minAmount = new BigDecimal(playerAction.minAmount);

        //return playerAction.minAmount;

        // 90% chance of min bet
        if (rng.nextInt(100) < 90) {
            return minAmount;
        }

        // 1% chance of all in
        if (rng.nextInt(100) < 1) {
            return maxAmount;
        }

        int maxLevel = 5;
        // Randomize how many min amount bets we will bet
        int bets = 1 + rng.nextInt(maxLevel);
        BigDecimal betThis = minAmount.multiply(new BigDecimal(bets));
        BigDecimal cappedBet = betThis.min(maxAmount);

        if (cappedBet.compareTo(minAmount) < 0) {
            cappedBet = minAmount;
        }
        return cappedBet;
    }

}
