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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.entity.Gender;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.entity.UserStatus;
import com.cubeia.backoffice.users.entity.UserType;

public class DTOFactoryTest {

    @Test
    public void createUserDTOByEntityAndReverse() {
        DTOFactory dtof = new DTOFactory();
        
        assertNull(dtof.createUserDTOByEntity(null));
        assertNull(dtof.createUserEntityByDTO(null));
        
        User ue = new User();
        ue.setId(12l);
        ue.setExternalId("x123");
        ue.setOperatorId(1234l);
        ue.setPassword("abc");
        ue.setStatus(UserStatus.REMOVED);
        ue.setUserType(UserType.OPERATOR);
        ue.setUserName("def");
        ue.setCreationDate(new Date(0));
        ue.setLastLoginDate(new Date(100));
        ue.addAttribute("apa", "banan");
        ue.getAttributes().get("apa").setId(11l);
        ue.addAttribute("knark", "tjack");
        ue.getAttributes().get("knark").setId(22l);
        UserInformation uie = new UserInformation();
        uie.setDateOfBirth(new Date(200));
        uie.setBillingAddress("ba");
        uie.setCellphone("2343434");
        uie.setCity("asdf");
        uie.setCountry("SE");
        uie.setEmail("a@cubeia.se");
        uie.setFax("fax234");
        uie.setFirstName("abba");
        uie.setGender(Gender.FEMALE);
        uie.setId(314l);
        uie.setLastName("bubba");
        uie.setPhone("083434");
        uie.setState("state");
        uie.setTitle("title");
        uie.setWorkphone("2334");
        uie.setZipcode("12242");
        uie.setTimeZone("UTC");
        uie.setCurrency("EUR");
        ue.setInformation(uie);
        
        com.cubeia.backoffice.users.api.dto.User udto = dtof.createUserDTOByEntity(ue);
        assertEquals(ue.getId(), udto.getUserId());
        assertEquals(ue.getExternalId(), udto.getExternalUserId());
        assertEquals(ue.getOperatorId(), udto.getOperatorId());
        assertEquals(ue.getUserName(), udto.getUserName());
        assertEquals(ue.getStatus().name(), udto.getStatus().name());
        assertEquals(ue.getUserType().name(), udto.getUserType().name());
        assertEquals(ue.getCreationDate(), new Date(0));
        assertEquals(ue.getLastLoginDate(), new Date(100));
        assertEquals("banan", udto.getAttributes().get("apa"));
        //assertEquals(new Long(11), udto.getAttributes().get("apa").getId());
        assertEquals("tjack", udto.getAttributes().get("knark"));
        //assertEquals(new Long(22l), udto.getAttributes().get("knark").getId());
        
        com.cubeia.backoffice.users.api.dto.UserInformation uidto = udto.getUserInformation();
        assertEquals(uie.getBillingAddress(), uidto.getBillingAddress());
        assertEquals(uie.getCellphone(), uidto.getCellphone());
        assertEquals(uie.getCity(), uidto.getCity());
        assertEquals(uie.getCountry(), uidto.getCountry());
        assertEquals(uie.getEmail(), uidto.getEmail());
        assertEquals(uie.getFax(), uidto.getFax());
        assertEquals(uie.getFirstName(), uidto.getFirstName());
        assertEquals(uie.getLastName(), uidto.getLastName());
        assertEquals(uie.getPhone(), uidto.getPhone());
        assertEquals(uie.getState(), uidto.getState());
        assertEquals(uie.getTitle(), uidto.getTitle());
        assertEquals(uie.getWorkphone(), uidto.getWorkphone());
        assertEquals(uie.getZipcode(), uidto.getZipcode());
        assertEquals(uie.getGender().name(), uidto.getGender().name());
        assertEquals(uie.getId(), uidto.getId());
        assertEquals(uie.getTimeZone(), uidto.getTimeZone());
        assertEquals(uie.getCurrency(), uidto.getCurrency());
        assertEquals(new Date(200), uie.getDateOfBirth());

        User ue2 = dtof.createUserEntityByDTO(udto);
        assertEquals(udto.getUserId(), ue2.getId());
        assertEquals(udto.getExternalUserId(), ue2.getExternalId());
        assertEquals(udto.getOperatorId(), ue2.getOperatorId());
        assertEquals(udto.getUserName(), ue2.getUserName());
        assertEquals(udto.getStatus().name(), ue2.getStatus().name());
        assertEquals(udto.getUserType().name(), ue2.getUserType().name());
        assertEquals(udto.getCreationDate(), new Date(0));
        assertEquals(udto.getLastLoginDate(), new Date(100));
        assertEquals("banan", ue2.getAttributes().get("apa").getValue());
        assertEquals("tjack", ue2.getAttributes().get("knark").getValue());
        
        UserInformation uei2 = ue2.getInformation();
        assertEquals(uidto.getBillingAddress(), uei2.getBillingAddress());
        assertEquals(uidto.getCellphone(), uei2.getCellphone());
        assertEquals(uidto.getCity(), uei2.getCity());
        assertEquals(uidto.getCountry(), uei2.getCountry());
        assertEquals(uidto.getEmail(), uei2.getEmail());
        assertEquals(uidto.getFax(), uei2.getFax());
        assertEquals(uidto.getFirstName(), uei2.getFirstName());
        assertEquals(uidto.getLastName(), uei2.getLastName());
        assertEquals(uidto.getPhone(), uei2.getPhone());
        assertEquals(uidto.getState(), uei2.getState());
        assertEquals(uidto.getTitle(), uei2.getTitle());
        assertEquals(uidto.getWorkphone(), uei2.getWorkphone());
        assertEquals(uidto.getZipcode(), uei2.getZipcode());
        assertEquals(uidto.getGender().name(), uei2.getGender().name());
        assertEquals(uidto.getId(), uei2.getId());
        assertEquals(uidto.getTimeZone(), uei2.getTimeZone());
        assertEquals(uidto.getCurrency(), uei2.getCurrency());
        assertEquals(new Date(200), uei2.getDateOfBirth());
    }

    @Test
    public void createUserOrderDTOByDomainAndReverse() {
        DTOFactory dtof = new DTOFactory();
        assertEquals(com.cubeia.backoffice.users.api.UserOrder.COUNTRY, dtof.createUserOrderDTOByDomain(UserOrder.COUNTRY));
        assertEquals(com.cubeia.backoffice.users.api.UserOrder.ID, dtof.createUserOrderDTOByDomain(UserOrder.ID));
        assertEquals(com.cubeia.backoffice.users.api.UserOrder.STATUS, dtof.createUserOrderDTOByDomain(UserOrder.STATUS));
        assertEquals(com.cubeia.backoffice.users.api.UserOrder.USER_NAME, dtof.createUserOrderDTOByDomain(UserOrder.USER_NAME));
        
        assertEquals(UserOrder.COUNTRY, dtof.createUserOrderDomainByDTO(com.cubeia.backoffice.users.api.UserOrder.COUNTRY));
        assertEquals(UserOrder.ID, dtof.createUserOrderDomainByDTO(com.cubeia.backoffice.users.api.UserOrder.ID));
        assertEquals(UserOrder.STATUS, dtof.createUserOrderDomainByDTO(com.cubeia.backoffice.users.api.UserOrder.STATUS));
        assertEquals(UserOrder.USER_NAME, dtof.createUserOrderDomainByDTO(com.cubeia.backoffice.users.api.UserOrder.USER_NAME));
    }

}
