package com.cubeia.game.poker.bot.ai;

import com.cubeia.firebase.bot.ai.AbstractAI;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.RequestAction;

/**
 * Implementations must have a default constructor.
 *
 * @author Fredrik
 */
public interface PokerAI {

    /**
     * Will be called straight after the constructor call.
     *
     * @param bot
     */
    void setBot(AbstractAI bot);

    PerformAction onActionRequest(RequestAction request, GameState state);

}
