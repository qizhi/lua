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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.CreationStatus;
import com.cubeia.backoffice.users.api.dto.Gender;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.api.dto.UserStatus;

public class UserServiceImplTest extends AbstractBaseTest {

    @Autowired
	private UserService service;

//	public void setUserService(UserService service) {
//		this.service = service;
//	}
	
    @Test
    public void createUser() {
		CreateUserRequest userCreateData = new CreateUserRequest("Apan", "pwd", "xid", 1337l);
		
		CreateUserResponse res = service.createUser(userCreateData);
		assertEquals(CreationStatus.OK, res.getStatus());
		User created = res.getUser();
		assertNotNull(created);
		assertNotNull(created.getUserId());
        assertEquals("xid", created.getExternalUserId());
        assertEquals("Apan", created.getUserName());
        assertEquals(new Long(1337l), created.getOperatorId());
        assertEquals(UserStatus.ENABLED, created.getStatus());

		User restored = service.getUserById(created.getUserId());
		assertNotNull(restored);
		assertNotNull(restored.getUserId());
        assertEquals("xid", restored.getExternalUserId());
        assertEquals("Apan", restored.getUserName());
        assertEquals(new Long(1337l), restored.getOperatorId());
        assertEquals(UserStatus.ENABLED, restored.getStatus());
	}
    
    @Test
    public void authenticateUser() {
        CreateUserRequest userCreateData = new CreateUserRequest("Apan2", "pwd2", "xid", 1339l);
        CreateUserResponse res = service.createUser(userCreateData);
        assertEquals(CreationStatus.OK, res.getStatus());
        User created = res.getUser();
        assertNotNull(created);
        
        Long id = service.authenticateUser("Apan2", "pwd2", 1339l);
        assertEquals(created.getUserId(), id);
        
        id = service.authenticateUser("Apan2", "pwd23", 1339l);
        assertNull(id);
        
        id = service.authenticateUser("Apan2", "pwd2", 1340l);
        assertNull(id);
    }

    @Test
    public void createDuplicateUsernames() {
        CreateUserRequest cu1 = new CreateUserRequest("name", null, null, 1337l);
        CreateUserRequest cu2 = new CreateUserRequest("name", null, null, 1337l);
		
        CreateUserResponse created1 = service.createUser(cu1);
		assertNotNull(created1.getUser());
		CreateUserResponse created2 = service.createUser(cu2);
		assertEquals(CreationStatus.USERNAME_ALREADY_EXISTS, created2.getStatus());
		assertNull(created2.getUser());
	}
	
    @Test
	public void getUserByUsername() {
        CreateUserRequest cu = new CreateUserRequest("name", null, null, 1337l);
        CreateUserResponse created = service.createUser(cu);
	    	    
		User found = service.getUserByUserName("name", 1337l);
		assertThat(found, notNullValue());
		assertThat(found.getUserName(), is("name"));
		
		// we should not get a removed user
        service.setUserRemoved(created.getUser().getUserId());
        found = service.getUserByUserName("name", 1337l);
        assertThat(found, nullValue());
	}

    @Test
    public void getUserById() {
        CreateUserRequest cu = new CreateUserRequest("name", null, null, 1337l);
        CreateUserResponse created = service.createUser(cu);
                
        User found = service.getUserById(created.getUser().getUserId());
        assertThat(found, notNullValue());
        assertThat(found.getUserId(), is(created.getUser().getUserId()));
        
        // we should not get a removed user
        service.setUserRemoved(created.getUser().getUserId());
        found = service.getUserById(created.getUser().getUserId());
        assertThat(found, nullValue());
    }
    
    @Test
    public void getUserByExternalId() {
        CreateUserRequest cu = new CreateUserRequest("name", null, "x1223", 1337l);
        CreateUserResponse created = service.createUser(cu);
                
        User found = service.getUserByExternalId("x1223", 1337l);
        assertThat(found, notNullValue());
        assertThat(found.getExternalUserId(), is("x1223"));
        
        // we should not get a removed user
        service.setUserRemoved(created.getUser().getUserId());
        found = service.getUserByExternalId("x1223", 1337l);
        assertThat(found, nullValue());
    }
    
