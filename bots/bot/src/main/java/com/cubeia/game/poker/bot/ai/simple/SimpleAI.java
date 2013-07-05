package com.cubeia.game.poker.bot.ai.simple;

import com.cubeia.firebase.bot.ai.AbstractAI;
import com.cubeia.game.poker.bot.ai.GameState;
import com.cubeia.game.poker.bot.ai.PokerAI;
import com.cubeia.games.poker.io.protocol.Enums.ActionType;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.PlayerAction;
import com.cubeia.games.poker.io.protocol.RequestAction;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.cubeia.game.poker.bot.ai.simple.SimpleAI.Strategy.STRONG;
import static com.cubeia.game.poker.bot.ai.simple.SimpleAI.Strategy.WEAK_BLUFF;
import static com.cubeia.games.poker.io.protocol.Enums.ActionType.BET;
import static com.cubeia.games.poker.io.protocol.Enums.ActionType.CALL;
import static com.cubeia.games.poker.io.protocol.Enums.ActionType.CHECK;
import static com.cubeia.games.poker.io.protocol.Enums.ActionType.FOLD;
import static com.cubeia.games.poker.io.protocol.Enums.ActionType.RAISE;

public class SimpleAI implements PokerAI {

    private AbstractAI bot;

    private static Random rng = new Random();

    private TexasHoldemHandCalculator handCalculator = new TexasHoldemHandCalculator();

    private StrengthCalculator strengthCalculator = new StrengthCalculator();

    /** Percent chance that the bot will bluff or act out of hand strength */
    private int bluffProbability = 10;

    enum Strategy {
        WEAK,
        WEAK_BLUFF,
        NEUTRAL,
        STRONG
    }

    public SimpleAI() {
        bluffProbability = bluffProbability + rng.nextInt(25);
    }

    @Override
    public void setBot(AbstractAI bot) {
        this.bot = bot;
    }

