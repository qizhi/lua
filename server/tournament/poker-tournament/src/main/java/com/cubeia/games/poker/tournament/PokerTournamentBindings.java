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

import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.tournament.lobby.TournamentLobbyFactory;
import com.cubeia.games.poker.tournament.util.PacketSenderFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class PokerTournamentBindings extends AbstractModule {

    @Override
    protected void configure() {
        bind(StyxSerializer.class).toProvider(StyxSerializerProvider.class);
        install(new FactoryModuleBuilder().build(PacketSenderFactory.class));
        bind(TournamentLobbyFactory.class);
    }

    private static class StyxSerializerProvider implements Provider<StyxSerializer> {

        @Override
        public StyxSerializer get() {
            return new StyxSerializer(new ProtocolObjectFactory());
        }
    }
}