    @Test
    public void updateUser() {
        CreateUserRequest cu = new CreateUserRequest("name", "pwd", null, 1337L);
        CreateUserResponse res = service.createUser(cu);
        assertNotNull(res.getUser());

        User u = res.getUser();
        u.setUserName("ux123");
        u.setExternalUserId("x234");
        u.setOperatorId(23434l);
        u.setStatus(UserStatus.ENABLED);
        UserInformation ui = new UserInformation();
        ui.setCountry("SE");
        ui.setGender(Gender.FEMALE);
        ui.setEmail("abba@snubbe.se");
        ui.setTimeZone("UTC");
        u.setUserInformation(ui);
        
        service.updateUser(u);
        
        User u2 = service.getUserById(u.getUserId());
        assertEquals("ux123", u2.getUserName());
        assertEquals("x234", u2.getExternalUserId());
        assertEquals(new Long(23434l), u2.getOperatorId());
        assertEquals(UserStatus.ENABLED, u2.getStatus());
        assertEquals("SE", u2.getUserInformation().getCountry());
        assertEquals(Gender.FEMALE, u2.getUserInformation().getGender());
        assertEquals("abba@snubbe.se", u2.getUserInformation().getEmail());
        assertEquals("UTC", u2.getUserInformation().getTimeZone());
        
        // password should not have changed
        Long auth = service.authenticateUser("ux123", "pwd", 23434l);
        assertNotNull(auth);
    }
    
    @Test
    public void updatePassword() {
        CreateUserRequest cu = new CreateUserRequest("name", "pwd", null, 1l);
        CreateUserResponse u = service.createUser(cu);
        assertNotNull(u.getUser());

        Long auth = service.authenticateUser("name", "pwd", 1l);
        assertNotNull(auth);
        auth = service.authenticateUser("name", "newpwd", 1l);
        assertNull(auth);
        
        service.updatePassword(u.getUser().getUserId(), "newpwd");
        
        auth = service.authenticateUser("name", "pwd", 1l);
        assertNull(auth);
        auth = service.authenticateUser("name", "newpwd", 1l);
        assertNotNull(auth);
    }
	
