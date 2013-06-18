package com.cubeia.poker.domainevents.impl;

import com.cubeia.firebase.guice.service.Configuration;
import com.cubeia.firebase.guice.service.ContractsConfig;
import com.cubeia.firebase.guice.service.GuiceServiceHandler;
import com.cubeia.poker.domainevents.api.DomainEventsService;

public class GuiceHandler extends GuiceServiceHandler {

	@Override
    protected Configuration getConfiguration() {
        return new Configuration() {

            @Override
            public ContractsConfig getServiceContract() {
                return new ContractsConfig(DomainEventsServiceImpl.class, DomainEventsService.class);
            }
        };
    }
	
}
