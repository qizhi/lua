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
package com.cubeia.backoffice.users.client;

import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.backoffice.operator.client.OperatorServiceClientHTTP;

import java.util.List;
import java.util.Map;

public class Runner {

	/**
	 * Executes most of the client methods against a running operator web service.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        OperatorServiceClient client = new OperatorServiceClientHTTP();
        List<OperatorDTO> operators = client.getOperators();
        System.out.println("List all operators: "+operators);
        
        Long operatorId = operators.get(0).getId();
        OperatorDTO operator = client.getOperator(operatorId);
        System.out.println("Loaded operator by id("+operatorId+"): "+operator);
        
        Map<OperatorConfigParamDTO, String> config = client.getConfig(operatorId);
        System.out.println("Config id("+operatorId+"): "+config);
        
        config.put(OperatorConfigParamDTO.INTEGRATION_URL, "url1");
        client.updateConfig(operatorId, config);
        System.out.println("Config updated");
        config = client.getConfig(operatorId);
        System.out.println("Config auth url id("+operatorId+") -> url1: "+config.get(OperatorConfigParamDTO.INTEGRATION_URL));
        
        config.put(OperatorConfigParamDTO.INTEGRATION_URL, "url1");
        client.updateConfig(operatorId, OperatorConfigParamDTO.INTEGRATION_URL, "url2");
        System.out.println("Config updated with url2");
        String value = client.getConfig(operatorId, OperatorConfigParamDTO.INTEGRATION_URL);
        System.out.println("Config auth url id("+operatorId+") -> url2: "+value);
        
        boolean enabled = client.isEnabled(operatorId);
        System.out.println("Operator("+operatorId+") enabled: "+enabled);
        
        Map<OperatorConfigParamDTO, String> clientConfig = client.getClientConfig(0L);
        System.out.println("1 Client config: "+clientConfig);
        
    }

}
