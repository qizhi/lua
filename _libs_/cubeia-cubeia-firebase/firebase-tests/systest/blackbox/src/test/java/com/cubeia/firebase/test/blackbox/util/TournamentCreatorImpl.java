package com.cubeia.firebase.test.blackbox.util;

import com.cubeia.firebase.test.blackbox.FirebaseTest;
import com.cubeia.firebase.test.common.TournamentCreator;
import com.cubeia.test.systest.tournament.ActivatorMBean;

public class TournamentCreatorImpl implements TournamentCreator {

	private final int seats;
	private final int capacity;
	private final int minPlayers;
	
	private final ActivatorMBean proxy;
	private final String proc;
	private final String domain;

	public TournamentCreatorImpl(ActivatorMBean proxy, String domain, int seats, int capacity, int minPlayers, String proc) {
		this.proxy = proxy;
		this.domain = domain;
		this.seats = seats;
		this.capacity = capacity;
		this.minPlayers = minPlayers;
		this.proc = proc;
	}

	public int create() {
		return proxy.createTournament(FirebaseTest.GAME_ID, domain, seats, "Systest Tournament <" + capacity + ">", capacity, minPlayers, proc);
	}
}
