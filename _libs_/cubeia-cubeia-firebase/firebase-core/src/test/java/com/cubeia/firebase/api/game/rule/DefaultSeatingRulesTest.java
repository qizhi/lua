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
package com.cubeia.firebase.api.game.rule;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.trans.TransactionalTableFactory;

public class DefaultSeatingRulesTest extends TestCase {

	private TransactionalTableFactory fact = new TransactionalTableFactory(null, 10240);
	
	public void testActionAllowedJoinActionTable() {
		FirebaseTable table = createTable(1, 10);
		DefaultSeatingRules rules = new DefaultSeatingRules();
		JoinRequestAction action = new JoinRequestAction(11, 1, 1, "testNick");
		
		assertTrue(rules.actionAllowed(action, table));
		
		JoinRequestAction wrongAction = new JoinRequestAction(11, 1, 10, "testNick");
		assertFalse(rules.actionAllowed(wrongAction, table));
		
		// Seat APAN on seat 1
		GenericPlayer player = new GenericPlayer(2, "apa");
        table.getPlayerSet().addPlayer(player, 1);
        table.commit();
		
        assertFalse(rules.actionAllowed(action, table));
        
        // Try to seat same player twice (on different seat)
        JoinRequestAction apan2Action = new JoinRequestAction(2, 1, 3, "apa");
        assertFalse(rules.actionAllowed(apan2Action, table));
        
        // Try to seat same player twice (on same seat)
        JoinRequestAction apan3Action = new JoinRequestAction(2, 1, 1, "apa");
        assertTrue(rules.actionAllowed(apan3Action, table));
        
        // Try to seat same player twice (with dynamic seat (-1))
        JoinRequestAction apan4Action = new JoinRequestAction(2, 1, -1, "apa");
        assertTrue(rules.actionAllowed(apan4Action, table));
        
        // Ticket #58. Try to seat same player twice, but when the table is full. 
        
	}

	public void testTicket58() {
        // Ticket #58. Try to seat same player twice, but when the table is full.
        FirebaseTable table = createTable(1, 2);
        DefaultSeatingRules rules = new DefaultSeatingRules();

        // Seat two players.
        GenericPlayer player1 = new GenericPlayer(1, "A");
        GenericPlayer player2 = new GenericPlayer(2, "B");
        table.getPlayerSet().addPlayer(player1, 0);
        table.getPlayerSet().addPlayer(player2, 1);
        table.commit();		
        
        // Let player 1 rejoin.
        JoinRequestAction action = new JoinRequestAction(1, 1, -1, "A");
        assertTrue(rules.actionAllowed(action, table));
    }

	
	public void testReserveSeat() {
		FirebaseTable table = createTable(1, 10);
		DefaultSeatingRules rules = new DefaultSeatingRules();
		
		ReserveSeatRequestAction action = new ReserveSeatRequestAction(2, 1, 3);
		assertTrue(rules.actionAllowed(action, table));
		
		action = new ReserveSeatRequestAction(2, 1, 13);
		assertFalse(rules.actionAllowed(action, table));
		
		// Seat APAN on seat 1
		GenericPlayer player = new GenericPlayer(2, "apa");
        table.getPlayerSet().addPlayer(player, 1);
        table.commit();
        
        action = new ReserveSeatRequestAction(3, 1, 3);
		assertTrue(rules.actionAllowed(action, table));
		
		action = new ReserveSeatRequestAction(2, 1, 3);
		assertFalse(rules.actionAllowed(action, table));
		
		
		
	}
	
	// --- PRIVATE METHODS --- //
    
    private FirebaseTable createTable(int id, int seats) {
    	InternalMetaData meta = new InternalMetaData();
		meta.setTableId(id);
		TableData data = fact.createTableData(meta, seats);
		return fact.createTable(data);
	}
}
