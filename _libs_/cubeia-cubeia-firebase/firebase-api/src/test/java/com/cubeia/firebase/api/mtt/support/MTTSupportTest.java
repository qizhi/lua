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
package com.cubeia.firebase.api.mtt.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.StartMttRoundAction;
import com.cubeia.firebase.api.action.StopMttRoundAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttRegisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesRemovedAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;

@SuppressWarnings("unchecked")
public class MTTSupportTest extends TestCase {

    private MttSupportAdapter support;
    private MTTStateSupport state;
    private MttInstance instance;
    
	@Before
	@SuppressWarnings("deprecation")
    public void setUp() {
        createMockStateAndSupport();
        
        // HACK!!!!
        support.setMttNotifier(instance.getMttNotifier());
        support.setTableCreator(instance.getTableCreator());
        
    }
    
	
    @Test
    public void testCreateTables() {
        assertEquals(0, state.getTables().size());
        support.createTables(state, 10, "testTable");
        assertEquals(10, state.getTables().size());
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testRemoveTables() {
        support.createTables(state, 3, "testTable");
        assertEquals(3, state.getTables().size());
        // HACK: RESET THE CREATOR !!!!
        support.setTableCreator(instance.getTableCreator());
        support.closeTables(state, state.getTables());
        assertEquals(0, state.getTables().size());
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testSeatPlayers() {
        support.createTables(state, 1, "testTable");
        int tableId = state.getTables().iterator().next();
        MttRegistrationRequest req = new MttRegistrationRequest(new MttPlayer(10, "p1"), Collections.EMPTY_LIST);
        state.getPlayerRegistry().register(instance, req);
        // HACK: RESET THE NOTIFIER !!!!
        support.setMttNotifier(instance.getMttNotifier());
        support.seatPlayers(state, Arrays.asList(new SeatingContainer(10, tableId, 0)));
        assertEquals(tableId, support.getTableIdByPlayerId(state, 10));
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testUnseatPlayers() {
        support.createTables(state, 1, "testTable");
        int tableId = state.getTables().iterator().next();
        MttRegistrationRequest req = new MttRegistrationRequest(new MttPlayer(10, "p1"), Collections.EMPTY_LIST);
        // HACK: RESET THE NOTIFIER !!!!
        support.setMttNotifier(instance.getMttNotifier());
        state.getPlayerRegistry().register(instance, req);
        support.seatPlayers(state, Arrays.asList(new SeatingContainer(10, tableId, 0)));
        support.unseatPlayers(state, tableId, Arrays.asList(10), Reason.OUT);
        assertEquals(-1, support.getTableIdByPlayerId(state, 10));
    }
        
    @Test
	@SuppressWarnings("deprecation")
    public void testMovePlayer() {
        support.createTables(state, 2, "testTable");
        Iterator<Integer> tableIdIter = state.getTables().iterator();
        int tableId1 = tableIdIter.next();
        int tableId2 = tableIdIter.next();
        MttRegistrationRequest req = new MttRegistrationRequest(new MttPlayer(10, "p1"), Collections.EMPTY_LIST);
        // HACK: RESET THE NOTIFIER !!!!
        support.setMttNotifier(instance.getMttNotifier());
        state.getPlayerRegistry().register(instance, req);
        support.seatPlayers(state, Arrays.asList(new SeatingContainer(10, tableId1, 0)));
        support.movePlayer(state, 10, tableId2, -1, Reason.BALANCING, null);
        assertEquals(tableId2, support.getTableIdByPlayerId(state, 10));
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testSendRoundStartActionToTables() {
        support.createTables(state, 2, "testTable");
        Iterator<Integer> tableIdIter = state.getTables().iterator();
        int tableId1 = tableIdIter.next();
        int tableId2 = tableIdIter.next();
        
        final Map<Integer, GameAction> tableToActionMap = new HashMap<Integer, GameAction>();
        MttNotifier mockNotifier = new MttNotifier() {
            public void notifyPlayer(int playerId, MttAction action) {
            }
            public void notifyTable(int tableId, GameAction action) {
                tableToActionMap.put(tableId, action);
            }
        };
        support.setMttNotifier(mockNotifier);
        
        support.sendRoundStartActionToTables(state, state.getTables());
        assertTrue(tableToActionMap.keySet().contains(tableId1));
        assertTrue(tableToActionMap.keySet().contains(tableId2));
        
        StartMttRoundAction a1 = (StartMttRoundAction) tableToActionMap.get(tableId1);
        assertEquals(1, a1.getMttId());
        assertEquals(tableId1, a1.getTableId());
        
        StartMttRoundAction a2 = (StartMttRoundAction) tableToActionMap.get(tableId2);
        assertEquals(1, a2.getMttId());
        assertEquals(tableId2, a2.getTableId());
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testSendRoundStopActionToTables() {
        support.createTables(state, 2, "testTable");
        Iterator<Integer> tableIdIter = state.getTables().iterator();
        int tableId1 = tableIdIter.next();
        int tableId2 = tableIdIter.next();
        
        final Map<Integer, GameAction> tableToActionMap = new HashMap<Integer, GameAction>();
        MttNotifier mockNotifier = new MttNotifier() {
            public void notifyPlayer(int playerId, MttAction action) {
            }
            public void notifyTable(int tableId, GameAction action) {
                tableToActionMap.put(tableId, action);
            }
        };
        support.setMttNotifier(mockNotifier);
        
        support.sendRoundStopActionToTables(state, state.getTables());
        assertTrue(tableToActionMap.keySet().contains(tableId1));
        assertTrue(tableToActionMap.keySet().contains(tableId2));
        
        StopMttRoundAction a1 = (StopMttRoundAction) tableToActionMap.get(tableId1);
        assertEquals(1, a1.getMttId());
        assertEquals(tableId1, a1.getTableId());
        
        StopMttRoundAction a2 = (StopMttRoundAction) tableToActionMap.get(tableId2);
        assertEquals(1, a2.getMttId());
        assertEquals(tableId2, a2.getTableId());
    }

    @Test
	@SuppressWarnings("deprecation")
    public void testGetTableIdByPlayerId() {
        support.createTables(state, 2, "testTable");
        Iterator<Integer> tableIdIter = state.getTables().iterator();
        int tableId1 = tableIdIter.next();
        int tableId2 = tableIdIter.next();
        MttRegistrationRequest req1 = new MttRegistrationRequest(new MttPlayer(10, "p1"), Collections.EMPTY_LIST);
        MttRegistrationRequest req2 = new MttRegistrationRequest(new MttPlayer(11, "p2"), Collections.EMPTY_LIST);
        state.getPlayerRegistry().register(instance, req1);
        state.getPlayerRegistry().register(instance, req2);
        // HACK: RESET THE NOTIFIER !!!!
        support.setMttNotifier(instance.getMttNotifier());
        support.seatPlayers(state, Arrays.asList(new SeatingContainer(10, tableId1, 0)));
        support.seatPlayers(state, Arrays.asList(new SeatingContainer(11, tableId2, 0)));
        assertEquals(tableId1, support.getTableIdByPlayerId(state, 10));
        assertEquals(tableId2, support.getTableIdByPlayerId(state, 11));
    }
    
    @Test
    public void testGetTableIds() {
        support.createTables(state, 2, "testTable");
        assertEquals(2, state.getTables().size());
    }

	private void createMockStateAndSupport() {
        state = new MTTStateSupport(1, 1);
        state.setCapacity(6);
        instance = new MttInstance() {
        	
        	private LobbyAttributeAccessor mockAccessor = new LobbyAttributeAccessorAdapter();
        	
        	public TournamentPlayerRegistry getSystemPlayerRegistry() {
        		return new TournamentPlayerRegistry() {
				
					public void unregisterAll(int mttId) { }
				
					public void unregister(int playerId, int mttId) { }
				
					public void register(int playerId, int mttId) { }
				
					public int[] getPlayersForTournament(int mttId) {
						return new int[0];
					}
					
					public int[] getTournamentsForPlayer(int playerId) {
						return new int[0];
					}
				};
        	}
        	
            public LobbyAttributeAccessor getLobbyAccessor() {
            	return mockAccessor;
            }
            
            public LobbyAttributeAccessor getTableLobbyAccessor(int tableId) {
            	return null;
            }
            
            public Scheduler<MttAction> getScheduler() {
                return null;
            }
            
            public ServiceRegistry getServiceRegistry() {
                return null;
            }
            
            public MTTState getState() {
                return state;
            }
            
            public int getId() {
                return 1;
            }
            
            public MttNotifier getMttNotifier() {
            	return new MttNotifierAdapter();
            }
            
            public MttTableCreator getTableCreator() {
            	return new MttTableCreator() {
                    public void createTables(int gameId, int mttId, int tableCount,
                            int seats, String baseName, Object attachment) {
                            
                            // mock action
                            MttTablesCreatedAction a = new MttTablesCreatedAction(mttId);
                            for (int i = 0; i < tableCount; i++) {
                                a.addTable(i);
                            }
                            support.handle(a, instance);
                        }

                        public void removeTables(int gameId, int mttId,
                            Collection<Integer> tableIds) {
                            
                            // mock action
                            MttTablesRemovedAction a = new MttTablesRemovedAction(mttId);
                            a.setTables(new HashSet<Integer>(tableIds));
                            support.handle(a, instance);
                        }

                        public void removeTables(int gameId, int mttId,
                            Collection<Integer> unusedTables, long delayMs) {
                            removeTables(gameId, mttId, unusedTables);
                        }
            		};
            }
        };
        state.setSeats(2);
        for (int i = 0; i < 4; i++) {
            MttPlayer player = new MttPlayer(i, "a" + i);
            MttRegistrationRequest req = new MttRegistrationRequest(player, Collections.EMPTY_LIST);
            state.getPlayerRegistry().register(instance, req);
        }
	    
        support = new MttSupportAdapter();
		//support.setMttNotifier();
		//support.setTableCreator();
	}
	
	
    @Test
	@SuppressWarnings("deprecation")
	public void testGetRemainingPlayers() {
		int playerId = 10;
		
		// Assert empty from the beginning.
		assertTrue(support.getRemainingPlayers(state).isEmpty());

		// Register a player and check that the set is still empty.
		 MttRegistrationRequest req = new MttRegistrationRequest(new MttPlayer(playerId, "p1"), Collections.EMPTY_LIST);
	        state.getPlayerRegistry().register(instance, req);
		assertTrue(support.getRemainingPlayers(state).isEmpty());
		
		// Seat the player.
        support.createTables(state, 1, "testTable");
        int tableId = state.getTables().iterator().next();
        
        // HACK: RESET THE NOTIFIER !!!!
        support.setMttNotifier(instance.getMttNotifier());
		support.seatPlayers(state, Arrays.asList(new SeatingContainer(10, tableId, 0)));
		
		// Assert that the player exists in the set.
		assertEquals(1, support.getRemainingPlayers(state).size());
		assertEquals(new Integer(playerId), support.getRemainingPlayers(state).iterator().next());
		
		// Remove the player.
		support.unseatPlayers(state, tableId, Collections.singleton(new Integer(playerId)), Reason.BALANCING);
		
		// Assert empty again.
		assertTrue(support.getRemainingPlayers(state).isEmpty());
	}
	
	/**
	 * Try to register more players then the capacity allows. 
	 * The tournament should not register players above capacity.
	 */
	@Test
	public void testRegisterPlayers() {
		state.setCapacity(4);
		assertEquals(state.getCapacity(), state.getRegisteredPlayersCount());
		MttAction action = new MttRegisterPlayerAction(1, 123);
		support.handle(action, instance);
		assertEquals(state.getCapacity(), state.getRegisteredPlayersCount());
	}
}
