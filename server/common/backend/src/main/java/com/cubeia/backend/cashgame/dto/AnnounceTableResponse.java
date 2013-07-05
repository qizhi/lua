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

import com.cubeia.backend.cashgame.TableId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class AnnounceTableResponse implements Serializable {

    private final Map<String, String> tableProperties;
    private final TableId tableId;

    public AnnounceTableResponse(TableId tableId) {
        this.tableId = tableId;
        tableProperties = new HashMap<String, String>();
    }

    public String getProperty(String key) {
        return getTableProperties().get(key);
    }

    public void setProperty(String key, String value) {
        tableProperties.put(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AnnounceTableResponse");
        sb.append("{tableProperties=").append(tableProperties);
        sb.append(", tableId=").append(tableId);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns an copy of the response properties.
     *
     * @return copy of properties
     */
    public Map<String, String> getTableProperties() {
        return new HashMap<String, String>(tableProperties);
    }

    public TableId getTableId() {
        return tableId;
    }
}
