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

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.backoffice.operator.client.OperatorServiceClientHTTP;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.game.poker.operator.api.FirebaseOperatorService;
import com.cubeia.game.poker.operator.api.OperatorServiceConfig;

/**
 * Firebase LocalHandlerService that handles operator configuration before
 * the user logs in
 */
public class FirebaseOperatorServiceImpl implements FirebaseOperatorService, Service {

    private final Logger log = Logger.getLogger(FirebaseOperatorServiceImpl.class);

    private String baseUrl;
    private Integer configCacheTTL;

    OperatorServiceClient client;

    ObjectMapper mapper;


    @Override
    public void init(ServiceContext context) throws SystemException {
        log.debug("Initializing operator service");
        mapper = new ObjectMapper();
        initConfig(context);
    }

    private void initConfig(ServiceContext context) {
        try {
            ClusterConfigProviderContract contr = context.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
            OperatorServiceConfig configuration = contr.getConfiguration(OperatorServiceConfig.class, new Namespace(OperatorServiceConfig.NAMESPACE));

            baseUrl = System.getProperty("com.cubeia.network.operatorservice.base-url");
            if (baseUrl == null) {
                baseUrl = configuration.getBaseUrl();
            }
            log.info("Operator service will connect to user service at this URL: "+baseUrl);

            if (baseUrl == null || !baseUrl.startsWith("http:")) {
                log.warn("The user service URL does not contain a full URL pattern, e.g. 'http://userservice:8080/operator-service'");
            }


            String ttl = System.getProperty("com.cubeia.netweork.operatorservice.config-cache-ttl");
            if(ttl==null) {
                ttl = configuration.getConfigCacheTTL();
            }
            configCacheTTL = Integer.valueOf(ttl);
            log.info("Operator service config cache ttl set to " + configCacheTTL);

        } catch (ConfigurationException e) {
            log.error("Failed to read operator service configuration. Will fall back on default value", e);
        }
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

        } catch (Exception e) {
            log.error("Unable to marshal operator configuration", e);
        }
    }

    private Map<OperatorConfigParamDTO, String> getClientConfiguration(Long operatorId) {
        return client.getClientConfig(operatorId);
    }

    @Override
    public void start() {
        client = new OperatorServiceClientHTTP(baseUrl,configCacheTTL);
        log.debug("Starting operator service");
    }

    @Override
    public void stop() {
    }
}
