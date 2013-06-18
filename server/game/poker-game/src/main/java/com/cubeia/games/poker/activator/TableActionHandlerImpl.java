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

package com.cubeia.games.poker.activator;

import org.apache.log4j.Logger;

import com.cubeia.backend.cashgame.dto.CloseTableRequest;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TableActionHandlerImpl implements TableActionHandler {

    @Inject
    private TableFactory tables;

    @Inject
    private ParticipantFactory participants;

    @Log4j
    private Logger log;

    @Inject
    private ActivatorRouter router;

    @Inject
    private TableNameManager tableNamer;

    @Override
    public void handleAction(TableModifierAction action) {
        switch (action.getType()) {
            case CLOSE: {
                doClose(action.getTableId());
                break;
            }
            case CREATE: {
                doCreate(action.getTemplate());
                break;
            }
            case DESTROY: {
                doDestroy(action.getTableId());
                break;
            }
        }
    }


    // --- PRIVATE METHODS --- //

    private void doCreate(TableConfigTemplate template) {
        tables.createTable(template.getSeats(), participants.createParticipantFor(template));
    }

    private void doDestroy(int tableId) {
        tables.destroyTable(tableId, true);
        tableNamer.tableDestroyed(tableId);
    }

    private void doClose(int tableId) {
        log.debug("Table[" + tableId + "] is eligible for closure, sending close request.");
        GameObjectAction action = new GameObjectAction(tableId);
        action.setAttachment(new CloseTableRequest(false));
        router.dispatchToGame(tableId, action);
    }
}
