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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.cubeia.backoffice.users.api.dto.*;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.users.api.UserOrder;

/**
 * User Service Java API client implementation.
 * 
 * 
 * <p>This implementation uses the Apache Commons HTTP Client to connect to the Cubeia Network User Service REST API.</p>
 * 
 * <p>The client is thread safe and lock free, there is no need to pool instances.</p>
 * 
 * <p>Usage:<p>
 * <code>
 *   UserServiceClient client = new UserServiceClientHTTP("http://my-user-service:8080/user-service");
 * </code>
 * 
 * @author w
 */
public class UserServiceClientHTTP implements UserServiceClient {

	private static final int NUMBER_OF_RETRIES = 3;

	private static final String PING = "/ping";
	
	private static final String AUTHENTICATE = "/users/authenticate";
	private static final String AUTHENTICATE_TOKEN = "/users/authenticate/token";
	private static final String QUERY = "/users/search/query";
	private static final String CREATE_USER = "/users";

	private static final String USER = "/user/id/%s";
	private static final String USER_ATTRIBUTES = USER+"/attributes";
	private static final String USER_USERNAME = USER+"/username";
    private static final String USER_AVATARID = USER+"/avatarid";
	private static final String USER_SESSION = USER+"/session";
	private static final String SET_PASSWORD = USER+"/password";
	private static final String SET_STATUS = USER+"/status";
	
	private String baseUrl;

	private ObjectMapper mapper;

	private Logger log = LoggerFactory.getLogger(getClass());
    private static final String EXTERNAL_USER = "/user/getbyexternalid/%s/%s";

    /**
	 * Constructs a user service client connecting to the default url: "http://userservice:8080/user-service".
	 * You need to map the "userservice" host in your hosts-file for this to work.
	 */
	public UserServiceClientHTTP() {
		this("http://userservice:8080/user-service");
	}
	
	/**
	 * Constructs a user service client with the given base url. The base url should contain the host, port and context of the user service.
	 * Example url: "http://localhost:8080/user-service".
	 * @param baseUrl the base url
	 */
	public UserServiceClientHTTP(String baseUrl) {
		this.baseUrl = baseUrl;
		initJsonMapper();
	}
	
	@Override
	public void setBaseUrl(String baseUrl) {
		log.debug("setting user service base url to: {}", baseUrl);
		this.baseUrl = baseUrl;
	}

	@Override
	public String getBaseUrl() {
	    return this.baseUrl;
	}
	
