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

package com.cubeia.backoffice.users;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserAttribute;
import com.cubeia.backoffice.users.manager.UserManager;

@ContextConfiguration(locations = {"classpath:user-service-app-test.xml"})
@TransactionConfiguration(transactionManager="user.service.transactionManager")
public class UserAttributeTest extends AbstractJUnit4SpringContextTests {

	@PersistenceContext(unitName = "userServicePersistenceUnit")
    protected EntityManager em;
    
    @Autowired
	private UserManager manager;
    
    @After
    public  void tearDown(){
    	for (Long id : manager.findUsersHavingAttributes("test1")){
    		manager.deleteUser(id);
    	}
    }
    
    @Before
    public  void setup(){
    	for (User u : manager.findUsers(null, null, null, -1, -1, null, true).getUsers()){    		
    		manager.deleteUser(u.getId());
    	}
    }
    
    @Test
    @Transactional
    public void updateAllUserAttributes() {
    	String username = "u_"+System.currentTimeMillis();
        User u = new User(username, 1337l);
        u.setPassword("abc");
        u.addAttribute("test1", "test1");
        u.addAttribute("test2", "test2");
        u.addAttribute("test3", "test3");
        manager.createUser(u);
        assertNotNull(u.getId());
        
        User u2 = manager.getUserById(u.getId());
        assertThat(u2.getAttributes().size(), is(3));
        assertNotNull(u2.getAttributes().get("test1").getId());
        assertNotNull(u2.getAttributes().get("test2").getId());
        assertNotNull(u2.getAttributes().get("test3").getId());
        
        Map<String, UserAttribute> attribs = new HashMap<String, UserAttribute>();
        attribs.put("test1", new UserAttribute(u2, "test1", "test1"));
        attribs.put("test4", new UserAttribute(u2, "test4", "test4"));
        u2.setAttributes(attribs);                
        manager.updateUser(u2);
        
        User u3 = manager.getUserById(u.getId());
        assertThat(u3.getAttributes().size(), is(2));
        assertNotNull(u3.getAttributes().get("test1").getId());
        assertNotNull(u3.getAttributes().get("test4").getId());        
    }
}
