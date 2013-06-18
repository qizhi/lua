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
package com.cubeia.backoffice.users.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Collection;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cubeia.backoffice.users.AbstractBaseTest;
import com.cubeia.backoffice.users.entity.Gender;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.entity.UserStatus;
import com.cubeia.backoffice.users.entity.UserType;

public class UserManagerImplTest extends AbstractBaseTest {

    @PersistenceContext(unitName = "userServicePersistenceUnit")
    protected EntityManager em;
    
    @Autowired
	private UserManager manager;
	
	@Test
	public void createUser() {
		assertNotNull(manager);
		User user = new User();
		user.setUserName("Apan");
		user.setOperatorId(1337l);
		user.setDecrypedPassword("knark");
		
		User created = manager.createUser(user);
		assertNotNull(created);
		assertNotNull(created.getId());

		User restored = manager.getUserById(created.getId());
		assertNotNull(restored);
		assertNotNull(restored.getId());

		assertEquals(restored.getId(), created.getId());
		assertEquals(restored.getUserName(), created.getUserName());
		assertEquals(restored.getDecrypedPassword(), "knark");
	}

    @Test
	public void createUserWithInformation() {
		assertNotNull(manager);
		User user = new User("InfoUser", 1337L);
		UserInformation info = new UserInformation();
		info.setBillingAddress("Some Street 23");
		info.setGender(Gender.FEMALE);
		user.setInformation(info);

		User created = manager.createUser(user);

		assertNotNull(created);
		assertNotNull(created.getId());

		User restored = manager.getUserById(created.getId());
		assertNotNull(restored);
		assertNotNull(restored.getId());
		assertNotNull(restored.getInformation());

		assertEquals(restored.getId(), created.getId());
		assertEquals(restored.getUserName(), created.getUserName());
		assertEquals(restored.getInformation().getBillingAddress(), "Some Street 23");
		assertEquals(restored.getInformation().getGender(), Gender.FEMALE);
		
		manager.deleteUser(created.getId());
		
		User removed = manager.getUserById(created.getId());
		assertNull(removed);
	}
	
    @Test
	public void createDuplicateUsernames() {
		String username = "u_"+System.currentTimeMillis();
		
		User user1 = new User("U1", 1337l);
		user1.setUserName(username);
		
		User user2 = new User("U2", 1337l);
		user2.setUserName(username);
		
		User created1 = manager.createUser(user1);
		assertNotNull(created1);
		try {
			manager.createUser(user2);
			fail();
		} catch (PersistenceException e) {
			// expected
		}
	}
	
    @Test
	public void getByUsername() {
		String username = "u_"+System.currentTimeMillis();
		User user1 = new User(username, 1337l);
		
		User created1 = manager.createUser(user1);
		assertNotNull(created1);
		
		User found = manager.getUserByUserName(username, 1337l);
		assertNotNull(found);
		assertEquals(found.getUserName(), username);
		
	}
	
    @Test
	public void getByExternalId() {
		String username = "u_"+System.currentTimeMillis();
		User user1 = new User(username, 1337l);
		user1.setExternalId("kalle");
		
		User created1 = manager.createUser(user1);
		assertNotNull(created1);
		
		User found = manager.getUserByExternalId("kalle", 1337l);
		assertNotNull(found);
		assertEquals(found.getUserName(), username);
		
	}

    @Test
    public void updateUser() {
        String username = "u_"+System.currentTimeMillis();
        User u = new User(username, 1337l);
        u.setPassword("abc");
        
        manager.createUser(u);
        assertNotNull(u.getId());

        User u2 = manager.getUserById(u.getId());
        assertEquals("abc", u2.getDecrypedPassword());
        
        u2.setUserName("ux123");
        manager.updateUser(u2);
        
        User u3 = manager.getUserById(u.getId());
        assertEquals("abc", u3.getDecrypedPassword());
        assertEquals("ux123", u3.getUserName());
    }
    
    @Test
    public void updatePassword() {
        String username = "u_"+System.currentTimeMillis();
        User u = new User(username, 1337l);
        u.setPassword("def");
        manager.createUser(u);
        assertNotNull(u.getId());
        
        manager.updatePassword(u.getId(), "newpwd");
        
        User u2 = manager.getUserById(u.getId());
        assertEquals(username, u2.getUserName());
        assertEquals("newpwd", u2.getDecrypedPassword());
    }
    
