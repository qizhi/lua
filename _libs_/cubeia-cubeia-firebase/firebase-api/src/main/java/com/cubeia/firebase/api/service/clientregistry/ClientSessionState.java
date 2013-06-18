/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.service.clientregistry;

public enum ClientSessionState {
    
    /**
     * The client is logged in to the system
     */
    CONNECTED,
    
    /**
     * The client is disconnected but we still have
     * the session in the client registry. If the client
     * reconnects before the reaper then the client will
     * revert to LOGGED_IN.
     */
    WAIT_REJOIN,
    
    /**
     * The client was not found in the registry.
     */
    NOT_CONNECTED    
}
