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

package com.cubeia.poker.shutdown.impl;

import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.router.RouterService;
import com.cubeia.firebase.api.service.sysstate.PublicSystemStateService;
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.games.poker.tournament.messages.CancelTournament;
import com.cubeia.poker.broadcast.api.BroadcastService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;

import javax.management.ObjectName;

/**
 * This is the collector implementation. It caches hands in a map
 * and will use an optional hand history persister service to
 * persist the result when the hand is ended. If no persister service
 * is deployed it will write the hand to the logs in JSON format on
 * DEBUG level.
 *
 * @author Lars J. Nilsson
 */
@Singleton
public class ShutdownService implements ShutdownServiceContract, com.cubeia.firebase.api.service.Service, ShutdownServiceMBean {

    public static final String POKER_NODE_PATH = "/poker";
    public static final String POKER_SYSTEM_STATUS = "POKER_SYSTEM_STATUS";
    public static final String UP = "UP";
    public static final String SHUTTING_DOWN = "SHUTTING_DOWN";
    public static final String DOWN = "DOWN";

    @Log4j
    private Logger log;

    @Inject
    private ServiceContext context;

    @Service
    private PublicSystemStateService systemStateService;

    @Service
    private RouterService router;

    @Service(proxy = true)
    private BroadcastService broadcastService;

    @Override
    public void init(ServiceContext con) throws SystemException {
    }

    @Override
    public void start() {
        try {
            context.getMBeanServer().registerMBean(this, new ObjectName("com.cubeia.poker.shutdown:type=ShutdownService"));
        } catch (Exception e) {
            log.warn("Failed registering mbean.", e);
        }

        setPokerSystemStatus(UP);
    }

    @Override
    public boolean isSystemShuttingDown() {
        String status = getPokerSystemStatus();
        return SHUTTING_DOWN.equals(status) || DOWN.equals(status);
    }

    @Override
    public boolean isSystemShutDown() {
        return DOWN.equals(getPokerSystemStatus());
    }

    @Override
    public void shutDownTournament(int tournamentId) {
        router.getRouter().dispatchToTournament(tournamentId, new MttObjectAction(tournamentId, new CancelTournament()));
    }

    @Override
    public boolean prepareShutdown() {
        log.info("Preparing system shutdown.");
        broadcastService.broadcastMessage("The system will be closing down shortly, please finish your game sessions.");
        setPokerSystemStatus(SHUTTING_DOWN);
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean finishShutdown() {
        log.info("Finishing system shutdown");
        if (isSystemShuttingDown()) {
            setPokerSystemStatus(DOWN);
            broadcastService.broadcastMessage("The system is now down.");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
    }

    private String getPokerSystemStatus() {
        return systemStateService.getAttribute(POKER_NODE_PATH, POKER_SYSTEM_STATUS).getStringValue();
    }

    private void setPokerSystemStatus(String pokerSystemStatus) {
        systemStateService.setAttribute(POKER_NODE_PATH, POKER_SYSTEM_STATUS, new AttributeValue(pokerSystemStatus));
    }
}
