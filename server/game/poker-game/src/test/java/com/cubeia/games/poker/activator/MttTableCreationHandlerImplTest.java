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

import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableGameState;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.poker.PokerState;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MttTableCreationHandlerImplTest {

    @Inject
    private MttTableCreationHandlerImpl handler;

    @Mock
    private Table table;

    @Mock
    private LobbyAttributeAccessor accessor;

    @Mock
    private PokerState pokerState;

    @Mock
    private TablePlayerSet playerSet;

    @Mock
    private TableSeatingMap seatingMap;

    @Mock
    private TableGameState gameState;

    @Before
    public void setup() {
        initMocks(this);
        when(table.getPlayerSet()).thenReturn(playerSet);
        when(playerSet.getSeatingMap()).thenReturn(seatingMap);
        when(seatingMap.getNumberOfSeats()).thenReturn(6);
        when(table.getGameState()).thenReturn(gameState);
        Module m1 = new TestActivatorModule();
        Guice.createInjector(Modules.override(m1).with(new AbstractModule() {

            @Override
            protected void configure() {
                bind(MttTableCreationHandler.class).to(MttTableCreationHandlerImpl.class);
                bind(PokerStateCreator.class).toProvider(new Provider<PokerStateCreator>() {

                    @Override
                    public PokerStateCreator get() {
                        return new PokerStateCreator() {

                            @Override
                            public PokerState newPokerState() {
                                return pokerState;
                            }
                        };
                    }
                });
            }
        })).injectMembers(this);
    }
}