    @Test
    public void findUsersByExample() {
        User user = new User("u-334", 1337l);
        UserInformation info = new UserInformation();
        info.setGender(Gender.FEMALE);
        info.setPhone("1234");
        info.setCountry("SE");
        user.setInformation(info);
        User created = manager.createUser(user);
        assertNotNull(created);
        assertNotNull(created.getId());
        
        UserInformation uix = new UserInformation();
        uix.setPhone("1234");
        Collection<User> users = manager.findUsersByExample(uix, null);
        assertEquals(1, users.size());
        assertEquals(created.getId(), users.iterator().next().getId());
        
        uix = new UserInformation();
        uix.setPhone("3434");
        users = manager.findUsersByExample(uix, null);
        assertEquals(0, users.size());
        
        uix = new UserInformation();
        uix.setGender(Gender.FEMALE);
        uix.setPhone("1234");
        users = manager.findUsersByExample(uix, null);
        assertEquals(1, users.size());
        assertEquals(created.getId(), users.iterator().next().getId());
        
        uix = new UserInformation();
        uix.setPhone("1234");
        uix.setGender(Gender.MALE);
        users = manager.findUsersByExample(uix, null);
        assertEquals(0, users.size());
        
        User user2 = new User("u-33434", 1337l);
        UserInformation info2 = new UserInformation();
        info2.setGender(Gender.FEMALE);
        user2.setInformation(info2);
        manager.createUser(user2);
        
        uix = new UserInformation();
        uix.setGender(Gender.FEMALE);
        users = manager.findUsersByExample(uix, null);
        assertEquals(2, users.size());
    }
    
