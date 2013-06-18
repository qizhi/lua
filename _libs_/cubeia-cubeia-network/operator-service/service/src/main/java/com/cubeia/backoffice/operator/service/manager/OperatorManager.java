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
package com.cubeia.backoffice.operator.service.manager;

import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;
import com.cubeia.backoffice.operator.service.entity.Operator;

import java.util.List;

/**
 * Manager for operator configuration
 */
public interface OperatorManager {

    /**
     * Creates an operator
     * @param o
     */
    void create(Operator o);

    /**
     * Retrieves an operator
     * @param id
     * @return
     */
    Operator getOperator(long id);

    /**
     * Retrieves a list of all operators
     * @return
     */
    List<Operator> getOperators();

    /**
     * Get a specific config parameter value of a specific operator
     * @param operatorId
     * @param test
     * @return
     */
    String getConfig(long operatorId, OperatorConfigParameter test);

    /**
     * updates an operator
     * @param operator
     */
    void updateOperator(Operator operator);

    /**
     * Checks whether a operator is enabled or not
     * @param operatorId
     * @return
     */
    boolean isEnabled(Long operatorId);

	Operator getOperatorByApiKey(String apiKey);
}
