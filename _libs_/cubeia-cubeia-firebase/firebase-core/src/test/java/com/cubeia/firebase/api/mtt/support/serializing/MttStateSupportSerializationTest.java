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
package com.cubeia.firebase.api.mtt.support.serializing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttRegisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesRemovedAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttPlayerStatus;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.support.LobbyAttributeAccessorAdapter;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MttNotifierAdapter;
import com.cubeia.firebase.api.mtt.support.MttSupportAdapter;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.util.Serializer;

public class MttStateSupportSerializationTest extends TestCase {
    
    private static int PLAYER_COUNT = 1000;
    
    private MttSupportAdapter support;
    private MTTStateSupport state;
    private MttInstance instance;

	@SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        createMockStateAndSupport();
        
        // HACK !!!!
        support.setMttNotifier(instance.getMttNotifier());
        support.setTableCreator(instance.getTableCreator());
    }
    
    

	@SuppressWarnings("deprecation")
    public void testRegisterPlayers() throws Exception {
        state.setCapacity(PLAYER_COUNT);
        assertEquals(0, state.getRegisteredPlayersCount());
        
        for (int i = 0; i < PLAYER_COUNT; i++) {
            MttAction action = new MttRegisterPlayerAction(1, i);
            support.handle(action, instance);
        }

        assertEquals(state.getCapacity(), state.getRegisteredPlayersCount());
        byte[] bytes = new Serializer().serialize(state);
        
        System.out.println("Serialized registered state ("+PLAYER_COUNT+" players) : "+bytes.length+" bytes. "+(bytes.length/state.getRegisteredPlayersCount())+" bytes/player");
        //MTTStateSupport readState = (MTTStateSupport)new Serializer().deserialize(bytes);
        
        // HACK: RESET THE CREATOR !!!!
        support.setTableCreator(instance.getTableCreator());
        support.createTables(state, 100, "testTable");
        
        bytes = new Serializer().serialize(state);
        System.out.println("Serialized tables state ("+PLAYER_COUNT+" players) : "+bytes.length+" bytes. "+(bytes.length/state.getRegisteredPlayersCount())+" bytes/player");
        // readState = (MTTStateSupport)new Serializer().deserialize(bytes);
        
        int pid = 0;
        for (int tableId : state.getTables() ) {
            for (int seat = 0; seat < 10; seat++) {
                // HACK: RESET THE NOTIFIER !!!!
                support.setMttNotifier(instance.getMttNotifier());
                support.seatPlayers(state, Arrays.asList(new SeatingContainer(pid++, tableId, 0)));
            }
        }
        
        System.out.println("Seated players: "+state.getRemainingPlayerCount());
        System.out.println("Tables: "+state.getTables().size());
        
        bytes = new Serializer().serialize(state);
        System.out.println("Serialized seated state ("+PLAYER_COUNT+" players) : "+bytes.length+" bytes. "+(bytes.length/state.getRegisteredPlayersCount())+" bytes/player");
        // readState = (MTTStateSupport)new Serializer().deserialize(bytes);
    }
    
    
    public void testSerializeMttPlayer() throws Exception {
        MttPlayer player = new MttPlayer(111, "Testplayer");
        player.setPosition(77);
        player.setStatus(MttPlayerStatus.PLAYING);
        
        byte[] bytes = new Serializer().serialize(player);
        
        System.out.println("Serialized player: "+bytes.length+" bytes");
        
//        System.out.println("Player data: "+Arrays.toString(bytes));
        
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
            
            public MttNotifier getMttNotifier() {
            	return new MttNotifierAdapter();
            }
        };
        state.setSeats(10);
        
        support = new MttSupportAdapter();
        //support.setMttNotifier();
        //support.setTableCreator();
    }

}
