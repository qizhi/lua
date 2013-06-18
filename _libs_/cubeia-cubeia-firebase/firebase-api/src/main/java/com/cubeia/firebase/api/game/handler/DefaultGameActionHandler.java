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
package com.cubeia.firebase.api.game.handler;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.ScheduledGameAction;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableScheduler;


/**
 * 
 * Created on 2006-sep-28
 * 
 * @author Fredrik Johansson
 * 
 * $RCSFile: $ $Revision: $ $Author: $ $Date: $
 */
public class DefaultGameActionHandler extends AbstractActionHandler {
	
	private static transient Logger log = Logger.getLogger(DefaultGameActionHandler.class);

	private final Game game;
	private GameProcessor processor;

	private final Table table;

	public DefaultGameActionHandler(Table table, Game game) {
		this.table = table;
		this.game = game;
	}
	
	private GameProcessor getProcessor() {
		if(processor == null) {
			processor = game.getGameProcessor();
		} 
		return processor;
	}
	
	/**
	 * Dispatch a GameDataAction.
	 * 
	 */
	public void visit(GameDataAction action) {
		if(log.isTraceEnabled()) {
        	log.trace("Visiting game data action [" + action + "] for table " + table.getId());
        }
		getProcessor().handle(action, table);
	}
	
	/**
	 * Dispatch a GameObjectAction
	 * We will only propagate non-internal GameObjectActions
	 * 
	 */
	public void visit(GameObjectAction action) {
		if(log.isTraceEnabled()) {
        	log.trace("Visiting game object action [" + action + "] for table " + table.getId());
        }
		getProcessor().handle(action, table);
	}
	
    /**
     * Handles a scheduled action. First the action is executed, then
     * the scheduled action is removed from the map of scheduled actions. 
     * 
     */
    public void visit(ScheduledGameAction action) {
        action.getScheduledAction().visit(this);
        TableScheduler scheduler = table.getScheduler();
        scheduler.cancelScheduledAction(action.getIdentifier());
    }
    
  
}