    @Override
    public PerformAction onActionRequest(RequestAction request, GameState state) {
        HandStrength handStrength = getHandStrength(state);

        PerformAction response = new PerformAction();
        response.seq = request.seq;
        response.player = bot.getBot().getPid();

        PlayerAction playerAction = null;

        // Always post blinds
        for (PlayerAction action : request.allowedActions) {
            switch (action.type) {
                case BIG_BLIND:
                case SMALL_BLIND:
                case ANTE:
                case ENTRY_BET:
                    playerAction = action;
                    break;
                default:
            }
        }

        if (playerAction != null) {
            // Blind or Ante
            response.action = playerAction;
            return response;
        }

        // We need to act
        Strategy strategy = strengthCalculator.getStrategy(request, state, handStrength);

        boolean bluff = false;
        if (doBluff()) {
            bluff = true;
            switch (strategy) {
                case WEAK:
                    strategy = STRONG;
                    break;
                case NEUTRAL:
                    strategy = STRONG;
                    break;
                case STRONG:
                    strategy = WEAK_BLUFF;
                    break;
            }
        }

        BigDecimal betAmount = BigDecimal.ZERO;

        if (strategy == Strategy.WEAK) {
            if (hasPlayerAction(CHECK, request)) {
                playerAction = new PlayerAction(CHECK, "0", "0");
            } else {
                playerAction = new PlayerAction(FOLD, "0", "0");
            }
        }

        if (strategy == Strategy.WEAK_BLUFF) {
            if (hasPlayerAction(CHECK, request)) {
                playerAction = new PlayerAction(CHECK, "0", "0");

            } else if (hasPlayerAction(CALL, request)) {
                playerAction = getPlayerAction(CALL, request);

            } else {
                playerAction = getPlayerAction(FOLD, request);

            }
        }

        if (strategy == Strategy.NEUTRAL) {
            if (hasPlayerAction(CALL, request)) {
                playerAction = getPlayerAction(CALL, request);
                betAmount = calculateBet(playerAction, request.currentPotSize, strategy);

            } else if (hasPlayerAction(BET, request)) {
                playerAction = getPlayerAction(BET, request);
                betAmount = calculateBet(playerAction, request.currentPotSize, strategy);

            } else if (hasPlayerAction(CHECK, request)) {
                playerAction = getPlayerAction(CHECK, request);

            } else {
                playerAction = getPlayerAction(FOLD, request);

            }
        }

        if (strategy == Strategy.STRONG) {
            if (hasPlayerAction(RAISE, request)) {
                playerAction = getPlayerAction(RAISE, request);
                betAmount = calculateBet(playerAction, request.currentPotSize, strategy);

            } else if (hasPlayerAction(BET, request)) {
                playerAction = getPlayerAction(BET, request);
                betAmount = calculateBet(playerAction, request.currentPotSize, strategy);

            } else if (hasPlayerAction(CALL, request)) {
                playerAction = getPlayerAction(CALL, request);
                betAmount = calculateBet(playerAction, request.currentPotSize, strategy);

            } else if (hasPlayerAction(CHECK, request)) {
                playerAction = getPlayerAction(CHECK, request);

            } else {
                playerAction = getPlayerAction(FOLD, request);

            }
        }

        if (playerAction == null) {
            bot.getBot().logWarn("Player Action was not set! Allowed Actions: " + request.allowedActions);
            playerAction = request.allowedActions.get(0);
            bot.getBot().logInfo("Simple AI. Hand Strength: " + handStrength + ", State: " + state + ", PA: " + playerAction);
        }


        response.action = playerAction;
        response.betAmount = betAmount.toPlainString();

        HandType handType = handStrength.getHandType();
        Rank highestRank = handStrength.getHighestRank();
        ActionType playerActionType = playerAction.type;

        if (!bluff) {
            bot.getBot().logInfo("Simple AI. I got " + handType + " " + highestRank + " on the " + state
                    .getPhase() + ". I am feeling " + strategy + ". I will " + playerActionType + ", with bet amout " + betAmount);
        } else {
            bot.getBot().logInfo("Simple AI. I got " + handType + " " + highestRank + " on the " + state
                    .getPhase() + ". I am bluffing as " + strategy + ". I will " + playerActionType + ", with bet amout " + betAmount);
        }

        return response;
    }

    private BigDecimal calculateBet(PlayerAction playerAction, String currentPotSize, Strategy strategy) {
        BigDecimal minAmount = new BigDecimal(playerAction.minAmount);
        BigDecimal maxAmount = new BigDecimal(playerAction.minAmount);
        BigDecimal pot = new BigDecimal(currentPotSize);

        BigDecimal betAmount;

        if (strategy == STRONG) {
            int potMultiplier = rng.nextInt(2) + 1;
            betAmount = pot.multiply(new BigDecimal(potMultiplier));
        } else {
            int minBetMultiplier = rng.nextInt(2) + 1;
            betAmount = pot.multiply(new BigDecimal(minBetMultiplier));
        }

        // Adjust if outside boundaries.
        if (betAmount.compareTo(minAmount) < 0) {
            betAmount = minAmount;
        } else if (betAmount.compareTo(maxAmount) > 0) {
            betAmount = maxAmount;
        }

        return betAmount;
    }


    private boolean doBluff() {
        return rng.nextInt(100) < bluffProbability;
    }

    private PlayerAction getPlayerAction(ActionType type, RequestAction request) {
        for (PlayerAction action : request.allowedActions) {
            if (action.type == type) {
                return action;
            }
        }
        return null;
    }

    private boolean hasPlayerAction(ActionType type, RequestAction request) {
        return getPlayerAction(type, request) != null;
    }

    public HandStrength getHandStrength(GameState state) {
        List<Card> cards = new ArrayList<Card>(state.getPrivateCards());
        cards.addAll(state.getCommunityCards());
        Hand hand = new Hand(cards);
        return handCalculator.getHandStrength(hand);
    }
}
