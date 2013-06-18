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
package com.cubeia.backoffice.users.util;

import java.util.Map;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.api.dto.UserType;
import com.cubeia.backoffice.users.entity.Gender;
import com.cubeia.backoffice.users.entity.UserAttribute;
import com.cubeia.backoffice.users.entity.UserInformation;

/**
 * Factory for converting DTO objects from entities and the other way around.
 * @author w
 */
public class DTOFactory {

    public User createUserDTOByEntity(com.cubeia.backoffice.users.entity.User user) {
        if (user == null) {
            return null;
        }
        User userDTO = new User();
        userDTO.setUserId(user.getId());
        userDTO.setExternalUserId(user.getExternalId());
        userDTO.setUserName(user.getUserName());
        userDTO.setPassword(user.getPassword()); //temporary
        userDTO.setCreationDate(user.getCreationDate());
        userDTO.setLastLoginDate(user.getLastLoginDate());
        if (user.getStatus() != null) {
            userDTO.setStatus(UserStatus.valueOf(user.getStatus().name()));
        }
        if (user.getUserType() != null) {
            userDTO.setUserType(UserType.valueOf(user.getUserType().name()));
        }
        userDTO.setOperatorId(user.getOperatorId());
        userDTO.setUserInformation(createUserInformationDTOByEntity(user.getInformation()));
        
        for (Map.Entry<String, UserAttribute> ae : user.getAttributes().entrySet()) {
            UserAttribute atr = ae.getValue();
            userDTO.getAttributes().put(ae.getKey(), atr.getValue());
        }
        
        return userDTO;
    }

    public com.cubeia.backoffice.users.entity.User createUserEntityByDTO(User user) {
        if (user == null) {
            return null;
        }
        com.cubeia.backoffice.users.entity.User userEntity = new com.cubeia.backoffice.users.entity.User();
        userEntity.setId(user.getUserId());
        userEntity.setExternalId(user.getExternalUserId());
        userEntity.setOperatorId(user.getOperatorId());
        userEntity.setUserName(user.getUserName());
        userEntity.setPassword(user.getPassword()); //temporary
        userEntity.setCreationDate(user.getCreationDate());
        userEntity.setLastLoginDate(user.getLastLoginDate());
        if (user.getStatus() != null) {
            userEntity.setStatus(com.cubeia.backoffice.users.entity.UserStatus.valueOf(user.getStatus().name()));
        }
        if (user.getUserType() != null) {
            userEntity.setUserType(com.cubeia.backoffice.users.entity.UserType.valueOf(user.getUserType().name()));
        }
        userEntity.setInformation(createUserInformationEntityByDTO(user.getUserInformation()));
        
        for (Map.Entry<String, String> e : user.getAttributes().entrySet()) {            
            userEntity.getAttributes().put(e.getKey(), new UserAttribute(userEntity, e.getKey(), 
                    e.getValue()));
        }
        
        return userEntity;
    }
    
    public UserInformation createUserInformationEntityByDTO(
        com.cubeia.backoffice.users.api.dto.UserInformation info) {
        if (info == null) {
            return null;
        }
        
        UserInformation information = new UserInformation();
        information.setDateOfBirth(info.getDateOfBirth());
        information.setId(info.getId());
        information.setBillingAddress(info.getBillingAddress());
        information.setCellphone(info.getCellphone());
        information.setCity(info.getCity());
        information.setCountry(info.getCountry());
        information.setEmail(info.getEmail());
        information.setFax(info.getFax());
        information.setFirstName(info.getFirstName());
        information.setLastName(info.getLastName());
        if (info.getGender() == null) {
            information.setGender(null);
        } else {
            information.setGender(Gender.values()[info.getGender().ordinal()]);
        }
        information.setPhone(info.getPhone());
        information.setState(info.getState());
        information.setTitle(info.getTitle());
        information.setWorkphone(info.getWorkphone());
        information.setZipcode(info.getZipcode());
        information.setTimeZone(info.getTimeZone());
        information.setCurrency(info.getCurrency());
        information.setAvatarId(info.getAvatarId());
        
        return information;        
    }

    public com.cubeia.backoffice.users.api.dto.UserInformation createUserInformationDTOByEntity(UserInformation info) {
        if (info == null) {
            return null;
        }
        
        com.cubeia.backoffice.users.api.dto.UserInformation information = new com.cubeia.backoffice.users.api.dto.UserInformation();

        information.setDateOfBirth(info.getDateOfBirth());
        information.setId(info.getId());
        information.setBillingAddress(info.getBillingAddress());
        information.setCellphone(info.getCellphone());
        information.setCity(info.getCity());
        information.setCountry(info.getCountry());
        information.setEmail(info.getEmail());
        information.setFax(info.getFax());
        information.setFirstName(info.getFirstName());
        information.setLastName(info.getLastName());
        if (info.getGender() == null) {
            information.setGender(null);
        } else {
            information.setGender(com.cubeia.backoffice.users.api.dto.Gender.values()[info.getGender().ordinal()]);
        }
        information.setPhone(info.getPhone());
        information.setState(info.getState());
        information.setTitle(info.getTitle());
        information.setWorkphone(info.getWorkphone());
        information.setZipcode(info.getZipcode());
        information.setTimeZone(info.getTimeZone());
        information.setCurrency(info.getCurrency());
        information.setAvatarId(info.getAvatarId());
        
        return information;
    }

    public com.cubeia.backoffice.users.api.UserOrder createUserOrderDTOByDomain(UserOrder order) {
        return order == null ? null : com.cubeia.backoffice.users.api.UserOrder.valueOf(order.name());
    }
    
    public UserOrder createUserOrderDomainByDTO(com.cubeia.backoffice.users.api.UserOrder order) {
        return order == null ? null : UserOrder.valueOf(order.name());
    }
}
