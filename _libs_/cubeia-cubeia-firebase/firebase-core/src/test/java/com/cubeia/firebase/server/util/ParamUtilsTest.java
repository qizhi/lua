/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.server.util;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.server.util.ParamUtils;

public class ParamUtilsTest extends TestCase {

    private StyxSerializer serializer = new StyxSerializer(null);
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetParameterList() throws Exception {
        
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("one", 1);
        map.put("name", "Apa");
        map.put("empty", "");
        map.put("Date", new Date(13123123));
        
        
        System.out.println("Map: "+map);
        List<Param> list = ParamUtils.getParameterList(map);
        
        
        
        for (Param p : list) {
            String output = p.key+"\t"; 
            ByteBuffer pack = serializer.pack(p);
            System.out.println(output + Arrays.toString(pack.array()));
        }
        
//        ByteBuffer pack2 = serializer.pack(new Param());
//        System.out.println("P2: " + Arrays.toString(pack2.array()));
        
    }

}