    @Test
    public void findUsersByExampleExcludeRemoved() {
        User user0 = new User("u-137", 1437l);
        UserInformation info0 = new UserInformation();
        info0.setPhone("1234");
        user0.setInformation(info0);
        User created0 = manager.createUser(user0);
        
        User user1 = new User("u-138", 1437l);
        UserInformation info1 = new UserInformation();
        info1.setPhone("1234");
        user1.setInformation(info1);
        User created1 = manager.createUser(user1);
        
        UserInformation uix = new UserInformation();
        uix.setPhone("1234");
        Collection<User> users = manager.findUsersByExample(uix, null);
        assertThat(users.size(), is(2));
        
        manager.setUserStatus(created1.getId(), UserStatus.REMOVED);
        
        users = manager.findUsersByExample(uix, null);
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(created0));
    }    
    
    @Test
    public void findUsersByExampleExcludeNonUsers() {
        User user0 = new User("u-137", 1437l);
        UserInformation info0 = new UserInformation();
        info0.setPhone("1234");
        user0.setInformation(info0);
        User created0 = manager.createUser(user0);
        
        User user1 = new User("u-138", 1437l);
        UserInformation info1 = new UserInformation();
        info1.setPhone("1234");
        user1.setInformation(info1);
        User created1 = manager.createUser(user1);
        
        UserInformation uix = new UserInformation();
        uix.setPhone("1234");
        Collection<User> users = manager.findUsersByExample(uix, null);
        assertThat(users.size(), is(2));
        
        created1.setUserType(UserType.OPERATOR);
        manager.updateUser(created1);
        
        users = manager.findUsersByExample(uix, null);
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(created0));
    }    
    
    @Test
    public void addUserAttributes() {
        User u = new User("ua-112", 0l);
        u.addAttribute("apa", "banan");
        User uc = manager.createUser(u);
        assertNotNull(uc);
        assertNotNull(uc.getId());
        assertEquals("banan", uc.getAttributeValue("apa"));
        
        User u2 = new User("ua-113", 0l);
        u2.addAttribute("apa", "banan2");
        u2.addAttribute("knark", "tjack");
        User u2c = manager.createUser(u2);
        assertEquals("banan2", u2c.getAttributeValue("apa"));
        assertEquals("tjack", u2c.getAttributeValue("knark"));
    }
    
    @Test
    public void removeUserAttributes() {
        User u = new User("ua-112342", 0l);
        u.addAttribute("apa", "banan");
        User uc = manager.createUser(u);
        assertNotNull(uc);
        assertNotNull(uc.getId());
        assertEquals("banan", uc.getAttributeValue("apa"));

        u.getAttributes().remove("apa");
        manager.updateUser(u);
        
        User u2 = manager.getUserById(u.getId());
        assertNull(u2.getAttributeValue("apa"));
    }
    
    @Test
    public void findUsersByAttributeKey() {
        Collection<User> users = manager.findUsersByAttributeKey("admin");
        assertThat(users.size(), notNullValue());
        
        User u = new User("ua-22112342", 0l);
        u.addAttribute("admin", "true");
        u = manager.createUser(u);
        
        users = manager.findUsersByAttributeKey("admin");
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(u));
        assertThat(users.iterator().next().getAttributeValue("admin"), is("true"));
        
        User u2 = new User("ua-22112343", 0l);
        u2.addAttribute("admin", "admin2");
        u2 = manager.createUser(u2);
        
        users = manager.findUsersByAttributeKey("admin");
        assertThat(users.size(), is(2));
        assertThat(users, hasItems(u, u2));
        
        users = manager.findUsersByAttributeKey("adminXXX");
        assertThat(users.size(), is(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findUsersByAttributeKeyNullValue() {
        manager.findUsersByAttributeKey(null);
    }
    
    @Test
    public void findUsersByAttribute() {
        Collection<Long> users = manager.findUsersHavingAttributes("admin");
        assertThat(users.size(), notNullValue());
        
        User u = new User("ua-22112342", 0l);
        u.addAttribute("admin", "true");
        // Causes findUsersNotHavingAttributes to bug out. Put back when fixed!!!
        //u.addAttribute("test", "test");
        u = manager.createUser(u);
        
        users = manager.findUsersHavingAttributes("admin");
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(u.getId()));
                
        User u2 = new User("ua-22112343", 0l);
        u2.addAttribute("admin", "admin2");
        u2 = manager.createUser(u2);
        
        users = manager.findUsersHavingAttributes("admin");
        assertThat(users.size(), is(2));
        assertThat(users, hasItems(u.getId(), u2.getId()));
        
        users = manager.findUsersNotHavingAttributes("admin");
        assertThat(users.size(), is(0));
        
        User u3 = new User("ua-22112344", 0l);
        u3.addAttribute("manager", "manager");
        u3 = manager.createUser(u3);
        
        users = manager.findUsersHavingAttributes("admin","manager");
        assertThat(users.size(), is(3));
        
        User u4 = new User("ua-22112345", 0l);        
        u4 = manager.createUser(u4);
        
        users = manager.findUsersNotHavingAttributes("manager");
        assertThat(users.size(), is(2));
        
        User u5 = new User("ua-22112346", 0l);        
        u5 = manager.createUser(u5);
        u5.addAttribute("manager", "manager");
        u5.addAttribute("admin", "admin");
        users = manager.findUsersHavingAttributes("manager", "admin");
        assertThat(users.size(), is(4));
        
// FIXME: By setting UserType to SYSTEM this user should not be included in the search
//        u5.setUserType(UserType.SYSTEM);
//        manager.updateUser(u5);
//        users = manager.findUsersHavingAttributes("manager", "admin");
//        assertThat(users.size(), is(3));
    }
    
    @Test
    public void findUsersByAttributeValue() {
        Collection<Long> users = manager.findUsersByAttributeValue("admin", "true");
        assertThat(users.size(), notNullValue());
        
        User u = new User("ua-22112342", 0l);
        u.addAttribute("admin", "true");
        u = manager.createUser(u);
        
        users = manager.findUsersByAttributeValue("admin", "true");
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(u.getId()));
        
        users = manager.findUsersByAttributeValue("admin", "false");
        assertThat(users.size(), is(0));       
        
        User u2 = new User("ua-22112343", 0l);
        u2.addAttribute("admin", null);
        u2 = manager.createUser(u2);
        
        users = manager.findUsersByAttributeValue("admin", null);
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next(), is(u2.getId()));
    }
    
    @Test
    public void getUsersWithIds() {
    	 manager.findUsersHavingAttributes("admin");
        User u = new User("ua-22112342", 0l);
        u.addAttribute("admin", "true");
        u = manager.createUser(u);
        
        User u2 = new User("ua-22112343", 0l);
        u2.addAttribute("admin", null);
        u2 = manager.createUser(u2);
        
        Collection<Long> userIds = manager.findUsersHavingAttributes("admin");
        assertThat(userIds.size(), is(2));        
        
        Collection<User> users = manager.getUsersWithIds(userIds);
        assertThat(users.size(), is(2));
    }
}
