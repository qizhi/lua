/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.game.handler;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.PlayerInfoAction;
import com.cubeia.firebase.api.action.TableQueryRequestAction;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.mock.MockDetailsProvider;
import com.cubeia.firebase.mock.MockNotifier;
import com.cubeia.firebase.mock.MockTable;

public class TableQueryRequestTest extends TestCase {

	StandardTableActionHandler actionHandler;
	MockNotifier mockNotifier;
	MockTable mockTable;
	
	@Override
	protected void setUp() throws Exception {
		mockTable = new MockTable();
		mockTable.extendedDetailsProvider = new MockDetailsProvider();
		mockNotifier = new MockNotifier();
		mockTable.notifier = mockNotifier;
		actionHandler = new StandardTableActionHandler(mockTable, null, null);		
	}

	public void testVisitTableQueryRequestAction() {
		TableQueryRequestAction t = new TableQueryRequestAction(1, 1);
		actionHandler.visit(t);
		assertEquals(TableQueryResponseAction.class, mockNotifier.actions.get(0).getClass());
	}
	
	public void testQueryRequestActionWithDetailedInfo() {
		mockTable.getPlayerSet().addPlayer(createTestPlayer(1), 0);
		mockTable.extendedDetailsProvider.details.put(1, createMockParams());
		TableQueryRequestAction t = new TableQueryRequestAction(1, 1);
		actionHandler.visit(t);
		TableQueryResponseAction r = (TableQueryResponseAction) mockNotifier.actions.get(0);
		PlayerInfoAction p = r.getSeatInfos().iterator().next().getPlayerInfo();
		assertEquals(2, p.getDetails().size());
	}
	
	private List<Param> createMockParams() {
		List<Param> params = new ArrayList<Param>();
		params.add(ParameterUtil.createParam("balance", 1000));
		params.add(ParameterUtil.createParam("location", "testtown"));
		return params;
	}

	private GenericPlayer createTestPlayer(int playerId) {
		GenericPlayer p = new GenericPlayer(playerId, "test" + playerId);
		return p;
	}
}
