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

package com.cubeia.backend.firebase.jmx;

import com.cubeia.backend.firebase.CashGamesBackendMock;
import com.cubeia.firebase.api.action.GameObjectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class MockController implements MockControllerMBean {

    private static final String JMX_BIND_NAME = "com.cubeia.poker.backend:type=MockController";

    private Logger log = LoggerFactory.getLogger(MockController.class);

    private final CashGamesBackendMock cashGamesBackendMock;

    private final String CLOSE_TABLE_MSG = "CLOSE_TABLE";
    private final String CLOSE_TABLE_HINT_MSG = "CLOSE_TABLE_HINT";

    public MockController(CashGamesBackendMock cashGamesBackendMock) {
        this.cashGamesBackendMock = cashGamesBackendMock;
        initJmx();
    }

    ;

    @Override
    public void closeTableByGameIdAndTableId(int gameId, int tableId) {
        sendGameObjectActionToTable(gameId, tableId, CLOSE_TABLE_MSG);
    }

    @Override
    public void closeTableHintByGameIdAndTableId(int gameId, int tableId) {
        sendGameObjectActionToTable(gameId, tableId, CLOSE_TABLE_HINT_MSG);
    }

    private void sendGameObjectActionToTable(int gameId, int tableId, Object object) {
        log.debug("sending object action: game = {}, table = {}, action = {}",
                new Object[]{gameId, tableId, object});
        GameObjectAction action = new GameObjectAction(tableId);
        action.setAttachment(object);
        cashGamesBackendMock.getRouter().dispatchToGame(gameId, action);
    }

    private void initJmx() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(JMX_BIND_NAME);
            mbs.registerMBean(this, objectName);
        } catch (Exception e) {
            log.error("failed to start mbean server", e);
        }
    }


}