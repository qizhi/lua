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

package com.pvp.firebase.broadcast.impl;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.poker.broadcast.impl.BroadcastServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BroadcastServiceImplTest {

    private BroadcastServiceImpl service;

    @Mock
    private ServiceContext context;

    @Before
    public void setup() throws SystemException {
        initMocks(this);
        service = new BroadcastServiceImpl();
        service.init(context);
    }

    @Test
    public void testBroadcastMessage() throws Exception {
        MBeanServer server = mock(MBeanServer.class);
        when(context.getMBeanServer()).thenReturn(server);
        service.broadcastMessage("hello");

        verify(server).invoke(isA(ObjectName.class), eq("sendSystemMessage"), eq(new Object[]{0, 0, "hello"}), Matchers.<String[]>any());
    }


}
