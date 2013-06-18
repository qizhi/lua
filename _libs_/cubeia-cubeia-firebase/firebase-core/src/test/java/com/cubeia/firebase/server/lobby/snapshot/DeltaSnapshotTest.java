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
package com.cubeia.firebase.server.lobby.snapshot;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;

public class DeltaSnapshotTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testMerge() throws Exception {
        LobbyPath path = new LobbyPath(99, "a");
        
        DeltaSnapshot snapshot = new DeltaSnapshot(path);
        
        
        List<Param> oldParameters = new ArrayList<Param>();
        Param p1 = ParameterUtil.createParam("one", "1");
        oldParameters.add(p1);
        
        
        List<Param> params = new ArrayList<Param>();
        Param p2 = ParameterUtil.createParam("two", "2");
        Param p3 = ParameterUtil.createParam("one", "3");
        params.add(p2);
        params.add(p3);
        
        snapshot.mergeParameters(oldParameters, params);
        assertEquals(new String(new byte[]{ 0,1,50}), new String(params.get(0).value));
    }
    
    public void testMergeNull() throws Exception {
/*        LobbyPath path = new LobbyPath(99, "a");
        
        DeltaSnapshot snapshot = new DeltaSnapshot(path);
        
        
        List<Param> oldParameters = new ArrayList<Param>();
        Param p1 = ParamUtils.createParam("one", "1");*/
//        Param p2 = ParamUtils.createParam(null, null);
//        oldParameters.add(p1);
        
        
//        List<Param> params = new ArrayList<Param>();
//        Param p3 = ParamUtils.createParam("two", "2");
//        Param p4 = ParamUtils.createParam("four", "4");
//        params.add(p2);
//        params.add(p3);
//        
//        System.out.println("Before: "+params);
//        
//        snapshot.mergeParameters(oldParameters, params);
//        
//        assertEquals(new String(new byte[]{ 0,1,50}), new String(params.get(0).value));
//        
//        System.out.println("After: "+params);
        
    }


}
