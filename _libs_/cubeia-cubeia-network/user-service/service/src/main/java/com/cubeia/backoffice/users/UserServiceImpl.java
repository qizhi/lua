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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityExistsException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.CreationStatus;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.api.dto.UserType;
import com.cubeia.backoffice.users.domain.UserQueryResultContainer;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.integrations.PluginFactory;
import com.cubeia.backoffice.users.manager.UserManager;
import com.cubeia.backoffice.users.migration.UserMigrationAdapter;
import com.cubeia.backoffice.users.util.DTOFactory;

@Component("user.service.userService")
public class UserServiceImpl implements UserService {
	
    private static final String SESSION_TIMESTAMP = "SESSION_TIMESTAMP";

	private static final String SESSION_TOKEN = "SESSION_TOKEN";

	private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserManager userManager;
    
    private DTOFactory dtoFactory = new DTOFactory();

    private PluginFactory pluginFactory; // wired by spring from xml
    
    private List<UserMigrationAdapter> migrationAdapters = new ArrayList<UserMigrationAdapter>();
    
    @Autowired Configuration config;
    
    public UserServiceImpl() {
	}
    
    @Autowired
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
    
    @Autowired
    public void setMigrationAdapters(List<UserMigrationAdapter> migrationAdapters) {
		this.migrationAdapters = migrationAdapters;
	}
    
    @Autowired
    public void setPluginFactory(PluginFactory pluginFactory) {
		this.pluginFactory = pluginFactory;
	}
    
    public PluginFactory getPluginFactory() {
		return pluginFactory;
	}

    /**
     * Authenticate user towards applicable authentication mechanism
     * 
     */
	@Override
    public Long authenticateUser(String userName, String password, Long operatorId) {
    	UserMigrationAdapter adapter = getValidMigrationAdapter(operatorId);
    	
    	log.debug("authenticating userName[" + userName + "] operatorId[" + operatorId+"] adapter[" + 
    			(adapter == null ? null : adapter.getClass().getSimpleName())
    			+"]");
    	
    	Long userId = null;
    	
    	if (adapter == null) {
    		// No migration adapter is applicable, use default local authentication
    		userId = userManager.authenticateUser(userName, password, operatorId);
    		
    	} else {
    		// Use the defined authentication adapter
    		
    		// Get user by external id and operator id
        	com.cubeia.backoffice.users.entity.User storedUser = userManager.getUserByExternalId(userName, operatorId);
        	
        	if (storedUser == null) {
    			// We have no stored User object so we need to migrate data
        		// Do remote authentication & migration
    			userId = adapter.authenticateAndMigrateUser(userName, password, operatorId);
    			log.info("Migrated data for user " + userId + " from external source.");
        		
        	} else {
        		// We have a local User object already, check where to authenticate
        		if (adapter.useLocalAuthentication()){
        			// Do local authentication
        			userId = userManager.authenticateUser(userName, password, operatorId);
        		} else {
        			// Do remote authentication
        			userId = adapter.authenticateUser(userName, password, operatorId);
        		}
        		
        	}
        	
    	}
    	
    	if (userId != null) {
    		User user = getUserById(userId);
    		updateSessionTokenTimestamp(user, true);
    	}
    	
    	return userId;
    }
	
	@Override
	public Long authenticateUserBySessionToken(String sessionToken) {
		Collection<Long> users = findUsersByAttributeValue(SESSION_TOKEN, sessionToken);
		log.debug("Session["+sessionToken+"] -> Users["+users+"]");
		
		if (users.size() == 1) {
			Long userId = users.iterator().next();

			// Check timestamp
			User user = getUserById(userId);
			String time = user.getAttributes().get(SESSION_TIMESTAMP);
			
			
			if (time == null) {
				log.warn("User["+userId+"] has "+SESSION_TIMESTAMP+" set to null, but still has a "+SESSION_TOKEN);
				
			} else {
				Long timestamp = new Long(time);
				Long now = System.currentTimeMillis();
				Long timeToLive = new Long(config.getProperty("user.service.session.timetolive"));
				
				if (timestamp + timeToLive > now) {
					log.debug("Timestamp for user is valid");
					updateSessionTokenTimestamp(user, false);
					return userId; // Successful authorization
				} else {
					long elapsed = (now - timestamp)/1000;
					log.debug("Timestamp for token is outside the limit. Elapsed: "+elapsed+"s");
				}
				
			}
			
		} else if (users.size() > 1) {
			log.warn("Authentication for session token[] resulted in more than one match("+users.size()+"): "+users);
		}
		
		return null;
	}
	
	@Override
	public void invalidateUserSessionToken(User user) {
		log.debug("Invalidate session for user "+user.getUserId()+":"+user.getUserName());
		user.getAttributes().remove(SESSION_TOKEN);
		user.getAttributes().remove(SESSION_TIMESTAMP);
		updateUser(user);
	}
	
