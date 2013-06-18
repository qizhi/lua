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

import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.shutdown.impl.ShutdownService;
import org.mockito.Mockito;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.game.poker.config.api.PokerConfigurationService;

import static org.mockito.Mockito.mock;

public class TestServiceRegistry extends ServiceRegistryAdapter {

    public TestServiceRegistry() {
        super.addImplementation(CashGamesBackendService.class, mock(CashGamesBackendService.class));
        super.addImplementation(PokerConfigurationService.class, mock(PokerConfigurationService.class));
        super.addImplementation(ShutdownServiceContract.class, mock(ShutdownServiceContract.class));
    }
}