package com.cubeia.poker.domainevents.api;

import java.io.Serializable;


public class BonusEventWrapper implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int playerId;
	public boolean broadcast = false;
	public String event;
	
	public BonusEventWrapper(int playerId, String event) {
		this.playerId = playerId;
		this.event = event;
	}
	
	public BonusEventWrapper() {}
}
