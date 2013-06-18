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
package com.cubeia.game.poker.operator.impl;

import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClientHTTP;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.game.poker.operator.api.FirebaseOperatorService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MockFirebaseOperatorServiceImpl implements FirebaseOperatorService, Service {

    private final Logger log = Logger.getLogger(MockFirebaseOperatorServiceImpl.class);

    ObjectMapper mapper;


    @Override
    public void init(ServiceContext context) throws SystemException {
        log.debug("Initializing mock operator service");
        mapper = new ObjectMapper();
    }


    @Override
    public void destroy() {
    }

    @Override
    public void handleAction(LocalServiceAction action, LocalActionHandler loopBack) {
        try {
            String decoded = new String(action.getData());

            Long operatorId = Long.parseLong(decoded);

            log.debug("Retrieving settings for operator " + operatorId);

            LocalServiceAction response = new LocalServiceAction();

            Map<OperatorConfigParamDTO, String> operatorConfig = getClientConfiguration(operatorId);
            response.setData(mapper.writeValueAsBytes(operatorConfig));

            loopBack.handleAction(response);

        } catch (IOException e) {
            log.error("Unable to marshal operator configuration", e);
        }
    }

    private Map<OperatorConfigParamDTO, String> getClientConfiguration(Long operatorId) {
        Map<OperatorConfigParamDTO, String> operatorConfig = new HashMap<OperatorConfigParamDTO, String>();
        operatorConfig.put(OperatorConfigParamDTO.CLIENT_HELP_URL, "http://cubeia.org");
        return operatorConfig;
    }

    @Override
    public void start() {
        log.debug("Starting mock operator service");
    }

    @Override
    public void stop() {
    }
}
