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

package com.cubeia.poker.handhistory.api;

import java.io.Serializable;

public class Table implements Serializable {

    private static final long serialVersionUID = -2964211759066901960L;

    private int tableId;
    private String tableIntegrationId;
    private String tableName;
    private int seats;

    public Table() {
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableIntegrationId() {
        return tableIntegrationId;
    }

    public void setTableIntegrationId(String tableIntegrationId) {
        this.tableIntegrationId = tableIntegrationId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table table = (Table) o;

        if (seats != table.seats) return false;
        if (tableId != table.tableId) return false;
        if (tableIntegrationId != null ? !tableIntegrationId.equals(table.tableIntegrationId) : table.tableIntegrationId != null)
            return false;
        if (tableName != null ? !tableName.equals(table.tableName) : table.tableName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tableId;
        result = 31 * result + (tableIntegrationId != null ? tableIntegrationId.hashCode() : 0);
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + seats;
        return result;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableId=" + tableId +
                ", tableIntegrationId='" + tableIntegrationId + '\'' +
                ", tableName='" + tableName + '\'' +
                ", seats=" + seats +
                '}';
    }
}
