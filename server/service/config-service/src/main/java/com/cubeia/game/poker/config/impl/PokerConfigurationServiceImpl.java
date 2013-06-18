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

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import com.cubeia.games.poker.common.money.Currency;
import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.firebase.guice.inject.Service;

import com.cubeia.game.poker.config.api.HandHistoryConfig;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.game.poker.config.api.PokerActivatorConfig;
import com.cubeia.game.poker.config.api.PokerSystemConfig;
import com.cubeia.games.poker.common.money.Money;
import com.google.inject.Singleton;

@Singleton
public class PokerConfigurationServiceImpl implements com.cubeia.firebase.api.service.Service, PokerConfigurationService {

    @Service(proxy = true)
    private ClusterConfigProviderContract clusterConfig;

    @Log4j
    private Logger log;

    public void init(ServiceContext con) throws SystemException {
    }

    public void start() {
    }
    
    @Override
    public HandHistoryConfig getHandHistoryConfig() {
    	return config(HandHistoryConfig.class);
    }

    @Override
    public PokerActivatorConfig getActivatorConfig() {
        return config(PokerActivatorConfig.class);
    }
    @Override
    public PokerSystemConfig getSystemConfig() {
        return config(PokerSystemConfig.class);
    }


    public void stop() {
    }

    public void destroy() {
    }


    // --- PRIVATE METHODS --- //

    private <T extends Configurable> T config(Class<T> clazz) {
        try {
            return clusterConfig.getConfiguration(clazz, null);
        } catch (ConfigurationException e) {
            log.error("Failed to read configuration", e);
            return null; // Er...
        }
    }

}