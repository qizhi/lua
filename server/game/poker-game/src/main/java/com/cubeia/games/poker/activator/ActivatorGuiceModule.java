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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

public class ActivatorGuiceModule extends FirebaseModule {

    private final ActivatorContext context;
    private final boolean useDatabase;

    private final Logger log = Logger.getLogger(getClass());

    public ActivatorGuiceModule(ActivatorContext context, boolean useDatabase) {
        super(context.getServices());
        this.context = context;
        this.useDatabase = useDatabase;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ScheduledExecutorService.class).annotatedWith(Names.named("activatorThreads")).toInstance(Executors.newSingleThreadScheduledExecutor());
        bind(TableFactory.class).toInstance(context.getTableFactory());
        bind(ActivatorContext.class).toInstance(context);
        bind(ParticipantFactory.class).to(ParticipantFactoryImpl.class);
        bind(LobbyDomainSelector.class).to(LobbyDomainSelectorImpl.class);
        bind(PokerStateCreator.class).to(InjectorPokerStateCreator.class);
        bind(TableActionHandler.class).to(TableActionHandlerImpl.class);
        bind(ActivatorRouter.class).toInstance(context.getActivatorRouter());
        bind(ActivatorTableManager.class).to(ActivatorTableManagerImpl.class);
        bind(LobbyTableInspector.class).to(LobbyTableInspectorImpl.class);
        bind(MttTableCreationHandler.class).to(MttTableCreationHandlerImpl.class);
        bind(TableNameManager.class).to(MapTableNameManager.class);
        if (!useDatabase) {
            // bind dummy configuration
            bind(TableConfigTemplateProvider.class).to(SimpleTableConfigTemplateProvider.class);
            log.info("Using dummy table template configuration.");
        } else {
            // install JPA and bind DB access
            install(new JpaPersistModule("pokerGameUnit"));
            bind(TableConfigTemplateProvider.class).to(DatabaseTableConfigTemplateProvider.class);
            log.info("Using table template configuration from database.");
        }
        bind(Long.class).annotatedWith(Names.named("activatorInterval")).toProvider(new Provider<Long>() {

            @Service
            private PokerConfigurationService serv;

            @Override
            public Long get() {
                return serv.getActivatorConfig().getActivatorInterval();
            }
        });
    }
}
