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

package com.cubeia.game.poker.bot;

import com.cubeia.games.poker.io.protocol.Enums;
import com.cubeia.games.poker.io.protocol.PlayerAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Strategy {

    private Random rng = new Random();

    public PlayerAction getAction(List<PlayerAction> allowedActions) {

        // Always post blinds
        for (PlayerAction action : allowedActions) {
            switch (action.type) {
                case BIG_BLIND:
                    return action;

                case SMALL_BLIND:
                    return action;

                case ANTE:
                    return action;

                default:
            }
        }

        // defensive copy
        allowedActions = new ArrayList<PlayerAction>(allowedActions);

        // first sanity check: never fold when you can check
        if (canCheck(allowedActions)) {
            removeFold(allowedActions);
        }

        int optionCount = allowedActions.size();
        int optionIndex = rng.nextInt(optionCount);
        PlayerAction playerAction = allowedActions.get(optionIndex);

        /*
        if (playerAction.type == ActionType.FOLD) {
            // We need to downplay fold
            if (rng.nextBoolean()) return getAction(allowedActions);

        }
        */

        return playerAction;
    }


    private void removeFold(List<PlayerAction> allowedActions) {
        for (Iterator<PlayerAction> it = allowedActions.iterator(); it.hasNext(); ) {
            PlayerAction a = it.next();
            if (a.type == Enums.ActionType.FOLD) {
                it.remove();
                break;
            }
        }
    }


    private boolean canCheck(List<PlayerAction> allowedActions) {
        for (PlayerAction p : allowedActions) {
            if (p.type == Enums.ActionType.CHECK) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param allowedActions
     * @return true if the returned action should use an arbitrary delay.
     */
    public boolean useDelay(List<PlayerAction> allowedActions) {
        for (PlayerAction action : allowedActions) {
            switch (action.type) {
                case BIG_BLIND:
                    return false;

                case SMALL_BLIND:
                    return false;

                case ANTE:
                    return false;

                default:
            }
        }
        return true;
    }


}
