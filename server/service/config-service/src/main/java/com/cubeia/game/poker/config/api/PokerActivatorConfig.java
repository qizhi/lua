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

package com.cubeia.game.poker.config.api;

import static com.cubeia.firebase.api.server.conf.Inheritance.ALLOW;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Property;

@Configurated(inheritance = ALLOW, namespace = "com.cubeia.game.poker.activator")
public interface PokerActivatorConfig extends Configurable {

    @Property(defaultValue = "true")
    public boolean useMockIntegrations();

    @Property(defaultValue = "60000", property = "default-table-ttl")
    public long getDefaultTableTTL();

    @Property(defaultValue = "15000")
    public long getActivatorInterval();

}
