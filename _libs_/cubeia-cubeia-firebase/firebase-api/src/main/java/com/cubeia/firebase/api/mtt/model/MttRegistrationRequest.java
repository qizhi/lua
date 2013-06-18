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
package com.cubeia.firebase.api.mtt.model;

import java.util.List;

import com.cubeia.firebase.api.common.Attribute;

/**
 * Data Transfer Object for a tournament registration request.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class MttRegistrationRequest {
    
    private final MttPlayer player;
    
    private final List<Attribute> parameters;

    public MttRegistrationRequest(MttPlayer player, List<Attribute> parameters) {
        this.player = player;
        this.parameters = parameters;
    }

    public String toString() {
        return "MttRegistrationRequest - player["+player+"] params["+parameters+"]";
    }
    
    public MttPlayer getPlayer() {
        return player;
    }

    public List<Attribute> getParameters() {
        return parameters;
    }
    
}
