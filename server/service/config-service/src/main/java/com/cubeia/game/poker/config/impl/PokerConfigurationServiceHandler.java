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

package com.cubeia.game.poker.config.impl;

import com.cubeia.firebase.guice.service.Configuration;
import com.cubeia.firebase.guice.service.ContractsConfig;
import com.cubeia.firebase.guice.service.GuiceServiceHandler;
import com.cubeia.game.poker.config.api.PokerConfigurationService;

public class PokerConfigurationServiceHandler extends GuiceServiceHandler {

    @Override
    protected Configuration getConfiguration() {
        return new Configuration() {

            @Override
            public ContractsConfig getServiceContract() {
                return new ContractsConfig(PokerConfigurationServiceImpl.class, PokerConfigurationService.class);
            }
        };
    }
}
