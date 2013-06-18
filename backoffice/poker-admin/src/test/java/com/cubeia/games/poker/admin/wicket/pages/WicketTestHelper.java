/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.pages;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.network.NetworkClient;
import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.handhistory.api.Results;
import com.cubeia.poker.handhistory.api.Table;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;

public class WicketTestHelper {

    public static WicketTester createWicketTester(AdminDAO adminDAO) {
        return createWicketTester(mock(HistoryService.class), adminDAO, mock(NetworkClient.class));
    }

    public static WicketTester createWicketTester(AdminDAO adminDAO, NetworkClient networkClient) {
        return createWicketTester(mock(HistoryService.class), adminDAO, networkClient);
    }

    public static WicketTester createWicketTester(HistoryService historyService) {
        return createWicketTester(historyService, mock(AdminDAO.class), mock(NetworkClient.class));
    }

    public static WicketTester createWicketTester(HistoryService historyService, AdminDAO adminDAO, NetworkClient networkClient) {
        ApplicationContextMock context = new ApplicationContextMock();
        context.putBean("historyService", historyService);
        context.putBean("adminDAO", adminDAO);
        context.putBean("networkClient", networkClient);
        WicketTester tester = new WicketTester();
        tester.getApplication().getComponentInstantiationListeners().add(new SpringComponentInjector(tester.getApplication(), context));
        return tester;
    }

    public static HistoricHand createMockHand() {
            HistoricHand hand = new HistoricHand("handId");
            Table table = new Table();
            table.setTableId(1);
            table.setTableIntegrationId("integrationId1");
            table.setTableName("Table name");
            hand.setTable(table);
            Results results = new Results();
            results.setTotalRake(new BigDecimal(7));
            hand.setResults(results);
            return hand;
        }

}
