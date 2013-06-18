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
package com.cubeia.backoffice.operator.client;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class OperatorServiceClientHTTP implements  OperatorServiceClient {

    private final Logger logger = Logger.getLogger(OperatorServiceClientHTTP.class);

    private static final int NUMBER_OF_RETRIES = 3;

    private String baseUrl;

    private ObjectMapper mapper;

    private static final String OPERATOR = "/operator/id/%s";
    private static final String OPERATOR_API_KEY = "/operator/key/%s";
    private static final String OPERATOR_LIST  = "/operator";
    private static final String CREATE_OPERATOR = "/operator/create";
    private static final String UPDATE_OPERATOR = "/operator/update";
    
    private static final String OPERATOR_CONFIG_MAP 	= OPERATOR+"/config";
    private static final String OPERATOR_CLIENT_CONFIG_MAP 	= OPERATOR+"/clientconfig";
    private static final String OPERATOR_CONFIG_PARAM 	= OPERATOR+"/config/%s";
    private static final String UPDATE_CONFIG			= OPERATOR+"/config";
    private static final String UPDATE_CONFIG_PARAM 	= OPERATOR+"/config/%s/%s";
    private static final String OPERATOR_ENABLED 		= OPERATOR+"/enabled";
    
    
    private Cache<OperatorConfigCacheKey,String> configParamCache = null;
    private Cache<Long,Boolean> operatorEnabledCache = null;
    
    private LoadingCache<Long, Map<OperatorConfigParamDTO, String>> operatorConfigCache = CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.maximumSize(10000)
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new CacheLoader<Long, Map<OperatorConfigParamDTO, String>>() {
			     public Map<OperatorConfigParamDTO, String> load(Long operatorId) {
			         return loadOperatorConfigForClients(operatorId);
			       }
			     });

    private int configCacheTTL = 5;



    public OperatorServiceClientHTTP(String baseUrl) {
        this(baseUrl,5);
    }
    
	public OperatorServiceClientHTTP(String baseUrl, int configCacheTTL) {
        this.baseUrl = baseUrl;
        this.configCacheTTL = configCacheTTL;
        initJsonMapper();
        setUpConfigCache();

    }

    public  OperatorServiceClientHTTP() {
        this("http://localhost:9092/operator-service-rest/rest");
    }

    private void setUpConfigCache() {
        configParamCache = CacheBuilder.newBuilder().
                expireAfterWrite(getConfigCacheTTL(), TimeUnit.MINUTES).maximumSize(1000).build();

        operatorEnabledCache = CacheBuilder.newBuilder().
                expireAfterWrite(getConfigCacheTTL(), TimeUnit.MINUTES).maximumSize(200).build();
    }
    public String getConfigFromCache(Long operatorId, OperatorConfigParamDTO param) {
        return configParamCache.getIfPresent(new OperatorConfigCacheKey(operatorId,param));
    }

    @Override
    public List<OperatorDTO> getOperators() {
        HttpMethodBase method = createGetMethod(baseUrl+OPERATOR_LIST);

        try {
            // Execute the method.
            InputStream body = execute(method);
            if (body == null) {
                return null;
            }
            return this.<List<OperatorDTO>>parseList(body, new TypeReference<List<OperatorDTO>>(){});
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    protected InputStream execute(HttpMethodBase method) throws IOException {
        try {
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new IOException("Exception executing http method at " + method.getURI() ,e);
        }
    }

    @Override
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    @Override
    public void createOperator(OperatorDTO operator) {
    	PostMethod method = new PostMethod(baseUrl + CREATE_OPERATOR);
        prepareMethod(method);
        try {

            String data = serialize(operator);
            method.setRequestEntity(new StringRequestEntity(data,"application/json", "UTF-8"));

            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public void updateOperator(OperatorDTO operator) {
        PutMethod method = new PutMethod(baseUrl + UPDATE_OPERATOR);
        prepareMethod(method);
        try {

            String data = serialize(operator);
            method.setRequestEntity(new StringRequestEntity(data,"application/json", "UTF-8"));

            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public void updateConfig(Long operatorId, Map<OperatorConfigParamDTO, String> config) {
        PutMethod method = new PutMethod(String.format(baseUrl + UPDATE_CONFIG, operatorId));
        prepareMethod(method);
        try {
            String data = serialize(config);
            method.setRequestEntity(new StringRequestEntity(data,"application/json", "UTF-8"));

            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }
    
    @Override
    public void updateConfig(Long operatorId, OperatorConfigParamDTO key, String value) {
        PutMethod method = new PutMethod(String.format(baseUrl + UPDATE_CONFIG_PARAM, operatorId, key.name(), value));
        prepareMethod(method);
        method.setRequestHeader("content-type","text/plain;charset=UTF-8;");
        try {
            String data = value;
            method.setRequestEntity(new StringRequestEntity(data,"text/plain", "UTF-8"));

            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public String getConfig(Long operatorId, OperatorConfigParamDTO param) {
        String val = getConfigFromCache(operatorId, param);
        if(val==null) {
            val = getOperatorConfig(operatorId,param);
            if(val!=null) {
                cacheOperatorConfig(operatorId,param,val);
            }
        }
        return val;
    }
    
    @Override
	public Map<OperatorConfigParamDTO, String> getClientConfig(Long operatorId) {
		try {
			return operatorConfigCache.get(operatorId);
		} catch (ExecutionException e) {
			throw new RuntimeException("Failed to load operator configuration", e);
		}
	}

    private String getOperatorConfig(Long operatorId, OperatorConfigParamDTO param) {
        String val;
        String url = String.format(baseUrl + OPERATOR_CONFIG_PARAM, operatorId, param.name());
        HttpMethodBase method = createGetMethod(url);

        try {
            InputStream body = execute(method);
            if (body == null) {
                return null;
            }
            val =  new java.util.Scanner(body).useDelimiter("\\A").next();
        } catch (Exception e) {
            logger.error("unable to get " + url);
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
        return val;
    }
    
    @SuppressWarnings("unchecked")
	protected Map<OperatorConfigParamDTO, String> loadOperatorConfigForClients(Long operatorId) {
    	String url = String.format(baseUrl + OPERATOR_CLIENT_CONFIG_MAP, operatorId);
        HttpMethodBase method = createGetMethod(url);

        try {
            InputStream body = execute(method);
            if (body == null) {
                return new HashMap<OperatorConfigParamDTO, String>();
            }
            return (Map<OperatorConfigParamDTO, String>)parseJson(body, Map.class);
        } catch (Exception e) {
        	
            logger.error("unable to get " + url);
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
	}

    @Override
    public boolean isEnabled(Long operatorId) {

        Boolean val = operatorEnabledCache.getIfPresent(operatorId);
        if(val != null) {
            return val;
        }
        val = isOperatorEnabled(operatorId);
        operatorEnabledCache.put(operatorId,val);
        return val;
    }

    private boolean isOperatorEnabled(Long operatorId) {
        String url = String.format(baseUrl+OPERATOR_ENABLED, operatorId);
        HttpMethodBase method = createGetMethod(url);

        try {
            // Execute the method.
            InputStream body = execute(method);
            if (body == null) {
                return false;
            }
            return parseJson(body, Boolean.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    private void cacheOperatorConfig(Long operatorId, OperatorConfigParamDTO param, String val) {
        configParamCache.put(new OperatorConfigCacheKey(operatorId,param),val);
    }

    @Override
    public Map<OperatorConfigParamDTO, String> getConfig(Long operatorId) {
        String url = String.format(baseUrl+OPERATOR_CONFIG_MAP, operatorId);
        HttpMethodBase method = createGetMethod(url);

        try {
            // Execute the method.
            InputStream body = execute(method);
            if (body == null) {
                return null;
            }
            return this.<Map<OperatorConfigParamDTO,String>>parseList(body, new TypeReference<Map<OperatorConfigParamDTO,String>>(){});
        } catch (Exception e) {

            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public OperatorDTO getOperator(Long operatorId) {
        String url = String.format(baseUrl + OPERATOR, operatorId);
        HttpMethodBase method = createGetMethod(url);

        try {
            // Execute the method.
            InputStream body = execute(method);
            if (body == null) {
                return null;
            }
            return parseJson(body,OperatorDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }
    
    @Override
	public OperatorDTO getOperatorByApiKey(String apiKey) {
    	String url = String.format(baseUrl + OPERATOR_API_KEY, apiKey);
        HttpMethodBase method = createGetMethod(url);

        try {
            // Execute the method.
            InputStream body = execute(method);

            return parseJson(body, OperatorDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
	}

    /**
     * Configure Jackson to use JAXB annotations (only)
     */
    private void initJsonMapper() {
        mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
    }

    private <T> T parseJson(InputStream data, Class<T> clazz) {
        try {
            return mapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private <T> T parseList(InputStream data, TypeReference<T> ref) {
        try {
            return mapper.<T>readValue(data, ref);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("error serializing json", e);
        }
    }

    private void assertResponseCodeOK(HttpMethodBase method, int statusCode) throws HttpException {
        if (statusCode != HttpStatus.SC_OK) {
            throw new HttpException("Method failed: " + method.getStatusLine());
        }
    }
    protected HttpClient getClient() {
        return new HttpClient();
    }
    private HttpMethodBase createGetMethod(String resource) {
        GetMethod method = new GetMethod(resource);
        prepareMethod(method);
        return method;
    }

    private HttpMethodBase prepareMethod(HttpMethodBase method) {
        method.setRequestHeader("content-type","application/json;charset=UTF-8;");
        // Provide custom retry handler
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(NUMBER_OF_RETRIES, false));
        return method;
    }

    public int getConfigCacheTTL() {
        return configCacheTTL;
    }

    private static class OperatorConfigCacheKey {
        final Long operatorId;
        final OperatorConfigParamDTO param;

        OperatorConfigCacheKey(Long operatorId, OperatorConfigParamDTO param) {
            this.operatorId = operatorId;
            this.param = param;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperatorConfigCacheKey that = (OperatorConfigCacheKey) o;

            if (operatorId != null ? !operatorId.equals(that.operatorId) : that.operatorId != null) return false;
            if (param != that.param) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = operatorId != null ? operatorId.hashCode() : 0;
            result = 31 * result + (param != null ? param.hashCode() : 0);
            return result;
        }
    }

}