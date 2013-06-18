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

package com.cubeia.backoffice.auth;

import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class DTOMarshallTest {

    @Test
    public void marshallUserList() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(BackofficeUserDTO.class, BackofficeUserListDTO.class);
        Marshaller marshaller = ctx.createMarshaller();
        
        BackofficeUserDTO u1 = new BackofficeUserDTO();
        u1.setUserName("u1");
        u1.setPassword("pwd1");
        u1.setRoles(Arrays.asList(BackofficeRole.SUPER_USER, BackofficeRole.WALLET_ADMIN));
        
        BackofficeUserDTO u2 = new BackofficeUserDTO();
        u2.setUserName("u2");
        u2.setPassword("pwd2");
        u2.setRoles(Arrays.asList(BackofficeRole.USER_ADMIN));
        
        BackofficeUserListDTO users = new BackofficeUserListDTO();
        users.setUsers(Arrays.asList(u1, u2));
        
        marshaller.marshal(users, System.out);
    }
    
}
