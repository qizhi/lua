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

package com.cubeia.games.poker.tournament;

import com.cubeia.firebase.guice.tournament.Configuration;
import com.cubeia.firebase.guice.tournament.GuiceTournament;
import com.cubeia.firebase.guice.tournament.TournamentHandler;
import com.google.inject.Module;

import java.util.List;

public class GuicePokerTournament extends GuiceTournament {

    @Override
    public Configuration getConfigurationHelp() {
        return new Configuration() {
            @Override
            public Class<? extends TournamentHandler> getTournamentHandlerClass() {
                return PokerTournamentProcessor.class;
            }
        };
    }

    @Override
    protected void preInjectorCreation(List<Module> modules) {
        modules.add(new PokerTournamentBindings());
    }
}
