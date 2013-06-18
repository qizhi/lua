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
package com.cubeia.backoffice.operator.service;


import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.api.OperatorDTO;

import java.util.List;
import java.util.Map;


public interface OperatorService {

    /**
     * Creates an operator
     * @param operator
     */
    void createOperator(OperatorDTO operator);

    void updateOperator(OperatorDTO operator);

    /**
     * Retrieves operator containing basic info without any settings
     * @param operatorId
     * @return
     */
    OperatorDTO getOperator(Long operatorId);
    
    /**
     * Retrieves operator from API Key containing basic info without any settings
     * 
     * @param apiKey
     * @return
     */
    OperatorDTO getOperatorByApiKey(String apiKey);

    /**
     * Retrieves a list of all available operators
     * @return
     */
    List<OperatorDTO> getOperators();

    /**
     * Returns a map of all configuration parameter and values currently set
     * on a operator
     * @param operatorId - the id of the operator
     * @return
     */
    Map<OperatorConfigParamDTO,String> getOperatorConfigList(Long operatorId);

    /**
     * Retrieves the value for a specific config parameter of a specific operator
     * @param operatorId for which operator to retrieve the config value
     * @param param the parameter name of the config to retrieve
     * @return
     */
    String getOperatorConfig(Long operatorId, OperatorConfigParamDTO param);

    /**
     * Saves a configuration value for a specific operator
     * @param operatorId for which operator to save the parameter value
     * @param param the parameter to save
     * @param value the value associated to the parameter
     */
    void saveConfig(Long operatorId, OperatorConfigParamDTO param, String value);

    /**
     * Checks whether the operator is enabled or not
     * @param operatorId
     * @return
     */
    boolean isEnabled(Long operatorId);

	Map<OperatorConfigParamDTO, String> getOperatorConfigListForClients(Long operatorId);
}
