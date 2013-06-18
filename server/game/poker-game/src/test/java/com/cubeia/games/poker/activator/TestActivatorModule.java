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

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.guice.inject.FirebaseModule;

class TestActivatorModule extends FirebaseModule {

    private ServiceRegistry registry;

    public TestActivatorModule() {
        this(new TestServiceRegistry());
    }

    public TestActivatorModule(ServiceRegistry reg) {
        super(reg);
        this.registry = reg;
    }

    @Override
    protected void configure() {
        bind(ServiceRegistry.class).toInstance(registry);
        bind(ParticipantFactory.class).to(ParticipantFactoryImpl.class);
        bind(LobbyDomainSelector.class).to(LobbyDomainSelectorImpl.class);
        bind(PokerStateCreator.class).to(InjectorPokerStateCreator.class);
        bind(TableNameManager.class).to(MapTableNameManager.class);
        super.configure();
    }
}