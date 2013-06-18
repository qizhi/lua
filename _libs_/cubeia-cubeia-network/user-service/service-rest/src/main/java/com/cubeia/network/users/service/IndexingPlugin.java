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
package com.cubeia.network.users.service;

import static com.cubeia.backoffice.users.api.dto.CreationStatus.OK;
import static org.apache.http.protocol.HTTP.CONTENT_TYPE;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.cubeia.backoffice.users.api.dto.CreationStatus;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.integrations.PluginFactory;
import com.cubeia.backoffice.users.integrations.UserServicePluginAdapter;

public class IndexingPlugin extends UserServicePluginAdapter {

	private static final String APPLICATION_JSON = "application/json";
	
	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	private PluginFactory plugins;
	
	@Value("${user.json.indexing.url}")
	private String baseUrl; 
	
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final ObjectMapper mapper = new ObjectMapper();
	
	/*
	 * Init is called by the spring context.
	 */
	public void init() {
		if(baseUrl != null && baseUrl.length() > 0) {
			log.info("Registering indexing plugin with base URL: " + baseUrl);
			plugins.getPlugins().add(this);
			if(!baseUrl.endsWith("/")) {
				baseUrl += "/";
			}
		} else {
			log.info("Indexing plugin disabled");
		}
	}
	
	@Override
	public void afterCreate(CreationStatus status, User user) {
		if(status == OK) {
			addTask(user);
		}
	}
	
	@Override
	public void afterUpdate(User user) {
		addTask(user);
	}

	
	// --- PRIVATE METHODS --- //
	
	private void addTask(final User user) {
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				log.debug("Indexing user " + user.getUserId());
				DefaultHttpClient dhc = new DefaultHttpClient();
				HttpPut method = new HttpPut(baseUrl + user.getUserId());
				method.addHeader(CONTENT_TYPE, APPLICATION_JSON);
				try {
					String json = mapper.writeValueAsString(user);
					if(log.isTraceEnabled()) {
						log.trace("User " + user.getUserId() + " JSON: " + json);
					}
					StringEntity ent = new StringEntity(json, "UTF-8");
					ent.setContentType(APPLICATION_JSON);
					method.setEntity(ent);
					HttpResponse resp = dhc.execute(method);
					if(!String.valueOf(resp.getStatusLine().getStatusCode()).startsWith("2")) {
						log.warn("Failed to index user " + user.getUserId() + "; Server responded with status: " + resp.getStatusLine());
					} else {
						HttpEntity entity = resp.getEntity();
						String string = EntityUtils.toString(entity, "UTF-8");
						if(log.isTraceEnabled()) {
							log.trace("User " + user.getUserId() + " return JSON: " + string);
						}
						JsonNode node = mapper.readTree(string);
						if(!node.get("ok").asBoolean()) {
							log.warn("Failed to index user " + user.getUserId() + "; Response JSON: " + string);
						} 
					}
				} catch(Exception e) {
					log.error("Failed to index user " + user.getUserId(), e);
				} 
			}
		});
	}
}
