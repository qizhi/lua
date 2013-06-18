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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.operator.service.dao.OperatorDAO;
import com.cubeia.backoffice.operator.service.entity.Operator;
import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;

@Component(value = "operator.operatorManager")
public class OperatorManagerImpl implements OperatorManager {

	Logger log = LoggerFactory.getLogger(getClass());
	
    @Resource(name="operator.operatorDAO")
    private OperatorDAO operatorDAO;
    
    @Autowired
    private OperatorDefaultParams operatorDefault;

    @Override
    public void create(Operator o) {
        if(operatorDAO.getOperator(o.getId())!=null) {
            throw new RuntimeException("Operator id ["+o.getId()+"] already exist. Name ["+o.getName()+"]");
        }
        operatorDefault.addDefaultConfiguration(o.getConfig());
        o.getConfig().put(OperatorConfigParameter.API_KEY, UUID.randomUUID().toString());
        o.getConfig().put(OperatorConfigParameter.REGISTRATION_TIMESTAMP, ISODateTimeFormat.date().print(System.currentTimeMillis()));
        operatorDAO.save(o);
    }

    @Override
    public Operator getOperator(long id) {
        return operatorDAO.getOperator(id);
    }
    
    @Override
    public Operator getOperatorByApiKey(String apiKey) {
        Collection<Operator> operators = operatorDAO.findOperatorsHavingConfigValue(OperatorConfigParameter.API_KEY, apiKey);
        if (operators.size() > 1) {
        	throw new RuntimeException("More than one operators are matching API key["+apiKey+"]. Found: "+operators);
        } else if (operators.size() == 0) {
        	throw new IllegalArgumentException("No operator found for API key["+apiKey+"]");
        }
        return operators.iterator().next();
    }

    @Override
    public List<Operator> getOperators() {
        return operatorDAO.getOperators();
    }

    @Override
    public String getConfig(long operatorId, OperatorConfigParameter parameter) {
        Operator operator = operatorDAO.getOperator(operatorId);
        if(operator!=null) {
            return operator.getConfig(parameter);
        }
        return null;
    }

    @Override
    public void updateOperator(Operator operator) {
        operatorDAO.update(operator);
    }

    @Override
    public boolean isEnabled(Long operatorId) {
        Operator operator = getOperator(operatorId);
        if(operator!=null) {
            return operator.isEnabled();
        }
        return false;
    }


}
