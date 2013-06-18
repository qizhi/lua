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

package com.cubeia.poker.shutdown.api;

import com.cubeia.firebase.api.service.Contract;

/**
 * Service contract for shutting down the system.
 *
 * Will be called via JMX.
 *
 */
public interface ShutdownServiceContract extends Contract {

    /**
     * Checks if the system is currently in shutting down mode or already shut down.
     * @return true if the system is shutting down or shut down and false if not
     */
    public boolean isSystemShuttingDown();

    /**
     * Checks if the system is shut down.
     *
     * @return true if the system is shut down, false otherwise
     */
    public boolean isSystemShutDown();

    /**
     * Shuts down a given tournament. The tournament will be cancelled. Should only be called
     * for tournaments that have not been started.
     *
     * @param tournamentId
     */
    void shutDownTournament(int tournamentId);
}
