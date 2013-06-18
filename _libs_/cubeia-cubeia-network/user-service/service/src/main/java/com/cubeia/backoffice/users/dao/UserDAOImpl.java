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
package com.cubeia.backoffice.users.dao;

import static com.cubeia.backoffice.users.entity.UserStatus.REMOVED;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.like;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Required;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserAttribute;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.entity.UserStatus;
import com.cubeia.backoffice.users.entity.UserType;

/**
 * Implementation of the UserDAO interface.
 * Uses Spring JPATemplate for easier handling of JPA/Hibernate entities.
 * 
 * This implementation will encrypt the passwords using a JASYPT encryptor.
 * The encryption method is specified in the user-service-app.xml and defaults
 * to MD5 with DES.
 * 
 * The encryption is transparent to the user of this DAO, i.e. the password 
 * will be encrypted when saving and decrypted when loading so the password 
 * will always be in plain text for the accessor of this class. 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class UserDAOImpl implements UserDAO {

    @PersistenceContext(unitName = "userServicePersistenceUnit")
    protected EntityManager em;
    
	private StandardPBEStringEncryptor encoder;

	/**
	 * DI by Spring
	 * @param encoder
	 */
	@Resource(name = "user.service.encryption") @Required
	public void setEncryption(StandardPBEStringEncryptor encoder) {
		this.encoder = encoder;
	}
	
	/**
	 * Will decrypt the password before returning the user
	 */
	@Override
	public User getById(Long id) {
		User user = em.find(User.class, id);
		decryptPassword(user);
		return user;
	}
	

	/**
	 * Will encrypt the password before persisting the User
	 */
	@Override
	public void persist(User user) {
		encryptPassword(user);
		em.persist(user);
		decryptPassword(user);
	}
	
	public void merge(User user) {
	    em.merge(user);
	}

	@Override
	public void delete(User user) {
		em.remove(user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getByUserName(String userName, Long operatorId) {
        Query q = em.createQuery("from User as u where u.userName = :username and u.operatorId = :operatorId");
        q.setParameter("username", userName);
        q.setParameter("operatorId", operatorId);
        List<User> result = q.getResultList();
        
        if (result.size() == 1) {
        	User user = result.get(0);
        	decryptPassword(user);
        	return user;
        } else {
        	return null;
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public User getByExternalId(String externalId, Long operatorId) {
        Query q = em.createQuery("from User as u where u.externalId = :externalId and u.operatorId = :operatorId");
        q.setParameter("externalId", externalId);
        q.setParameter("operatorId", operatorId);
        List<User> result = q.getResultList();
        if (result.size() == 1) {
        	User user = result.get(0);
        	decryptPassword(user);
        	return user;
        } else {
        	return null;
        }
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public List<User> findUsers(Long userId, Long operatorId, String userName, Collection<UserStatus> includedStatuses, 
    	int offset, int limit, UserOrder order, boolean ascending) {
    	
        Criteria c = createFindUserCriteria(userId, operatorId, userName, includedStatuses, offset, limit, order, ascending);
        // always add ascending id order as fallback (or sub ordering)
        c.addOrder(Order.asc("id"));
        return c.list();
    }

    @Override
    public int countUsers(Long userId, Long operatorId, String name, Collection<UserStatus> includedStatuses) {
        Criteria c = createFindUserCriteria(userId, operatorId, name, includedStatuses, 0, Integer.MAX_VALUE, null, true);
        c.setProjection(Projections.rowCount());
        return ((Number) c.uniqueResult()).intValue();
    }
    
    private Session getHibernateSession() {
        return (Session) em.getDelegate();
    }
    
    private Criteria createFindUserCriteria(Long userId, Long operatorId, String name, Collection<UserStatus> includeStatuses,
        int offset, int limit, UserOrder order, boolean ascending) {
    	
        Session hbSession = getHibernateSession();
        Criteria c = hbSession.createCriteria(User.class);
        c.createAlias("information", "information", JoinType.LEFT_OUTER_JOIN);
        c.setFetchMode("attributes", FetchMode.SELECT);
        
        if (userId != null) {
            c.add(eq("id", userId));
        }
        
        if (operatorId != null) {
        	c.add(eq("operatorId", operatorId));
        }
        
        if (name != null  &&  !name.isEmpty()) {
            c.add(Restrictions.disjunction().
                add(like("userName", name)).
                add(like("information.firstName", name)).
                add(like("information.lastName", name)));
        }
        
        if (includeStatuses != null) {
            c.add(Restrictions.in("status", includeStatuses));
        }
        
        if (order != null) {
	        if (ascending) {
	        	c.addOrder(Order.asc(order.getColumnName()));
	        } else {
	        	c.addOrder(Order.desc(order.getColumnName()));
	        }
        }
        
        c.setFirstResult(offset);
        c.setMaxResults(limit);
        return c;
    }
    
	private void decryptPassword(User user) {
		if (user != null && user.getPassword() != null) {
			user.setDecrypedPassword(encoder.decrypt(user.getPassword()));
		}
	}
	
	private void encryptPassword(User user) {
		user.setPassword(encoder.encrypt(user.getPassword()));
	}

    @SuppressWarnings("unchecked")
    @Override
    public Collection<User> findUsersByExample(UserInformation ui, Long operatorId) {
        Session hbs = getHibernateSession();
        Criteria uc = hbs.createCriteria(User.class);
        uc.add(Restrictions.ne("status", REMOVED));
        uc.add(Restrictions.eq("userType", UserType.USER));
        
        if(operatorId != null && operatorId >= 0){
        	uc.add(Restrictions.eq("operatorId", operatorId));
        }
        
        Example ex = Example.create(ui);
        uc.createCriteria("information").add(ex);
        return uc.list();
    }

    @Override
    public void delete(UserAttribute attrib) {
        em.remove(attrib);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<User> findUsersByAttributeKey(String key) {
        return em.createQuery("select ua.user from UserAttribute ua where ua.key = :key").setParameter("key", key).
            getResultList();
    }

    /*
     * Dont set notHavingAttr to true. Bugged out!!!!
     */
    @SuppressWarnings("unchecked")
    @Override
	public Collection<Long> findUsersHavingAttributes(boolean notHavingAttr, String ... attributeKeys) {
    	String hql = "select distinct ua.user.id from UserAttribute ua where ua.key " + (notHavingAttr ? "not" : "") + " in (:keys)";    	
    	return em.createQuery(hql).setParameter("keys", Arrays.asList(attributeKeys)).
        	getResultList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
	public Collection<Long> findUsersHavingAttributeValue(String key, Object value){
    	Query query =  em.createQuery("select ua.user.id from UserAttribute ua where ua.key = :key and ua.value " 
    			+ (value == null ? "is null" : "= :value"))
    			.setParameter("key", key);
    	if(value != null){
    		query.setParameter("value", value);
    	}
    	
    	return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<User> getUsersWithIds(Collection<Long> userIds){
    	String hql = "select u from User u where u.id in (:userIds)";    	
    	return em.createQuery(hql).setParameter("userIds", userIds).
        	getResultList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<User> getUsersWithUserType(Collection<UserType> userTypes){
    	String hql = "select u from User u where u.userType in (:userTypes)";    	
    	return em.createQuery(hql).setParameter("userTypes", userTypes).
        	getResultList();
    }
}
