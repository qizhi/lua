package com.cubeia.firebase.test.blackbox.tickets;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.FilteredJoinResponseStatus;
import com.cubeia.firebase.test.blackbox.MultiClientTest;
import com.cubeia.firebase.test.blackbox.util.ParameterParserUtil;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.Builder;
import com.cubeia.firebase.test.common.rules.impl.MemberAssertFilter;
import com.cubeia.firebase.test.common.util.Parameters;
 
/*
 * This test is tricky to fail correctly as it is dependent on
 * timing. But in essence, this is the problem: Given a number of
 * table with X seats, where X+n clients do a filtered join at the
 * same time, and one or more of the n clients manages to do the
 * request *before* the lobby is updated correctly, the waiting list
 * will attempt to join more than X clients to the same table, and
 * trigger the bug. 
 * 
 * In order to test this behavior, the JBC transactions was disabled
 * for the system state (hard coded) and a replication sync interval of
 * 300 milliseconds used. 
 */
public class Ticket248Test extends MultiClientTest {
	
	private final int GAME_ID = 98;
	private final String PATH = "/";
	private final Logger log = Logger.getLogger(getClass());
	
	
	// --- INSTANCE MEMBERS --- //
	
	private int seq = 0;
	
	public Ticket248Test() {
		setNumberOfClients(12);
	}

	@Test
	public void testWaitingList() throws Exception {
		
		/*
		 * Create two tables with 6 seats. Also map them
		 * for easy access later.
		 */
		GameTable[] tables = super.createTables(2, 6);
		Map<Integer, GameTable> map = new HashMap<Integer, GameTable>();
		for (GameTable t : tables) {
			map.put(t.getTableId(), t);
		}
		
		/*
		 * If we're in a cluster, wait for the lobby to catch up
		 */
		Thread.sleep(500);
		
		for (Client c : clients) {
			/*
			 * Send a filtered join.
			 */
			log.debug("Ticket 248: Testing client " + c.getPlayerId());
			c.sendFirebasePacket(new FilteredJoinTableRequestPacket(seq++, GAME_ID, PATH, ParameterParserUtil.getFilteredParams("")));
			
			/*
			 * Expect a response which indicates that we are in the
			 * seating (ie. not in waiting list).
			 */
			c.expect(Builder.expect(
	    			FilteredJoinTableResponsePacket.class, 
	    			new MemberAssertFilter("status", FilteredJoinResponseStatus.SEATING)
	    		));
			
			/*
			 * Expect an available table and join it
			 */
			FilteredJoinTableAvailablePacket packet = c.expectFirebasePacket(FilteredJoinTableAvailablePacket.class);
			map.get(packet.tableid).join(c, packet.seat, Parameters.emptyParams(), true);
		}
	}
}
