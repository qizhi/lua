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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class BatchHandResponse implements Serializable {

    private final List<TransactionUpdate> resultingBalances;

    public BatchHandResponse() {
        this(new LinkedList<TransactionUpdate>());
    }

    public BatchHandResponse(List<TransactionUpdate> resultingBalances) {
        this.resultingBalances = resultingBalances;
    }

    public void addResultEntry(TransactionUpdate balanceUpdate) {
        resultingBalances.add(balanceUpdate);
    }

    public List<TransactionUpdate> getResultingBalances() {
        return new ArrayList<TransactionUpdate>(resultingBalances);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BatchHandResponse");
        sb.append("{resultingBalances=").append(resultingBalances);
        sb.append('}');
        return sb.toString();
    }
}
