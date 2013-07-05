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

package com.cubeia.games.poker.gameplay;

import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.games.poker.PokerGame;
import mock.MockServiceRegistry;
import org.junit.Test;

public class BasicGameTest {

    @Test
    public void testBasic() throws Exception {
        PokerGame game = new PokerGame();

        game.init(new GameContext() {
            public ResourceLocator getResourceLocator() {
                return null;
            }

            public ServiceRegistry getServices() {
                MockServiceRegistry mockServiceRegistry = new MockServiceRegistry();
                // mockServiceRegistry.addService(WalletServiceContract.class, mockWalletService);
                return mockServiceRegistry;
            }
        });
    }

}
