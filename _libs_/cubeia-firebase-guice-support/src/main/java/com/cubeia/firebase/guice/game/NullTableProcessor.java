/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.game;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;

/**
 * This is an adaptor class used by the Guice game for un-configured interfaces. It
 * will log all un-implemented method on trace level.
 * 
 * @author larsan
 */
public class NullTableProcessor implements TableListener, TableInterceptor, TournamentProcessor {

	private Logger log = Logger.getLogger(getClass());

	// --- TOURNAMENT LISTENER --- //
	
	public void startRound(Table table) {
		if(log.isTraceEnabled()) {
			log.trace("TournamentProcessor.startRound - No TournamentProcessor bound for injection");
		}
	};
	
	@Override
	public void stopRound(Table table) { 
		if(log.isTraceEnabled()) {
			log.trace("TournamentProcessor.stopRound - No TournamentProcessor bound for injection");
		}
	}
	
	
	
	// --- TABLE LISTENER --- //
	
	@Override
	public void playerJoined(Table arg0, GenericPlayer arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.playerJoined - No TableListener bound for injection");
		}
	}

	@Override
	public void playerLeft(Table arg0, int arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.playerLeft - No TableListener bound for injection");
		}
	}

	@Override
	public void playerStatusChanged(Table arg0, int arg1, PlayerStatus arg2) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.playerStatusChanged - No TableListener bound for injection");
		}
	}

	@Override
	public void seatReserved(Table arg0, GenericPlayer arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.seatReserved - No TableListener bound for injection");
		}
	}

	@Override
	public void watcherJoined(Table arg0, int arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.watcherJoined - No TableListener bound for injection");
		}
	}

	@Override
	public void watcherLeft(Table arg0, int arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableListener.watcherLeft - No TableListener bound for injection");
		}
	}
	
	
	// --- INTERCEPTOR --- //

	@Override
	public InterceptionResponse allowJoin(Table arg0, SeatRequest arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableInterceptor.allowJoin - No TableListener bound for injection");
		}
		return new InterceptionResponse(true, 0);
	}

	@Override
	public InterceptionResponse allowLeave(Table arg0, int arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableInterceptor.allowLeave - No TableListener bound for injection");
		}
		return new InterceptionResponse(true, 0);
	}

	@Override
	public InterceptionResponse allowReservation(Table arg0, SeatRequest arg1) {
		if(log.isTraceEnabled()) {
			log.trace("TableInterceptor.allowReservation - No TableListener bound for injection");
		}
		return new InterceptionResponse(true, 0);
	}
}