    @Override
    public String ping() {
        GetMethod method = createGetMethod(baseUrl + PING);
        
        try {
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
            return  method.getResponseBodyAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }
	
	@Override
	public AuthenticationResponse authenticate(Long operatorId, String username, String password) {
		PostMethod method = new PostMethod(baseUrl+AUTHENTICATE);
		AuthenticationRequest request = new AuthenticationRequest();
		request.setOperatorId(operatorId);
		request.setUserName(username);
		request.setPassword(password);
		
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				AuthenticationResponse response = new AuthenticationResponse();
				response.setAuthenticated(false);
				return response;
				
			} else if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, AuthenticationResponse.class);
				
			} else {
				throw new RuntimeException("Failed to authenticate user, RESPONSE CODE: "+statusCode);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public AuthenticationResponse authenticateSessionToken(String sessionToken) {
		PostMethod method = new PostMethod(baseUrl+AUTHENTICATE_TOKEN);
		AuthenticationTokenRequest request = new AuthenticationTokenRequest();
		request.setSessionToken(sessionToken);
		
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);

			if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				AuthenticationResponse response = new AuthenticationResponse();
				response.setAuthenticated(false);
				return response;
				
			} else if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, AuthenticationResponse.class);
				
			} else {
				throw new RuntimeException("Failed to authenticate user, RESPONSE CODE: "+statusCode);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public void invalidateSessionToken(Long userId) {
		// Create a method instance.
		String resource = String.format(baseUrl+USER_SESSION, userId);
		DeleteMethod method = new DeleteMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
			assertResponseCodeOK(method, statusCode);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
	
	@Override
	public User getUserById(Long userId) {
		// Create a method instance.
		String resource = String.format(baseUrl+USER, userId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			
			return parseJson(body, User.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

    @Override
    public User getUserByExternalId(String externalId, Long operatorId) {
        // Create a method instance.
        String resource = String.format(baseUrl+EXTERNAL_USER, operatorId, externalId);
        GetMethod method = createGetMethod(resource);

        try {
            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
            // Read the response body.
            InputStream body = method.getResponseBodyAsStream();
            return parseJson(body, User.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getUserAttributes(Long userId) {
		// Create a method instance.
		String resource = String.format(baseUrl+USER_ATTRIBUTES, userId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			//FileInputStream fis = new FileInputStream("src/test/resources/attributes.json");
			
			return parseJson(body, HashMap.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
	
	@Override
	public String getUsername(Long userId) {
		// Create a method instance.
		String resource = String.format(baseUrl+USER_USERNAME, userId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			return new String(method.getResponseBody());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
	
	private void assertResponseCodeOK(HttpMethodBase method, int statusCode) throws HttpException {
		if (statusCode != HttpStatus.SC_OK) {
			throw new HttpException("Method failed: " + method.getStatusLine());
		}
	}
	
	private GetMethod createGetMethod(String resource) {
		GetMethod method = new GetMethod(resource);
		// Provide custom retry handler 
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(NUMBER_OF_RETRIES, false));
		return method;
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
	
	private String serialize(Object value) {
		try {
			//return mapper.writeValueAsBytes(value);
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException("error serializing json", e);
		} 
	}

	@Override
	public UserQueryResult findUsers(Long userId, String name, int offset, int limit, UserOrder sortOrder, boolean ascending) {
		PutMethod method = new PutMethod(baseUrl+QUERY);
		
		UserQuery request = new UserQuery();
		request.setUserId(userId);
		request.setUserName(name);
		request.setQueryOffset(offset);
		request.setQueryLimit(limit);
		request.setAscending(ascending);
		request.setOrder(sortOrder);
		
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, UserQueryResult.class);
				
			} else {
				throw new RuntimeException("Failed to query users, RESPONSE CODE: "+statusCode);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
	
	@Override
	public void setUserStatus(Long userId, UserStatus status) {
		String resource = String.format(baseUrl+SET_STATUS, userId);
		PutMethod method = new PutMethod(resource);
		try {
			ChangeUserStatusRequest request = new ChangeUserStatusRequest();
			request.setUserStatus(status);
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
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
	public void updateUser(User user) {
		String resource = String.format(baseUrl+USER, user.getUserId());
		PutMethod method = new PutMethod(resource);
		try {
			String data = serialize(user);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
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
    public String getUserAvatarId(Long userId) {
        // Create a method instance.
        String resource = String.format(baseUrl+USER_AVATARID, userId);
        GetMethod method = createGetMethod(resource);

        try {
            // Execute the method.
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
            return new String(method.getResponseBody());

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public void setUserAvatarId(Long userId, String avatarId) {
        String resource = String.format(baseUrl+USER_AVATARID, userId);
        PutMethod method = new PutMethod(resource);
        try {
            ChangeUserAvatarIdRequest request = new ChangeUserAvatarIdRequest();
            request.setUserAvatarId(avatarId);
            String data = serialize(request);
            method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));

            // Execute the HTTP Call
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
	public void updatePassword(Long userId, String newPassword) {
		String resource = String.format(baseUrl+SET_PASSWORD, userId);
		PutMethod method = new PutMethod(resource);
		try {
			ChangeUserPasswordRequest request = new ChangeUserPasswordRequest();
			request.setPassword(newPassword);
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
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
	public CreateUserResponse createUser(CreateUserRequest request) {
		PostMethod method = new PostMethod(baseUrl+CREATE_USER);
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
			
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, CreateUserResponse.class);
				
			} else {
				throw new RuntimeException("Failed to create user, RESPONSE CODE: "+statusCode);
			}


		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}		
	
	private HttpClient getClient() {
		return new HttpClient();
	}

	@Override
	public void setUserAttribute(Long userId, String key, String value) {
		String resource = String.format(baseUrl+USER_ATTRIBUTES+"/"+key, userId);
		PutMethod method = new PutMethod(resource);
		try {
			method.setRequestEntity(new StringRequestEntity(value, "text/plain", "UTF-8"));
			
			// Execute the HTTP Call
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
	public String getUserAttribute(Long userId, String key) {
		// Create a method instance.
		String resource = String.format(baseUrl+USER_ATTRIBUTES+"/"+key, userId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			
			return new java.util.Scanner(body).useDelimiter("\\A").next();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
}