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

package com.cubeia.poker;

import com.cubeia.poker.states.PokerGameSTM;
import com.cubeia.poker.states.StateChanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * This class holds the current state and is responsible for changing the current state.
 *
 * It exists so that PokerState won't have to expose a public "changeState" method.
 */
public class StateHolder implements StateChanger, Serializable {

    private static final long serialVersionUID = 1L;

    private PokerGameSTM currentState;

    private static final Logger log = LoggerFactory.getLogger(StateHolder.class);

    @Override
    public void changeState(PokerGameSTM newState) {
        if (newState == null) throw new IllegalArgumentException("New state is null");
        if (currentState != null) {
            currentState.exitState();
        }
        currentState = newState;
        currentState.enterState();
    }

    PokerGameSTM get() {
        return currentState;
    }

}
