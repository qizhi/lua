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
package com.game.server.service.lobby;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;
import com.cubeia.firebase.api.service.dosprotect.Rule;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.systemstate.StateLobby;
import com.cubeia.firebase.server.service.lobby.LobbyConfig;
import com.cubeia.firebase.server.service.lobby.LobbyServiceContract;
import com.cubeia.util.Lists;

public class LobbyService implements Service, LobbyServiceContract {
	
	private Lobby lobby;
	private ServiceContext con;
	private LobbyConfig conf;

	private final Logger log = Logger.getLogger(getClass());
	
	public void destroy() {
		lobby = null;
	}

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		setupConfig();
		configDos();
	}



	public void start() {
		lobby = new StateLobby(con.getParentRegistry());
		lobby.start();
	}
	
	
	public void stop() {
		lobby.stop();
	}

	public Lobby getLobby() {
		return lobby;
	}
	
	public LobbyConfig getConfig() {
		return conf;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupConfig() throws SystemCoreException {
		ClusterConfigProviderContract contr = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		conf = contr.getConfiguration(LobbyConfig.class, new Namespace(LOBBY_NS));
	}
	
	private void configDos() {
		DosProtector dos = con.getParentRegistry().getServiceInstance(DosProtector.class);
		List<Rule> rules = new LinkedList<Rule>();
		checkFixedRule(rules);
		checkIntervalRule(rules);
		if(rules.size() == 0) {
			log.warn("Lobby unprotected; Consider setting access frequency protection in the configuration.");
		} else {
			dos.config(LobbyServiceContract.LOBBY_DOS_KEY, Lists.toArray(rules, Rule.class));
		}
	}

	private void checkIntervalRule(List<Rule> rules) {
		long millis = conf.getIntervalAccessFrequencyLength();
		int count = conf.getIntervalAccessFrequency();
		if(millis == -1 || count == -1) {
			log.info("Lobby ingoring interval frequency protection");
		} else {
			log.info("Lobby configured for max " + count + " requests every " + millis + " millisecond interval");
			rules.add(new FrequencyRule(count, millis));
		}
	}

	private void checkFixedRule(List<Rule> rules) {
		long millis = conf.getMaxFixedAccessFrequency();
		if(millis == -1) {
			log.info("Lobby ingoring minimum frequency protection");
		} else {
			log.info("Lobby configured for max 1 request every " + millis + " millisecond");
			rules.add(new FrequencyRule(1, millis));
		}
	}
}