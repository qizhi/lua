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

import static com.cubeia.backoffice.users.entity.UserStatus.BLOCKED;
import static com.cubeia.backoffice.users.entity.UserStatus.ENABLED;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cubeia.backoffice.users.dao.BlockingDAO;
import com.cubeia.backoffice.users.entity.*;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.users.Configuration;
import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.dao.UserDAO;
import com.cubeia.backoffice.users.domain.UserQueryResultContainer;
import com.cubeia.backoffice.users.phonelookup.PhoneLookup;

/**
 * This class is Transactional through AOP advice set in the Spring configuration.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class UserManagerImpl implements UserManager {
	
	private static Logger log = LoggerFactory.getLogger(UserManagerImpl.class);
	
	private UserDAO userDAO;

    private BlockingDAO blockingDAO;

    private StandardPBEStringEncryptor encoder;
    
    private PhoneLookup phoneLookup;
    
    private Configuration config;

	/**
	 * Injected by Spring
	 * @param userDAO
	 */
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

    /**
     * Injected by Spring
     * @param blockingDAO
     */
    public void setBlockingDAO(BlockingDAO blockingDAO) {
        this.blockingDAO = blockingDAO;
    }
	
    public void setEncryption(StandardPBEStringEncryptor encoder) {
        this.encoder = encoder;
    }
	
	@Override
	public User getUserById(Long id) {
		User user = userDAO.getById(id);

		if (user == null  ||  user.getStatus() == UserStatus.REMOVED) {
            return null;
        }
        
        return user;
	}
	
	@Override
	public User getUserByExternalId(String externalId, Long operatorId) {
		User user = userDAO.getByExternalId(externalId, operatorId);
		
        if (user == null  ||  user.getStatus() == UserStatus.REMOVED) {
            return null;
        }
        
        return user;
	}
	
	@Override
	public User getUserByUserName(String userName, Long operatorId) {
		User user = userDAO.getByUserName(userName, operatorId);
	
		if (user == null  ||  user.getStatus() == UserStatus.REMOVED) {
		    return null;
		}
		
        return user;
	}


	@Override
	public User createUser(User user) {
	    if (user == null) {
	        throw new IllegalArgumentException("user was null");
	    }
	    
	    if (user.getOperatorId() == null) {
	        throw new NullPointerException("operator id must be set on user");
	    }
	    
		userDAO.persist(user);
		return user;
	}
	
	@Override
	public boolean deleteUser(Long userId) {
		User user = userDAO.getById(userId);
		if (user != null) {
			userDAO.delete(user);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Long authenticateUser(String userName, String password, Long operatorId) {
	    User user = getUserByUserName(userName, operatorId);
	    
	    if (user == null) {
	        log.debug("authentication failed for '{}' with operatorId '{}', user not found", userName, operatorId);
	        return null;
	    } 
	    
	    String decPwd = user.getDecrypedPassword();
	    
        if (decPwd != null  &&  decPwd.equals(password)  &&  user.getStatus() == ENABLED  &&  user.getUserType() == UserType.USER) {
            return user.getId();
        } else {
            log.debug("authentication failed for '{}' with operatorId '{}'", userName, operatorId);
            return null;
        }
	}

	@Override
	public UserQueryResultContainer findUsers(Long userId, Long operatorId, String name, int offset, int limit, 
	        UserOrder order, boolean ascending) {
	    
	    Collection<UserStatus> nonRemovedStatuses = Arrays.asList(BLOCKED, ENABLED);
	    
		List<User> users;
		if (limit == 0) {
		    users = Collections.emptyList();
		} else {
		    users = userDAO.findUsers(userId,operatorId, name, nonRemovedStatuses, offset, limit, order, ascending);
		}
        
        int size = userDAO.countUsers(userId, operatorId, name, nonRemovedStatuses);
		
        return new UserQueryResultContainer(size, users);
	}

	@Override
	public void updateUser(User user) {
        User oldUser = getUserById(user.getId());
        user.setPassword(oldUser.getPassword());
        
        // Merge ids for all existing attributes and delete old attributes 
        for (UserAttribute persistentAttribute : oldUser.getAttributes().values()) {
        	Map<String, UserAttribute> newAttributes = user.getAttributes();
            if(newAttributes.containsKey(persistentAttribute.getKey())){
            	newAttributes.get(persistentAttribute.getKey()).setId(persistentAttribute.getId());
            } else {
            	userDAO.delete(persistentAttribute);
            }
        }
        
		userDAO.merge(user);
	}
	
    @Override
    public void setUserStatus(Long userId, UserStatus newStatus) {
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new RuntimeException("user not found, id = " + userId);
        }
        user.setStatus(newStatus);

        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setDescription("Common reason");//ToDo: Must be specified later
        blockedUser.setUserId(userId);
        blockingDAO.persist(blockedUser);
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        User u = getUserById(userId);
        String pwd = config.isPasswordEncryptionEnabled() ? encoder.encrypt(newPassword) : newPassword; 
        u.setPassword(pwd);
    }

	public void setPhoneLookup(PhoneLookup phoneLookup) {
		this.phoneLookup = phoneLookup;
	}

	@Override
	public UserInformation lookupUserInformationFromPhone(String country, String phoneNumber) {
		return phoneLookup.getUserInformation(country, phoneNumber);
	}

    @Override
    public Collection<User> findUsersByExample(UserInformation ui, Long operatorId) {
        return userDAO.findUsersByExample(ui, operatorId);
    }

    @Override
    public Collection<User> findUsersByAttributeKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        return userDAO.findUsersByAttributeKey(key);
    }
    
    @Override
    public Collection<Long> findUsersHavingAttributes(String ... keys) {
        if (keys == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        return userDAO.findUsersHavingAttributes(false, keys);
    }
    
    @Override
    public Collection<Long> findUsersNotHavingAttributes(String ... keys) {
        if (keys == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        return userDAO.findUsersHavingAttributes(true, keys);
    }
    
    @Override
    public Collection<Long> findUsersByAttributeValue(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        return userDAO.findUsersHavingAttributeValue(key, value);
    }    
    
    @Override
    public Collection<User> getUsersWithIds(Collection<Long> userIds){
    	if (userIds == null) {
            throw new IllegalArgumentException("userIds cannot be null");
        }
        
        return userDAO.getUsersWithIds(userIds);
    }
    
    @Override
    public Collection<User> getUsersWithUserType(Collection<UserType> userTypes){
    	if (userTypes == null) {
            throw new IllegalArgumentException("userTypes cannot be null");
        }
        
        return userDAO.getUsersWithUserType(userTypes);
    }

	public void setConfig(Configuration config) {
		this.config = config;
	}
    
    
}
