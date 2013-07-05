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

package com.cubeia.backend.cashgame;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TableId implements Serializable {

    private static final long serialVersionUID = 4377559096783591987L;

    /**
     * Firebase game ID. Mandatory.
     */
    public final int gameId;

    /**
     * Firebase table ID. Mandatory.
     */
    public final int tableId;

    /**
     * Optional ID produced by integration,
     * may be null.
     */
    public final String integrationId;

    /**
     * @param gameId        Firebase game ID, must not be null
     * @param tableId       Firebase table ID, must not be null
     * @param integrationId Optional integration ID, may be null
     */
    public TableId(int gameId, int tableId, String integrationId) {
        this.gameId = gameId;
        this.tableId = tableId;
        this.integrationId = integrationId;
    }

    /**
     * @param tableId       Firebase table ID, must not be null
     * @param integrationId Optional integration ID, may be null
     */
    public TableId(TableId tableId, String integrationId) {
        this(tableId.gameId, tableId.tableId, integrationId);
    }

    /**
     * @param gameId  Firebase game ID, must not be null
     * @param tableId Firebase table ID, must not be null
     */
    public TableId(int gameId, int tableId) {
        this(gameId, tableId, null);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
