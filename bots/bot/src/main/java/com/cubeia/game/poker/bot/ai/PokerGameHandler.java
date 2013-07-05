package com.cubeia.game.poker.bot.ai;

import com.cubeia.firebase.bot.action.Action;
import com.cubeia.firebase.bot.ai.AbstractAI;
import com.cubeia.game.poker.bot.AiProvider;
import com.cubeia.game.poker.bot.ai.random.RandomAI;
import com.cubeia.games.poker.io.protocol.GameCard;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.RequestAction;
import com.cubeia.poker.hand.Card;

public class PokerGameHandler {

    private GameState state = new GameState();

    private AbstractAI bot;

    private PokerAI ai;

    public PokerGameHandler(AbstractAI bot) {
        this.bot = bot;

    }

    @SuppressWarnings("unchecked")
    private void initAi(AbstractAI bot) {
        AiProvider provider = (AiProvider) bot;
        String aiClass = provider.getPokerAi();
        try {
            Class<PokerAI> forName = (Class<PokerAI>) Class.forName(aiClass);
            ai = forName.newInstance();
        } catch (Exception e) {
            bot.getBot().logWarn("Could not create AI class: " + aiClass + ". Will use Random AI instead. Error: " + e);
            ai = new RandomAI();
        }

        ai.setBot(bot);
    }

    /*----------------------------------------------
     *
     * GAME LOGIC METHODS
     *
     *----------------------------------------------*/

    public Action onActionRequest(final RequestAction request) {
        if (ai == null) {
            initAi(bot);
        }

        final PerformAction response = ai.onActionRequest(request, state);

        Action action = new Action(bot.getBot()) {
            public void run() {
                try {
                    bot.getBot().sendGameData(bot.getTable().getId(), bot.getBot().getPid(), response);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        };

        return action;
    }



    /*----------------------------------------------
     *
     * TABLE STATE METHODS
     *
     *----------------------------------------------*/

    public GameState getState() {
        return state;
    }

    public void clear() {
        bot.getBot().logDebug("Hand End - Clear poker table state");
        state.clear();
    }

    public void addPrivateCard(GameCard card) {
        state.addPrivateCard(convertGameCard(card));
    }

    public void addCommunityCard(GameCard card) {
        state.addCommunityCard(convertGameCard(card));
    }

    private Card convertGameCard(GameCard c) {
        return new Card(c.cardId, com.cubeia.poker.hand.Rank.values()[c.rank.ordinal()], com.cubeia.poker.hand.Suit.values()[c.suit.ordinal()]);
    }

}
