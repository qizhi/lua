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
package com.cubeia.firebase.api.action;

import java.util.UUID;

/**
 * Interface for describing identifiable actions.
 * 
 *
 */
public interface IdentifiableAction {

    /**
     * Sets the identifier for this action.
     * 
     * @param identifier a unique identifier for this action.
     */
    public void setIdentifier(UUID identifier);
    
    /**
     * Gets the identifier for this action.
     * 
     * @return the identifier.
     */
    public UUID getIdentifier();
}
