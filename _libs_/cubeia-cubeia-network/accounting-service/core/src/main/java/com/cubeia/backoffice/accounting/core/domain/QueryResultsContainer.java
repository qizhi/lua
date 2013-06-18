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

package com.cubeia.backoffice.accounting.core.domain;

import java.util.List;

/**
 * A holder for query results and total result set size (without size limit).
 * @author w
 *
 */
public class QueryResultsContainer<T> {
	
    private List<T> results;
    private int totalQueryResultSize;
    
    public QueryResultsContainer(int size, List<T> results) {
        totalQueryResultSize = size;
        this.results = results;
    }

    /**
     * List of entities returned by the query and limited to the offset and size limit.
     * @return users
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * The total query result size without limits.
     * @return the total result size
     */
    public int getTotalQueryResultSize() {
        return totalQueryResultSize;
    }
}
