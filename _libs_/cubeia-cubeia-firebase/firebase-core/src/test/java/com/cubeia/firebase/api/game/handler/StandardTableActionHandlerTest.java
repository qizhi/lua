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

import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.game.rule.DefaultSeatingRules;
import com.cubeia.firebase.api.game.rule.SeatingRules;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.mock.MockNotifier;
import com.cubeia.firebase.mock.MockStandardTableActionHandler;
import com.cubeia.firebase.mock.MockTableInterceptor;
import com.cubeia.firebase.mock.TableCreator;

/**
 * Tests the handling of seating and such in the StandardTableActionHandler.
 * We will send in actions for execution and examine the returned actions
 * through a mocked notifier.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class StandardTableActionHandlerTest extends TestCase {

	private MockStandardTableActionHandler handler;
	private MockNotifier notifier;
	private FirebaseTable table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		table = TableCreator.createTable(1, 10);
		handler = new MockStandardTableActionHandler(table, null, null);
		SeatingRules rules = new DefaultSeatingRules();
		handler.setSeatingRules(rules);
		notifier = (MockNotifier)handler.getNotifier();
		notifier.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		table = null;
		notifier = null;
	}
	
	/**
	 * Since we are first to join we should get three actions:
	 * 
	 * 	 - JoinResponse: playerId: 22 table: 1 seatId: 0 status: 0 code: -1
	 *   - SeatInfoAction - sid[0] pi[PlayerInfoAction - pid[22] nick[apan]] ps[CONNECTED]
	 *   - JoinRequest: playerId: 22 table: 1 seatId: 0 nick: apan
	 *   
	 *   
	 *
	 */
	public void testVisitJoinRequestAction() {
		JoinRequestAction request = new JoinRequestAction(22, 1, 0, "apan");
		request.visit(handler);
		
		List<GameAction> actions = (notifier).actions;
		
		boolean responseFound = false;
		boolean seatInfoFound = false;
		boolean joinFound = false;
		
		for (GameAction action : actions) {
			if (action instanceof JoinResponseAction) { responseFound = true; }
			if (action instanceof SeatInfoAction) { seatInfoFound = true; }
			if (action instanceof JoinRequestAction) { joinFound = true; }
		}
		
		assertEquals(3, actions.size());
		
		assertTrue(responseFound);
		assertTrue(seatInfoFound);
		assertTrue(joinFound);
		
		
		/* 
		 * Try and join another player in the same seat -> should fail
		 * In this case we should only receive one action:
		 * 
		 *  - Action : JoinResponse: playerId: 33 table: 1 seatId: 0 status: 2 code: -1
		 *  
		 * Status 2 = Denied
		 */
		notifier.clear();
		JoinRequestAction request2 = new JoinRequestAction(33, 1, 0, "banan");
		request2.visit(handler);
		
		List<GameAction> actions2 = (notifier).actions;
		assertEquals(1, actions2.size());
		for (GameAction action : actions2) {
			if (action instanceof JoinResponseAction) {
				JoinResponseAction response = (JoinResponseAction) action;
				assertEquals(2, response.getStatus());
			} else { 
				fail("Expected a JoinResponseAction but got a: "+action);
			}
		}
	}
	
	/**
	 * Set an interceptor that will not allow players to leave
	 */
	public void testDeniedJoin() {
		table.setInterceptor(new MockTableInterceptor(false, false));
		JoinRequestAction request = new JoinRequestAction(22, 1, 0, "apan");
		request.visit(handler);
		List<GameAction> actions = (notifier).actions;
		assertEquals(1, actions.size());
		for (GameAction action : actions) {
			if (action instanceof JoinResponseAction) {
				JoinResponseAction response = (JoinResponseAction) action;
				assertEquals(2, response.getStatus());
			} else { 
				fail("Expected a JoinResponseAction but got a: "+action);
			}
		}
	}
	
	
	/**
	 * Leave a table. We should get two actions from the notifier:
	 * 
	 *  - Leave Action pid[22] tableid[1]
	 *  - Leave Response Action pid[22] tableid[1] code[-1]
	 *
	 */
	public void testVisitLeaveAction() {
		// Seat a player
		JoinRequestAction request = new JoinRequestAction(22, 1, 0, "apan");
		request.visit(handler);
		
		notifier.clear();
		
		// Try to leave
		LeaveAction leave = new LeaveAction(22, 1);
		leave.visit(handler);
		
		List<GameAction> actions = (notifier).actions;
		boolean leaveFound = false;
		boolean standFound = false;
		assertEquals(2, actions.size());
		for (GameAction action : actions) {
			if (action instanceof LeaveAction) { leaveFound = true; }
			if (action instanceof LeaveResponseAction) { standFound = true; }
		}
		assertTrue(leaveFound);
		assertTrue(standFound);
		
		
		/*
		 * Set an interceptor that will not allow players to join but not leave
		 */
		table.setInterceptor(new MockTableInterceptor(true, false));
		request.visit(handler); // Join
		notifier.clear();
		leave.visit(handler); // Leave
		List<GameAction> actions2 = (notifier).actions;
		assertEquals(1, actions2.size());
		for (GameAction action : actions2) {
			if (action instanceof LeaveResponseAction) {
				LeaveResponseAction response = (LeaveResponseAction) action;
				assertEquals(2, response.getStatus());
			} else { 
				fail("Expected a LeaveResponseAction but got a: "+action);
			}
		}
	}

	
	
	public void testVisitSeatPlayersMttAction() {
		SeatPlayersMttAction request = new SeatPlayersMttAction(12, 1);
		request.visit(handler);
		
		assertEquals(0, notifier.actions.size());
	}

}