	private void updateSessionTokenTimestamp(User user, boolean createNewToken) {
		// We have a valid authentication, create a session token for this user
		if (createNewToken) {
			user.setAttribute(SESSION_TOKEN, ""+UUID.randomUUID());
		}
		user.setAttribute(SESSION_TIMESTAMP, ""+System.currentTimeMillis());
		updateUser(user);
	}

    @Override
    public CreateUserResponse createUser(CreateUserRequest userData) {
    	// Notify Plugins
    	pluginFactory.beforeCreate(userData.getUser());
        com.cubeia.backoffice.users.entity.User newUser = dtoFactory.createUserEntityByDTO(userData.getUser());
        
        if (newUser.getStatus() == null) {
            newUser.setStatus(com.cubeia.backoffice.users.entity.UserStatus.ENABLED);
        }
        
        newUser.setPassword(userData.getPassword());
        if (newUser.getInformation() == null) {
            newUser.setInformation(new UserInformation());
        }
        
        CreateUserResponse res = new CreateUserResponse();
        try {
            newUser = userManager.createUser(newUser);
            User dto = dtoFactory.createUserDTOByEntity(newUser);
            res.setStatus(CreationStatus.OK);
            res.setUser(dto);
        } catch(EntityExistsException e) {
        	res.setStatus(CreationStatus.USERNAME_ALREADY_EXISTS);
            log.debug("duplicate username / operator");
        } catch (Exception e) {
        	if (e.getCause() instanceof ConstraintViolationException) {
            	res.setStatus(CreationStatus.USERNAME_ALREADY_EXISTS);
                log.debug("possible duplicate username / operator", e);
        	} else {
        		res.setStatus(CreationStatus.INTERNAL_ERROR);
        		log.error("error creating user", e);
        	}
        }
        
        pluginFactory.afterCreate(res.getStatus(), res.getUser());
        
        return res;
    }

    @Override
    public List<CreateUserResponse> createUsers(List<CreateUserRequest> users) {
        ArrayList<CreateUserResponse> createdUsers = new ArrayList<CreateUserResponse>(users.size());
        for (CreateUserRequest u : users) {
            createdUsers.add(createUser(u));
        }
        return createdUsers;
    }
    
    @Override
    public User getUserById(Long userId) {
        com.cubeia.backoffice.users.entity.User user = userManager.getUserById(userId);
        User userDTO = dtoFactory.createUserDTOByEntity(user);
        return userDTO;
    }
    
    @Override
    public User getUserByExternalId(String externalId, Long operatorId) {
    	com.cubeia.backoffice.users.entity.User user = userManager.getUserByExternalId(externalId, operatorId);
        User userDTO = dtoFactory.createUserDTOByEntity(user);
        return userDTO;
    }

    @Override
    public User getUserByUserName(String userName, Long operatorId) {
        com.cubeia.backoffice.users.entity.User user = userManager.getUserByUserName(userName, operatorId);
        User userDTO = dtoFactory.createUserDTOByEntity(user);
        return userDTO;
    }

    @Override
    public void updateUser(User user) {
    	pluginFactory.beforeUpdate(user);
        com.cubeia.backoffice.users.entity.User userEntity = dtoFactory.createUserEntityByDTO(user);
        userManager.updateUser(userEntity);
        pluginFactory.afterUpdate(user);
    }

    @Override
    public String getUserAvatarId(Long userId) {
        return userManager.getUserById(userId).getInformation().getAvatarId();
    }

    @Override
    public void setUserAvatarId(Long userId, String avatarId) {
        com.cubeia.backoffice.users.entity.User userEntity = userManager.getUserById(userId);
        userEntity.getInformation().setAvatarId(avatarId);
        userManager.updateUser(userEntity);
    }

    @Override
    public UserQueryResult findUsers(Long userId, Long operatorId, String name, int offset, int limit, UserOrder order, boolean ascending) {
        log.debug("find users: userId = {}, operatorId = {}, screenName = {}, offset = {}, limit = {}, order = {}, ascending = {}", 
            new Object[] {userId, operatorId, name, new Integer(offset), new Integer(limit), order, ascending});
        
        UserQueryResultContainer result = userManager.findUsers(userId, operatorId, name, offset, 
            limit, dtoFactory.createUserOrderDomainByDTO(order), ascending);
        
        ArrayList<User> userDTOs = new ArrayList<User>();
        for (com.cubeia.backoffice.users.entity.User u : result.getUsers()) {
            userDTOs.add(dtoFactory.createUserDTOByEntity(u));
        }
        
        log.debug("returning {} users, total query result size = {}", userDTOs.size(), result.getTotalQueryResultSize());
        
        UserQueryResult queryResult = new UserQueryResult(offset, limit, result.getTotalQueryResultSize(), 
            userDTOs, order, ascending);
        
        return queryResult;
    }
  