    @Test
	public void findUsers() {
        CreateUserRequest cu0 = new CreateUserRequest("a", "pwd", null, 1l);
        CreateUserRequest cu1 = new CreateUserRequest("b", "pwd", null, 1l);
        CreateUserRequest cu2 = new CreateUserRequest("c", "pwd", null, 1l);
        CreateUserRequest cu3 = new CreateUserRequest("d", "pwd", null, 1l);
        User u0 = service.createUser(cu0).getUser();
        u0.getAttributes().put("ROLES", "ADMIN");
        u0.getAttributes().put("enable", "true");
        service.updateUser(u0);
        u0 = service.getUserById(u0.getUserId());
        User u1 = service.createUser(cu1).getUser();
        User u2 = service.createUser(cu2).getUser();
        User u3 = service.createUser(cu3).getUser();
        assertThat(u0, notNullValue());
        assertThat(u1, notNullValue());
        assertThat(u2, notNullValue());
        assertThat(u3, notNullValue());
        
		
		// test wildcards
		UserQueryResult usersResult = service.findUsers(u0.getUserId(), null, "a", 0, 10000, UserOrder.ID, true);
		assertEquals(1, usersResult.getTotalQueryResultSize());
		List<User> users = usersResult.getUsers();
		assertEquals(1, users.size());
		assertThat(users.get(0), is(u0));
		
		usersResult = service.findUsers(null, null, "a", 0, 10000, UserOrder.ID, true);
        assertEquals(1, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(1, users.size());
		assertTrue(users.contains(u0));
		
		usersResult = service.findUsers(null, null, "c", 0, 10000, UserOrder.ID, true);
        assertEquals(1, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(1, users.size());
		assertTrue(users.contains(u2));
		
		usersResult = service.findUsers(u0.getUserId(), null, null, 0, 10000, UserOrder.ID, true);
        assertEquals(1, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(1, users.size());
		assertTrue(users.contains(u0));
		
		// test offset and limit
		usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.ID, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(4, users.size());
		assertTrue(users.contains(u0));
		assertTrue(users.contains(u1));
		assertTrue(users.contains(u2));
		assertTrue(users.contains(u3));
		
		usersResult = service.findUsers(null, null, null, 0, 2, UserOrder.ID, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(2, users.size());
		assertTrue(users.contains(u0));
		assertTrue(users.contains(u1));
		
		usersResult = service.findUsers(null, null, null, 2, 2, UserOrder.ID, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(2, users.size());
		assertTrue(users.contains(u2));
		assertTrue(users.contains(u3));
		
		usersResult = service.findUsers(null, null, null, 4, 2, UserOrder.ID, true);
		assertEquals(4, usersResult.getTotalQueryResultSize());
		users = usersResult.getUsers();
		assertEquals(0, users.size());
		
		usersResult = service.findUsers(null, null, null, 0, 0, UserOrder.ID, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
		assertEquals(0, users.size());
		
		// test statuses
        u0.setStatus(UserStatus.REMOVED);
        u1.setStatus(UserStatus.BLOCKED);
        u2.setStatus(UserStatus.ENABLED);
        u3.setStatus(UserStatus.ENABLED);
        service.updateUser(u0);
        service.updateUser(u1);
        service.updateUser(u2);
        service.updateUser(u3);
        usersResult = service.findUsers(null, null, null, 0, 10, UserOrder.ID, true);
        assertEquals(3, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertTrue(users.contains(u1));
        assertTrue(users.contains(u2));
        assertTrue(users.contains(u3));
	}
	
    @Test
	public void findUsersByName() {
        CreateUserRequest cu0 = new CreateUserRequest("user1", "pwd", null, 1l);
        CreateUserRequest cu1 = new CreateUserRequest("user2", "pwd", null, 1l);
        CreateUserRequest cu2 = new CreateUserRequest("user3", "pwd", null, 1l);
        User u0 = service.createUser(cu0).getUser();
        User u1 = service.createUser(cu1).getUser();
        User u2 = service.createUser(cu2).getUser();
        
        u0.getUserInformation().setFirstName("first1");
        u0.getUserInformation().setLastName("lastB");
        u1.getUserInformation().setFirstName("firstA");
        u1.getUserInformation().setLastName("lastB");
        u2.getUserInformation().setFirstName("firstA");
        u2.getUserInformation().setLastName("lastB");
        service.updateUser(u0);
        service.updateUser(u1);
        service.updateUser(u2);
        
        UserQueryResult users = service.findUsers(null, null, "user2", 0, 100, null, true);
        assertEquals(users.getTotalQueryResultSize(), 1);
        assertEquals(users.getUsers().size(), 1);
        assertEquals(users.getUsers().get(0), u1);

        users = service.findUsers(null, null, "firstA", 0, 100, UserOrder.ID, true);
        assertEquals(users.getTotalQueryResultSize(), 2);
        assertEquals(users.getUsers().size(), 2);
        assertEquals(users.getUsers().get(0), u1);
        assertEquals(users.getUsers().get(1), u2);
        
        users = service.findUsers(null, null, "lastB", 0, 100, UserOrder.ID, true);
        assertEquals(users.getTotalQueryResultSize(), 3);
        assertEquals(users.getUsers().size(), 3);
        assertEquals(users.getUsers().get(0), u0);
        assertEquals(users.getUsers().get(1), u1);
        assertEquals(users.getUsers().get(2), u2);
	}
	
	
    @Test
    public void findUsersOrdering() {
        CreateUserRequest cu0 = new CreateUserRequest("b1", "pwd", null, 1l);
        CreateUserRequest cu1 = new CreateUserRequest("c1", "pwd", null, 1l);
        CreateUserRequest cu2 = new CreateUserRequest("d1", "pwd", null, 1l);
        CreateUserRequest cu3 = new CreateUserRequest("a1", "pwd", null, 1l);
        cu0.getUser().setCreationDate(new Date(100));
        cu1.getUser().setCreationDate(new Date(300));
        cu2.getUser().setCreationDate(new Date(200));
        cu3.getUser().setCreationDate(new Date(400));
        cu0.getUser().setLastLoginDate(new Date(400));
        cu1.getUser().setLastLoginDate(new Date(100));
        cu2.getUser().setLastLoginDate(new Date(300));
        cu3.getUser().setLastLoginDate(new Date(200));
        
        User u0 = service.createUser(cu0).getUser();
        User u1 = service.createUser(cu1).getUser();
        User u2 = service.createUser(cu2).getUser();
        User u3 = service.createUser(cu3).getUser();
        assertNotNull(u0);
        assertNotNull(u1);
        assertNotNull(u2);
        assertNotNull(u3);
        
        UserInformation ui0 = new UserInformation();
        UserInformation ui1 = new UserInformation();
        UserInformation ui2 = new UserInformation();
        UserInformation ui3 = new UserInformation();
        ui0.setCountry("A");
        ui1.setCountry("B");
        ui2.setCountry("C");
        ui3.setCountry("D");
        u0.setUserInformation(ui0);
        u1.setUserInformation(ui1);
        u2.setUserInformation(ui2);
        u3.setUserInformation(ui3);
        
        service.updateUser(u0);
        service.updateUser(u1);
        service.updateUser(u2);
        service.updateUser(u3);
        
        // order by id ascending
        UserQueryResult usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.ID, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        List<User> users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u0, users.get(0));
        assertEquals(u1, users.get(1));
        assertEquals(u2, users.get(2));
        assertEquals(u3, users.get(3));
        
        // order by id descending
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.ID, false);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u3, users.get(0));
        assertEquals(u2, users.get(1));
        assertEquals(u1, users.get(2));
        assertEquals(u0, users.get(3));

        // order by name ascending
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.USER_NAME, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u3, users.get(0));
        assertEquals(u0, users.get(1));
        assertEquals(u1, users.get(2));
        assertEquals(u2, users.get(3));
        
        // order by name descending
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.USER_NAME, false);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u2, users.get(0));
        assertEquals(u1, users.get(1));
        assertEquals(u0, users.get(2));
        assertEquals(u3, users.get(3));
        
        // order by country (which is a sub property of user)
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.COUNTRY, false);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u0, users.get(3));
        assertEquals(u1, users.get(2));
        assertEquals(u2, users.get(1));
        assertEquals(u3, users.get(0));
        
