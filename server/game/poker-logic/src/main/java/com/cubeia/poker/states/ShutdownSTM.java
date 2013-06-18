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

package com.cubeia.poker.states;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.variant.GameType;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownSTM extends AbstractPokerGameSTM {

    private static Logger log = LoggerFactory.getLogger(ShutdownSTM.class);

    private static final long serialVersionUID = 7076228045164551068L;

    public ShutdownSTM() {
    }

    @VisibleForTesting
    public ShutdownSTM(GameType gameType, PokerContext context, ServerAdapterHolder serverAdapterHolder, StateChanger stateChanger) {
        super(gameType, context, serverAdapterHolder, stateChanger);
    }

    public String toString() {
        return "ShutdownState";
    }

    @Override
    public boolean act(PokerAction action) {
        log.warn("table {} is shut down, dropping incoming action: {}", context.getTableId(), action);
        return false;
    }

    @Override
    public void timeout() {
        log.warn("table {} is shut down, dropping incoming timeout", context.getTableId());
    }

}
