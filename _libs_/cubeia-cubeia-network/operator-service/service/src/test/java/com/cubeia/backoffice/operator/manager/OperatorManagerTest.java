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
package com.cubeia.backoffice.operator.manager;


import com.cubeia.backoffice.operator.service.entity.Operator;
import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;
import com.cubeia.backoffice.operator.service.manager.OperatorManager;
import com.cubeia.backoffice.operator.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class OperatorManagerTest extends BaseTest {

    @Resource(name = "operator.operatorManager")
    OperatorManager operatorManager;

    @Test
    public void testCreateOperator() {
        Operator o = new Operator(1L,"test",true);
        operatorManager.create(o);

        Operator o2  = operatorManager.getOperator(1L);
        assertEquals(o2.getName(),"test");
        assertEquals(o2.getId(),(Long)1L);
    }

    @Test
    public void testListOperators() {
        operatorManager.create(new Operator(1L,"test1",true));
        operatorManager.create(new Operator(2L,"test2",true));
        operatorManager.create(new Operator(3L,"test3",true));

        List<Operator> operators = operatorManager.getOperators();

        Assert.assertEquals(operators.size(), 3);
    }

    @Test
    public void testGetParameterByOperator() {
        operatorManager.create(new Operator(1L,"test1",true));
        operatorManager.create(new Operator(2L,"test2",true));
        Operator operator = operatorManager.getOperator(1L);
        operator.addConfig(OperatorConfigParameter.CLIENT_HELP_URL,"val1");

        String value = operatorManager.getConfig(1L, OperatorConfigParameter.CLIENT_HELP_URL);
        Assert.assertEquals(value, "val1");

        operator = operatorManager.getOperator(2L);
        operator.addConfig(OperatorConfigParameter.CLIENT_HELP_URL,"val2");

        value = operatorManager.getConfig(2L,OperatorConfigParameter.CLIENT_HELP_URL);
        Assert.assertEquals(value, "val2");
    }


}
