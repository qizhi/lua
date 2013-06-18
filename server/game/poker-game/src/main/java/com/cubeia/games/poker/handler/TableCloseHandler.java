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

package com.cubeia.games.poker.handler;

import com.cubeia.firebase.api.action.AbstractGameAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.games.poker.TableCloseHandlerImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(TableCloseHandlerImpl.class)
public interface TableCloseHandler {

    /**
     * @param table Table to close, must not be null
     * @param force True to close even if players are sitting, false to abort if players are seated
     */
    public abstract void closeTable(Table table, boolean force);

    public abstract void tableCrashed(Table table);

    public abstract void handleUnexpectedExceptionOnTable(
            AbstractGameAction action, Table table, Throwable throwable);

}