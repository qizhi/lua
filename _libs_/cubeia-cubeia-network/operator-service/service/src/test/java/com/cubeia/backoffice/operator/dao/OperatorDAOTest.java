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
package com.cubeia.backoffice.operator.dao;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.cubeia.backoffice.operator.BaseTest;
import com.cubeia.backoffice.operator.api.OperatorAccountStatus;
import com.cubeia.backoffice.operator.service.dao.OperatorDAO;
import com.cubeia.backoffice.operator.service.entity.Operator;
import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;

public class OperatorDAOTest extends BaseTest {

    @Resource(name = "operator.operatorDAO")
    OperatorDAO operatorDAO;

    @Test
    public void testCreateOperator() {
        Operator o =  operatorDAO.save(new Operator(1L, "test operator", true));
        o.setAccountStatus(OperatorAccountStatus.TRIAL);
        Assert.assertNotNull(o.getId());
        
        Operator operator = operatorDAO.getOperator(1L);
        assertThat(operator.getId(), is(1L));
        assertThat(operator.getAccountStatus(), is(OperatorAccountStatus.TRIAL));
        
    }
    
    
    @Test
    public void testGetOperator() {
        operatorDAO.save(new Operator(1L, "test operator", true));
        Operator operator = operatorDAO.getOperator(1L);

        assertEquals("test operator",operator.getName());
        assertEquals((Long) 1L, operator.getId());
        Assert.assertTrue(operator.isEnabled());
    }
    
    @Test
    public void testGetOperatorFromApiKey() {
        Operator o1 = new Operator(1L, "test operator", true);
        o1.addConfig(OperatorConfigParameter.API_KEY, "abc");
		operatorDAO.save(o1);
		
		Operator o2 = new Operator(2L, "big bird", true);
        o2.addConfig(OperatorConfigParameter.API_KEY, "foo");
		operatorDAO.save(o2);
		
        Collection<Operator> op = operatorDAO.findOperatorsHavingConfigValue(OperatorConfigParameter.API_KEY, "foo");
        assertThat(op.size(), is(1));
        assertThat(op.iterator().next().getId(), is(2L));
    }

    @Test
    public void testUpdateOperator() {
        operatorDAO.save(new Operator(1L, "test operator", true));

        Operator operator = operatorDAO.getOperator(1L);

        assertEquals("test operator", operator.getName());

        operator.setName("test operator updated");
        operator.setEnabled(false);

        operatorDAO.save(operator);
        operator = operatorDAO.getOperator(1L);

        assertEquals("test operator updated",operator.getName());
        Assert.assertFalse(operator.isEnabled());
    }

    @Test
    public void testSetConfig()  {
        operatorDAO.save(new Operator(1L, "test operator", true));
        Operator operator = operatorDAO.getOperator(1L);
        operator.addConfig(OperatorConfigParameter.CLIENT_HELP_URL,"test value");

        operatorDAO.save(operator);
        Operator o = operatorDAO.getOperator(1L);

        assertEquals(o.getConfig(OperatorConfigParameter.CLIENT_HELP_URL),"test value");


        o.addConfig(OperatorConfigParameter.CLIENT_HELP_URL,"new test value");
        operatorDAO.save(o);

        Operator updated = operatorDAO.getOperator(1L);

        assertEquals(updated.getConfig(OperatorConfigParameter.CLIENT_HELP_URL),"new test value");


    }

}
