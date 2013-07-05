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

package com.cubeia.backend.cashgame.dto;

public class AllowJoinResponse {

    public final boolean allowed;
    public final int responseCode;

    public AllowJoinResponse(boolean allowed, int responseCode) {
        this.allowed = allowed;
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AllowJoinResponse");
        sb.append("{allowed=").append(allowed);
        sb.append(", responseCode=").append(responseCode);
        sb.append('}');
        return sb.toString();
    }
}
