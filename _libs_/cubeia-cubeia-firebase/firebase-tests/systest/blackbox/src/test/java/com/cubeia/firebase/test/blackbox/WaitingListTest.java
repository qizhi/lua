package com.cubeia.firebase.test.blackbox;

import static org.testng.AssertJUnit.fail;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.FilteredJoinResponseStatus;
import com.cubeia.firebase.test.blackbox.util.ParameterParserUtil;
import com.cubeia.firebase.test.common.Client;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.Builder;
import com.cubeia.firebase.test.common.rules.impl.MemberAssertFilter;

public class WaitingListTest extends FirebaseTest {

	private final int GAME_ID = 98;
	private final String PATH = "/";
	private final String CRITERIUM = "_SEATED>0";
	
	
	// --- INSTANCE MEMBERS --- //
	
	private int seq = 0;

	private Client client1;
	private Client client2;
	
	private String password1;
	private String username1;
	private String password2;
	private String username2;
	
	
	// --- SETTERS --- //
	
	@BeforeClass
	@Parameters({ "username", "password"})
	public void setUserOne(
			@Optional("dummyUser1") String username,
			@Optional("666") int password) {

		this.password1 = String.valueOf(password);
		this.username1 = username;
	}
	
	@BeforeClass
	@Parameters({ "username", "password"})
	public void setUserTwo(
			@Optional("dummyUser2") String username,
			@Optional("667") int password) {

		this.password2 = String.valueOf(password);
		this.username2 = username;
	}
	
	
	// --- LIFETIME --- //

	@BeforeMethod
	public void login() throws Exception {
		client1 = super.newClient();
		super.connectClient(client1);
		client1.login(username1, password1, true);	
		
		client2 = super.newClient();
		super.connectClient(client2);
		client2.login(username2, password2, true);
	}
	
	@AfterMethod
	public void logout() throws IOException {
		closeClient(client1);
		closeClient(client2);
	}
	
	private void closeClient(Client cl) {
		try {
			cl.logout(true);
		} finally {
			cl.disconnect();
		}
	}

	
	// --- TESTS --- //
	
	@Test
    public void testWaitlistSeatingDoesNotSeatSamePlayer() throws Exception {

		/*
		 * Send join (98, /, _SEATED>0)
		 */
	    client1.sendFirebasePacket(new FilteredJoinTableRequestPacket(seq++, GAME_ID, PATH, ParameterParserUtil.getFilteredParams(CRITERIUM)));
	    
	    /*
	     * Create and join table
	     */
	    GameTable table = super.createTable(4);
	    table.join(client1, true);
	    
	    try {
	    	/*
	    	 * At this we should *not* get an available packet (as we 
	    	 * shouldn't match a table were we're sitting).
	    	 */
	    	client1.expectFirebasePacket(FilteredJoinTableAvailablePacket.class);
	    	fail("Unexpected FilteredJoinTableAvailablePacket");
	    } catch (Exception expected) { }
	    
	    /*
	     * Leave table
	     */
	    table.leave(client1, true);
	    
    }

	@Test
    public void testWaitlistSeating() throws Exception {
	    
		/*
		 * Send join (98, /, _SEATED>0)
		 */
	    client1.sendFirebasePacket(new FilteredJoinTableRequestPacket(seq++, GAME_ID, PATH, ParameterParserUtil.getFilteredParams(CRITERIUM)));
	    
	    /*
	     * Create and join table with client2
	     */
	    GameTable table = super.createTable(4);
	    table.join(client2, true);
	    
	    /*
	     * Make sure we got a table available for client one.
	     */
        client1.expectFirebasePacket(FilteredJoinTableAvailablePacket.class);
        
        /*
         * Leave table
         */
        table.leave(client2, true);
        
    }

	@Test
	public void testDirectSeatingDoesNotSeatSamePlayer() throws Exception {
		/*
		 * Create and join table
		 */
		GameTable table = super.createTable(4);
	    table.join(client1, true);
	    
		/*
		 * Send join (98, /, _SEATED>0)
		 */
		client1.sendFirebasePacket(new FilteredJoinTableRequestPacket(seq++, GAME_ID, PATH, ParameterParserUtil.getFilteredParams(CRITERIUM)));
		
		/*
		 * Expect a response which indicates that we are in the
		 * waiting list (ie. not seating).
		 */
	    client1.expect(Builder.expect(
    			FilteredJoinTableResponsePacket.class, 
    			new MemberAssertFilter("status", FilteredJoinResponseStatus.WAIT_LIST)
    		));
		
		try {
			/*
			 * At this point, double check that we don't get any table 
			 * available notification.
			 */
			client1.expectFirebasePacket(FilteredJoinTableAvailablePacket.class);
			fail("Unexpected FilteredJoinTableAvailablePacket");
		} catch (Exception expected) { }
	}
	
	@Test
    public void testTicket529() throws Exception {

		/*
		 * Create table with two seats, and join (ie.
		 * make sure there's exactly one seat free)
		 */
		GameTable table = super.createTable(2);
	    table.join(client1, true);
		
		/*
		 * Send join (98, /, _SEATED>0)
		 */
	    client1.sendFirebasePacket(new FilteredJoinTableRequestPacket(seq++, GAME_ID, PATH, ParameterParserUtil.getFilteredParams(CRITERIUM)));
	    
		/*
		 * Expect a response which indicates that we are in the
		 * waiting list (ie. not seating).
		 */
	    client1.expect(Builder.expect(
	    			FilteredJoinTableResponsePacket.class, 
	    			new MemberAssertFilter("status", FilteredJoinResponseStatus.WAIT_LIST)
	    		));
	    
	    /*
	     * Leave table
	     */
	    table.leave(client1, true);
	    
    }
}
