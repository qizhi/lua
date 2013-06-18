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
package com.cubeia.game.poker.operator.impl;


import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class FirebaseOperatorServiceImplTest {

    @Mock
    LocalServiceAction action;

    @Mock
    LocalActionHandler loopBack;

    @Mock
    OperatorServiceClient client;


    private FirebaseOperatorServiceImpl service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new FirebaseOperatorServiceImpl();
        service.client = client;
        service.mapper = new ObjectMapper();
    }

    @Test
    public void testHandleAction() {

       when(action.getData()).thenReturn(new byte[]{57, 57, 57});   //String 999
       when(client.getConfig(anyLong(),any(OperatorConfigParamDTO.class))).thenReturn("test");
       service.handleAction(action,loopBack);

    }

}
