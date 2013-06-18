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

package com.cubeia.games.poker;

import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.guice.game.Configuration;
import com.cubeia.firebase.guice.game.GuiceGame;
import com.cubeia.games.poker.jmx.PokerStats;
import com.google.inject.Module;

import java.util.List;

public class PokerGame extends GuiceGame {
    /*------------------------------------------------

         LIFECYCLE METHODS

         The constructor and methods will be called
         by Firebase which manages the life cycle of
         the Game.

      ------------------------------------------------*/
    public PokerGame() {
        // Trigger poker stats creation and binding to JMX
        PokerStats.getInstance();
    }

    public void init(GameContext con) throws SystemException {
        super.init(con);
        // services = con.getServices();
    }

    public void destroy() {
    }


    /*------------------------------------------------

         GUICE IoC METHODS

         Adapter methods for Guice support.

      ------------------------------------------------*/

    @Override
    public Configuration getConfigurationHelp() {
        return new IntegrationGuiceConfig();
    }

    @Override
    protected void preInjectorCreation(List<Module> modules) {
        modules.add(new IntegrationGuiceModule());
    }

    /*------------------------------------------------

           END OF GUICE IoC METHODS

       ------------------------------------------------*/


}
