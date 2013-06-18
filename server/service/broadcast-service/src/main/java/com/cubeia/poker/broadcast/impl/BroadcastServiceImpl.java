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

package com.cubeia.poker.broadcast.impl;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.poker.broadcast.api.BroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class BroadcastServiceImpl implements Service, BroadcastService {

    private ServiceContext context;

    private static final Logger logger = LoggerFactory.getLogger(BroadcastService.class);

    private static final String CLIENT_REGISTRY_MBEAN_NAME = "com.cubeia.firebase.clients:type=ClientRegistry";

    private ObjectName CLIENT_REGISTRY_OBJECT_NAME;

    @Override
    public void init(ServiceContext context) throws SystemException {
        this.context = context;
        try {
            CLIENT_REGISTRY_OBJECT_NAME = new ObjectName(CLIENT_REGISTRY_MBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            throw new SystemException("Error creating ObjectName instance.", e);
        }
        logger.info("Initialized.");
    }

    @Override
    public void start() {
        logger.debug("Started.");
    }

    @Override
    public void stop() {
        logger.debug("Stopped.");
    }

    @Override
    public void destroy() {
        logger.debug("Destroyed.");
    }

    @Override
    public void broadcastMessage(String message) {
        try {
            logger.info("Broadcasting message=" + message);
            context.getMBeanServer().invoke(CLIENT_REGISTRY_OBJECT_NAME, "sendSystemMessage", new Object[]{0, 0, message},
                                            new String[]{"int", "int", "java.lang.String"});
        } catch (Exception e) {
            logger.error("Error broadcasting system message.", e);
        }

    }

}