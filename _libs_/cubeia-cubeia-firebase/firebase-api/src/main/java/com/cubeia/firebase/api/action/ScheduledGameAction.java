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
package com.cubeia.firebase.api.action;

import java.util.UUID;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * This is a wrapper action for a scheduled action.
 * 
 * The responsibility for this class is to hold the actual action that should
 * be executed together with the identifier.
 *
 */
public class ScheduledGameAction extends AbstractGameAction {

    private static final long serialVersionUID = -1873405541317269020L;
    
    /** Identifier for the scheduled action. */
    private UUID identifier;
    
    /** The action to execute. */
    private GameAction scheduledAction;
    
    /**
     * Creates a scheduled game action.
     * 
     * @param tableId
     * @param scheduledActionIdentifier identifies the scheduled action
     */
    public ScheduledGameAction(int tableId, GameAction scheduledAction, UUID scheduledActionIdentifier) {
        super(tableId);
        this.identifier = scheduledActionIdentifier;
        this.scheduledAction = scheduledAction;
    }

    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }
    
    public String toString() {
    	return "ScheduledGameAction action["+scheduledAction+"] UUID["+identifier+"]"; 
    }
    
    public UUID getIdentifier() {
        return identifier;
    }
    
    public GameAction getScheduledAction() {
        return scheduledAction;
    }
}
