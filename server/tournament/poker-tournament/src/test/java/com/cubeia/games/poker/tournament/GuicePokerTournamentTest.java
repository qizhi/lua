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

package com.cubeia.games.poker.tournament;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.guice.game.EventScoped;
import com.cubeia.games.poker.tournament.lobby.TournamentLobbyFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scope;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;

public class GuicePokerTournamentTest {

    @Test @Ignore
    public void testDependencies() {
        Injector injector = Guice.createInjector(new PokerTournamentBindings(), new MockDependencies());
        TournamentLobbyFactory lobbyFactory = injector.getInstance(TournamentLobbyFactory.class);
        assertNotNull(lobbyFactory);
    }

    private static class MockDependencies extends AbstractModule {

        @Mock
        CashGamesBackendService backend;

        @Override
        protected void configure() {
//            bind()
            bindScope(EventScoped.class, new MockScope());
        }

        @Provides
        CashGamesBackendService provideBackEndService() {
            return backend;
        }
    }

    private static class MockScope implements Scope {

        @Override
        public <T> Provider<T> scope(Key<T> tKey, Provider<T> tProvider) {
            System.out.println("scope " + tKey);
            return null;
        }
    }
}