        // order by creation date
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.CREATION_DATE, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u0, users.get(0));
        assertEquals(u2, users.get(1));
        assertEquals(u1, users.get(2));
        assertEquals(u3, users.get(3));
        
        // order by last login date
        usersResult = service.findUsers(null, null, null, 0, 10000, UserOrder.LAST_LOGIN_DATE, true);
        assertEquals(4, usersResult.getTotalQueryResultSize());
        users = usersResult.getUsers();
        assertEquals(4, users.size());
        assertEquals(u1, users.get(0));
        assertEquals(u3, users.get(1));
        assertEquals(u2, users.get(2));
        assertEquals(u0, users.get(3));
    }
    
    @Test
    public void updateAndRemoveAttribute() {
        CreateUserRequest userCreateData = new CreateUserRequest("apan2", "pwd2", "xid2", 2337l);
        CreateUserResponse res = service.createUser(userCreateData);
        assertEquals(CreationStatus.OK, res.getStatus());
        
        User user = res.getUser();        
        user.getAttributes().put("apa", "banan");
        service.updateUser(user);
        
        User user2 = service.getUserById(user.getUserId());
        assertEquals("banan", user2.getAttributes().get("apa"));
        //assertNotNull(user2.getAttributes().get("apa").getId());
        
        user2.getAttributes().put("apa", "cirkus");
        service.updateUser(user2);

        User user3 = service.getUserById(user.getUserId());
        assertEquals("cirkus", user3.getAttributes().get("apa"));
        
        user3.getAttributes().remove("apa");
        service.updateUser(user3);
        
        User user4 = service.getUserById(user.getUserId());
        assertNull(user4.getAttributes().get("apa"));
    }
    
    @Test
    public void autenticateFailureForBlockedUser() {
        CreateUserRequest userCreateData = new CreateUserRequest("apan2", "pwd2", "xid2", 2337l);
        CreateUserResponse res = service.createUser(userCreateData);
        Long authUserId = service.authenticateUser("apan2", "pwd2", 2337l);
        assertThat(authUserId, notNullValue());
        
        service.setUserStatus(res.getUser().getUserId(), UserStatus.BLOCKED);
        
        authUserId = service.authenticateUser("apan2", "pwd2", 2337l);
        assertThat(authUserId, nullValue());
    }
    
    @Test
    public void autenticateFailureForRemovedUser() {
        CreateUserRequest userCreateData = new CreateUserRequest("apan22", "pwd22", "xid22", 2337l);
        CreateUserResponse res = service.createUser(userCreateData);
        Long authUserId = service.authenticateUser("apan22", "pwd22", 2337l);
        assertThat(authUserId, notNullValue());
        
        service.setUserRemoved(res.getUser().getUserId());
        
        authUserId = service.authenticateUser("apan2", "pwd2", 2337l);
        assertThat(authUserId, nullValue());
    }
    
    /*
    @Ignore    
	public void testJackson() throws IOException {       
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
		mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
		mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
        
        CreateUserRequest userCreateData = new CreateUserRequest("apan2", "pwd2", "xid2", 2337l);
        CreateUserResponse res = service.createUser(userCreateData);
        assertEquals(CreationStatus.OK, res.getStatus());
        
        User user = res.getUser();        
        user.getAttributes().put("apa", "banan");
        user.getAttributes().put("apa2", "päron");
        service.updateUser(user);
        
        User user2 = service.getUserById(user.getUserId());
        assertEquals("banan", user2.getAttributes().get("apa"));
        
        mapper.writeValue(System.err, res.getUser());
    } 
    */   
}
