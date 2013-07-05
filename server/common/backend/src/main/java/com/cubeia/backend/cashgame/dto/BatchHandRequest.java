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
import com.cubeia.games.poker.common.money.Money;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class BatchHandRequest implements Serializable {

    private final String handId;
    private final TableId tableId;
    private final List<HandResult> handResults;
    private final Money totalRake;

    private long startTime;
    private long endTime;

    public BatchHandRequest(String handId, TableId tableId, Money totalRake) {
        this(handId, tableId, new LinkedList<HandResult>(), totalRake);
    }

    public BatchHandRequest(String handId, TableId tableId, List<HandResult> handResults, Money totalRake) {
        this.handId = handId;
        this.tableId = tableId;
        this.handResults = handResults;
        this.totalRake = totalRake;
    }

    public void addHandResult(HandResult handResult) {
        handResults.add(handResult);
    }

    public String getHandId() {
        return handId;
    }

    public TableId getTableId() {
        return tableId;
    }

    public List<HandResult> getHandResults() {
        return new ArrayList<HandResult>(handResults);
    }

    public Money getTotalRake() {
        return totalRake;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BatchHandRequest");
        sb.append("{handId='").append(handId).append('\'');
        sb.append(", tableId=").append(tableId);
        sb.append(", handResults=").append(handResults);
        sb.append(", totalRake=").append(totalRake);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append('}');
        return sb.toString();
    }
}
