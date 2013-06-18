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
package com.cubeia.firebase.server.processor.filter;

import static com.cubeia.firebase.api.util.Arguments.notNull;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.game.handler.AbstractTableActionHandler;
import com.cubeia.firebase.api.game.handler.ActionHandler;
import com.cubeia.firebase.api.game.handler.MttTableActionHandler;
import com.cubeia.firebase.api.game.handler.StandardTableActionHandler;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.rule.DefaultSeatingRules;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.game.GameConfig;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyTableAccessor;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

/**
 * This filter uses a standard table action handler to take care of seating
 * rules (join, leave, watch etc) on a table. It accepts Firebase table and 
 * game actions only.
 * 
 * @author Lars J. Nilsson
 */
public class FirebaseSeatingFilter<T extends FirebaseTable, A extends GameAction> implements ProcessorFilter<T, A> {

	private final Logger log = Logger.getLogger(getClass());
	private final ServiceRegistry services;
	private final GameConfig config;
	
	public FirebaseSeatingFilter(GameConfig config, ServiceRegistry services) {
		notNull(services, "services");
		this.config = config;
		this.services = services;
	}
	
	@Override
	public void process(A action, T table, ProcessorChain<T, A> filters) {
		fowardToActionHandler(action, table);
		filters.next(action, table);
	}

	
	// --- PRIVATE METHODS --- //
	
	private void fowardToActionHandler(A action, T table) {
		ActionHandler handler = lookupTableActionHandler(table);
		if (handler != null) {
			if(log.isTraceEnabled()) {
	        	log.trace("Forwarding action [" + action + "] to action handler [" + handler + "]");
	        }
			action.visit(handler);
		}
	}
	
	/*
     * Get the ActionHandler used for this table.
     * Currently we only provide default handler, 
     * but this could easily be extended by adding an interface
     * available to the game.
     */
    protected ActionHandler lookupTableActionHandler(FirebaseTable table) {
    	DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(services.getServiceInstance(SystemStateServiceContract.class));
		AbstractTableActionHandler handler = null;
		
		if (table.getMetaData().getType() == TableType.NORMAL) {
			handler = new StandardTableActionHandler(table, acc, new DefaultLobbyMutator());
			
		} else if (table.getMetaData().getType() == TableType.MULTI_TABLE_TOURNAMENT) {
			handler = new MttTableActionHandler(table, acc, new DefaultLobbyMutator());
			
		} else {
			log.error("Unknown Table Type encountered. Action will be ignored!. Table: "+table.getId()+" Type: "+table.getMetaData().getType());
		}
		
		if (handler != null) {
			handler.setSeatingRules(new DefaultSeatingRules());
			handler.setNotifier(table.getNotifier());
			handler.setServiceRegistry(services);
			checkSetTimeouts(handler);
		}
		return handler;
	}
    
    private void checkSetTimeouts(AbstractTableActionHandler handler) {
		if(config != null) {
			handler.setPlayerReconnectTimeout(config.getPlayerReconnectTimeout());
			handler.setPlayerReservationTimeout(config.getPlayerReservationTimeout());
		}
	}
}
