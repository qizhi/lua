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
package com.cubeia.backoffice.users.client;

import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClientHTTP;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class OperatorServiceClientHTTPTest {

    @Mock
    private HttpClient client;

    private OperatorServiceClientHTTP operatorServiceClient;

    private String response;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        createServiceClient(5);
    }

    private void createServiceClient(int ttl) {
        operatorServiceClient = new OperatorServiceClientHTTP("",ttl){
            @Override
            protected InputStream execute(HttpMethodBase method) throws IOException {
                getClient().executeMethod(method);
                return new ByteArrayInputStream(response.getBytes());
            }
            @Override
            protected HttpClient getClient() {
                return client;
            }

        };
    }

    @Test
    public void testCachedConfig() throws Exception{
        when(client.executeMethod(any(HttpMethod.class))).thenReturn(HttpStatus.SC_OK);
        response = "TEST_VALUE";
        String val = operatorServiceClient.getConfig(1L, OperatorConfigParamDTO.CLIENT_HELP_URL);
        assertEquals(response,val);
        operatorServiceClient.getConfig(1L, OperatorConfigParamDTO.CLIENT_HELP_URL);
        verify(client,times(1)).executeMethod(any(HttpMethodBase.class));
    }

    @Test
    public void testNoCacheConfig() throws Exception {
        createServiceClient(0);
        when(client.executeMethod(any(HttpMethod.class))).thenReturn(HttpStatus.SC_OK);
        response = "TEST_VALUE";
        String val = operatorServiceClient.getConfig(1L, OperatorConfigParamDTO.CLIENT_HELP_URL);
        assertEquals(response,val);
        operatorServiceClient.getConfig(1L, OperatorConfigParamDTO.CLIENT_HELP_URL);
        verify(client,times(2)).executeMethod(any(HttpMethodBase.class));
    }

    @Test
    public void testGetOperator() {
        response = "{\"id\":1,\"name\":\"test\",\"enabled\":true}";
        OperatorDTO operator = operatorServiceClient.getOperator(1L);
        assertEquals(1,operator.getId());
        assertEquals("test",operator.getName());
        assertTrue(operator.isEnabled());
    }

    @Test
    public void testGetConfigMap() {
        response = "{\"CLIENT_HELP_URL\":\"1\",\"WALLET_SERVICE_ENDPOINT_URL\":\"2\"}";
        Map<OperatorConfigParamDTO,String> config = operatorServiceClient.getConfig(1L);
        assertEquals("1",config.get(OperatorConfigParamDTO.CLIENT_HELP_URL));
        assertEquals("2",config.get(OperatorConfigParamDTO.WALLET_SERVICE_ENDPOINT_URL));
    }

    @Test
    public void testGetOperators() {

        response = "[{\"id\":1,\"name\":\"o1\",\"enabled\":true},{\"id\":2,\"name\":\"o2\",\"enabled\":false}]";

        List<OperatorDTO> operators = operatorServiceClient.getOperators();

        assertEquals(2,operators.size());
        OperatorDTO operator1 = operators.get(0);
        OperatorDTO operator2 = operators.get(1);

        assertEquals(1, operator1.getId());
        assertEquals("o1", operator1.getName());
        assertTrue(operator1.isEnabled());

        assertEquals(2, operator2.getId());
        assertEquals("o2", operator2.getName());
        assertFalse(operator2.isEnabled());

    }

    @Test
    public void testOperatorEnabled() {
        createServiceClient(0);
        response = "true";
        boolean enabled = operatorServiceClient.isEnabled(1L);
        assertTrue(enabled);

        response = "false";
        enabled = operatorServiceClient.isEnabled(1L);
        assertFalse(enabled);
    }

    @Test
    public void testOperatorEnabledCached() {
        createServiceClient(5);
        response = "true";
        boolean enabled = operatorServiceClient.isEnabled(1L);
        assertTrue(enabled);

        response = "false";  //shouldn't be picked up cos of the cache
        enabled = operatorServiceClient.isEnabled(1L);
        assertTrue(enabled);
    }
}