    @Override
    public void setUserStatus(Long userId, UserStatus newStatus) {
        userManager.setUserStatus(userId, com.cubeia.backoffice.users.entity.UserStatus.valueOf(newStatus.name()));
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        log.debug("changing password for user id = {} to '{}'", userId, obfuscatePassword(newPassword)); 
        userManager.updatePassword(userId, newPassword);
    }
    
    @Override
    public void updatePasswordWithVerification(Long userId, String newPassword, String oldPassword) {
    	com.cubeia.backoffice.users.entity.User user = userManager.getUserById(userId);
    	Long id = authenticateUser(user.getUserName(), oldPassword, user.getOperatorId());
    	
    	if(id != null){
    		updatePassword(userId, newPassword);
    	} else {
    		throw new RuntimeException("Failed to verify user " + user.getUserName());
    	}
    }

    private Object obfuscatePassword(String newPassword) {
        if (newPassword == null  ||  newPassword.length() == 0) {
            return null;
        } 
        return newPassword.substring(0, 1) + newPassword.substring(1).replaceAll(".", "\\*");
    }

    @Override
    public Collection<User> findUsersByExample(
            com.cubeia.backoffice.users.api.dto.UserInformation ui) {
        return findUsersByExample(ui, null);        
    }
    
    @Override
    public Collection<User> findUsersByExample(
            com.cubeia.backoffice.users.api.dto.UserInformation ui, Long operatorId) {
    	Collection<com.cubeia.backoffice.users.entity.User> users = userManager.findUsersByExample(
                dtoFactory.createUserInformationEntityByDTO(ui), operatorId); 
        Collection<User> userDTOs = new ArrayList<User>();
        
        for (com.cubeia.backoffice.users.entity.User u : users) {
            userDTOs.add(dtoFactory.createUserDTOByEntity(u));
        }
        
        return userDTOs;
    }

    @Override
    public void setUserRemoved(Long userId) {
        setUserStatus(userId, UserStatus.REMOVED);
    }
    
    @Override
    public void deleteUser(Long userId) {
    	if(userManager.deleteUser(userId)) {
    		pluginFactory.afterDeletion(userId);
    	}
    }

    @Override
    public Collection<User> findUsersByAttributeKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        Collection<com.cubeia.backoffice.users.entity.User> users = userManager.
            findUsersByAttributeKey(key); 
        Collection<User> userDTOs = new ArrayList<User>();
        
        for (com.cubeia.backoffice.users.entity.User u : users) {
            userDTOs.add(dtoFactory.createUserDTOByEntity(u));
        }
        
        return userDTOs;
    }

	@Override
	public Collection<Long> findUsersByAttributeValue(String key, String value) {
		if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
		return userManager.findUsersByAttributeValue(key, value);
	}

	@Override
	public Collection<Long> findUsersHavingAnyOfAttributes(String... keys) {
		if (keys == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
		return userManager.findUsersHavingAttributes(keys);
	}

	/*
	@Override
	public Collection<Long> findUsersNotHavingAttributes(String... keys) {
		return userManager.findUsersNotHavingAttributes(keys);
	}
	*/

	@Override
	public Collection<User> getUsersWithIds(Collection<Long> userIds) {
		Collection<com.cubeia.backoffice.users.entity.User> users = userManager.
				getUsersWithIds(userIds); 
	    Collection<User> userDTOs = new ArrayList<User>();
	    
	    for (com.cubeia.backoffice.users.entity.User u : users) {
	        userDTOs.add(dtoFactory.createUserDTOByEntity(u));
	    }
	    
	    return userDTOs;
	}
	
	@Override
	public Collection<User> findUsersWithUserType(UserType... userTypes) {
		Collection<com.cubeia.backoffice.users.entity.UserType> typeEntities = new ArrayList<com.cubeia.backoffice.users.entity.UserType>();
		for (UserType dtoType : userTypes) {
			com.cubeia.backoffice.users.entity.UserType entityType = com.cubeia.backoffice.users.entity.UserType.valueOf(dtoType.name());
			typeEntities.add(entityType);
		}
		
		Collection<com.cubeia.backoffice.users.entity.User> users = userManager.getUsersWithUserType(typeEntities);
		Collection<User> userDTOs = new ArrayList<User>();
		
		for (com.cubeia.backoffice.users.entity.User u : users) {
		    userDTOs.add(dtoFactory.createUserDTOByEntity(u));
		}
		
		return userDTOs;
	}
	
	private UserMigrationAdapter getValidMigrationAdapter(Long operatorId){
		for(UserMigrationAdapter adapter : migrationAdapters) {
			// log.debug("Check if migration adapter '"+adapter.getClass().getSimpleName()+"' is applicable for operator id "+operatorId);	
			if(adapter.applicable(operatorId)) {
				log.debug("External migration/authentication found and will user adapter: "+adapter.getClass().getSimpleName()+" for operatorId "+operatorId);	
				return adapter;
			}
		}
		return null;
	}
}