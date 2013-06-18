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
package com.cubeia.backoffice.users.api.dto;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.junit.Test;

public class UserMarshallTest {
    
    @Test 
    public void xmlRoundtrip() throws JAXBException {
        User u = new User();
        u.setUserId(123l);
        u.setExternalUserId("x123");
        u.setOperatorId(1337l);
        u.setStatus(UserStatus.BLOCKED);
        u.setUserName("snubbe");
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("apa", "banan");
        attributes.put("knark", "gott");
        u.setAttributes(attributes);
        UserInformation ui = new UserInformation();
        ui.setCity("city");
        ui.setGender(Gender.FEMALE);
        
        u.setUserInformation(ui);
        
        JAXBContext context = JAXBContext.newInstance(User.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(u, new OutputStreamWriter(out));
        
        Unmarshaller unmarshaller = context.createUnmarshaller();
        User u2 = (User) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(u, u2);
    }
    
    @Test 
    public void userDateFormatJSON() throws JAXBException, IOException, ParseException {
        User u = new User();
        u.setUserId(123l);
        u.setExternalUserId("x123");
        u.setOperatorId(1337l);
        u.setStatus(UserStatus.BLOCKED);
        u.setUserName("snubbe");
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("apa", "banan");
        attributes.put("knark", "gott");
        u.setAttributes(attributes);
        UserInformation ui = new UserInformation();
        ui.setCity("city");
        ui.setGender(Gender.FEMALE);
        u.setUserInformation(ui);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm z");
        Date date = sdf.parse("2012.09.01 00:01 GMT");
        u.setCreationDate(date);
        
        ObjectMapper map = new ObjectMapper();
        map.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        String s = map.writeValueAsString(u);
    
        System.out.println(s);
    }
}
