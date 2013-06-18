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
import com.cubeia.backoffice.operator.service.entity.Operator;
import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;
import com.cubeia.backoffice.operator.service.manager.OperatorManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cubeia.backoffice.operator.api.OperatorConfigParamDTO.*;

@Component("operator.operatorService")
public class OperatorServiceImpl implements OperatorService {

    @Resource(name="operator.operatorManager")
    OperatorManager operatorManager;

    @Override
    public void createOperator(OperatorDTO operator) {
        Operator o = new Operator();
        updateOperatorEntity(operator, o);
        operatorManager.create(o);
    }

    @Override
    public void updateOperator(OperatorDTO operator) {
    	Operator op = operatorManager.getOperator(operator.getId());
    	updateOperatorEntity(operator, op);
    	operatorManager.updateOperator(op);
    }

    private void updateOperatorEntity(OperatorDTO operatorDTO, Operator operator) {
        operator.setId(operatorDTO.getId());
        operator.setName(operatorDTO.getName());
        operator.setEnabled(operatorDTO.isEnabled());
        operator.setAccountStatus(operatorDTO.getAccountStatus());
    }

    @Override
    public OperatorDTO getOperator(Long operatorId) {
        Operator operator = operatorManager.getOperator(operatorId);
        return extractOperatorDTO(operator);
    }

    @Override
	public OperatorDTO getOperatorByApiKey(String apiKey) {
    	Operator operator = operatorManager.getOperatorByApiKey(apiKey);
    	return extractOperatorDTO(operator);
	}
    
    private OperatorDTO extractOperatorDTO(Operator operator) {
        if (operator == null) {
            return null;
        }
        OperatorDTO operatorDTO = new OperatorDTO();
        operatorDTO.setId(operator.getId());
        operatorDTO.setName(operator.getName());
        operatorDTO.setEnabled(operator.isEnabled());
        operatorDTO.setAccountStatus(operator.getAccountStatus());
        return operatorDTO;
    }

    @Override
    public List<OperatorDTO> getOperators() {
        List<Operator> operators = operatorManager.getOperators();
        return extractOperatorDTOs(operators);
    }

    @Override
    public Map<OperatorConfigParamDTO,String> getOperatorConfigList(Long operatorId) {
        Operator operator = operatorManager.getOperator(operatorId);
        if(operator==null) {
            throw new RuntimeException("Operator not found");
        }

        Map<OperatorConfigParameter, String> config = operator.getConfig();
        Map<OperatorConfigParamDTO,String> configDTO = new HashMap<OperatorConfigParamDTO, String>(config.size());

        for(OperatorConfigParameter key :  config.keySet()) {
            configDTO.put(OperatorConfigParamDTO.valueOf(key.name()), config.get(key));
        }
        return configDTO;
    }

    private List<OperatorDTO> extractOperatorDTOs(List<Operator> operators) {
        List<OperatorDTO> list = new ArrayList<OperatorDTO>();
        for(Operator o : operators) {
            list.add(extractOperatorDTO(o));
        }
        return list;
    }

    @Override
    public String getOperatorConfig(Long operatorId, OperatorConfigParamDTO param) {
       return operatorManager.getConfig(operatorId,OperatorConfigParameter.valueOf(param.name()));
    }

    @Override
    public void saveConfig(Long operatorId, OperatorConfigParamDTO param, String value) {
        Operator operator = operatorManager.getOperator(operatorId);
        if(operator == null) {
            throw new RuntimeException("Operator ["+operatorId+"] not found");
        }
        OperatorConfigParameter configParam = OperatorConfigParameter.valueOf(param.name());
        operator.getConfig().put(configParam,value);
        operatorManager.updateOperator(operator);
    }

    @Override
    public boolean isEnabled(Long operatorId) {
        return operatorManager.isEnabled(operatorId);
    }

	@Override
	public Map<OperatorConfigParamDTO, String> getOperatorConfigListForClients(Long operatorId) {
		Map<OperatorConfigParamDTO, String> clientConfig = new HashMap<OperatorConfigParamDTO, String>();
		Map<OperatorConfigParamDTO, String> allConfig = getOperatorConfigList(operatorId);
		
		clientConfig.put(CLIENT_HELP_URL, allConfig.get(OperatorConfigParamDTO.CLIENT_HELP_URL));
		clientConfig.put(CLIENT_HOME_URL, allConfig.get(OperatorConfigParamDTO.CLIENT_HOME_URL));
		clientConfig.put(CSS_URL, allConfig.get(OperatorConfigParamDTO.CSS_URL));
		clientConfig.put(LOGOUT_PAGE_URL, allConfig.get(OperatorConfigParamDTO.LOGOUT_PAGE_URL));
		clientConfig.put(PROFILE_PAGE_URL, allConfig.get(OperatorConfigParamDTO.PROFILE_PAGE_URL));
        clientConfig.put(BUY_CREDITS_URL, allConfig.get(OperatorConfigParamDTO.BUY_CREDITS_URL));
        clientConfig.put(ACCOUNT_INFO_URL, allConfig.get(OperatorConfigParamDTO.ACCOUNT_INFO_URL));
        clientConfig.put(SHARE_URL, allConfig.get(OperatorConfigParamDTO.SHARE_URL));
		
		return clientConfig;
	}
}