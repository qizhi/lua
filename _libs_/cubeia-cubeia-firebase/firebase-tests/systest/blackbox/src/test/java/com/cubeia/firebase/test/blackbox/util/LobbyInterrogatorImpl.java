package com.cubeia.firebase.test.blackbox.util;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.lobby.systemstate.StateLobbyMBean;
import com.cubeia.firebase.test.common.LobbyInterrogator;

public class LobbyInterrogatorImpl implements LobbyInterrogator {

	private final StateLobbyMBean lobby;

	public LobbyInterrogatorImpl(StateLobbyMBean lobby) {
		this.lobby = lobby;
	}
	
	@Override
	public int getSubscribersForPath(LobbyPath path) {
		return lobby.countSubscribersForPath(path.getType().name(), path.getArea(), path.getDomain());
	}
	
	@Override
	public String getTableInfo(int table) {
		return lobby.printTableData(table);
	}
}
