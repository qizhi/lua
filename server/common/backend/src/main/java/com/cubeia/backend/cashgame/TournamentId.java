/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.backend.cashgame;

import java.io.Serializable;

public class TournamentId implements Serializable {

    public final String integrationId;
    public final int instanceId;

    public TournamentId(String integrationId, int instanceId) {
        this.integrationId = integrationId;
        this.instanceId = instanceId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public int getInstanceId() {
        return instanceId;
    }
}
