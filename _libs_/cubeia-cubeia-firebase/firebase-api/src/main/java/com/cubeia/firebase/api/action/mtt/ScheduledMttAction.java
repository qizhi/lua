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
package com.cubeia.firebase.api.action.mtt;

import java.util.UUID;

import com.cubeia.firebase.api.action.visitor.MttActionVisitor;

/**
 * This is a wrapper action for a scheduled action.
 * 
 * The responsibility for this class is to hold the actual action that should
 * be executed together with the identifier.
 *
 */
public class ScheduledMttAction extends AbstractMttAction {

    private static final long serialVersionUID = -1873405541317269020L;
    
    /** Identifier for the scheduled action. */
    private UUID identifier;
    
    /** The action to execute. */
    private MttAction scheduledAction;
    
    /**
     * Creates a scheduled game action.
     * 
     * @param mttId
     * @param scheduledActionIdentifier identifies the scheduled action
     */
    public ScheduledMttAction(int mttId, MttAction scheduledAction, UUID scheduledActionIdentifier) {
        super(mttId);
        this.identifier = scheduledActionIdentifier;
        this.scheduledAction = scheduledAction;
    }
    
    public UUID getIdentifier() {
        return identifier;
    }
    
    public MttAction getScheduledAction() {
        return scheduledAction;
    }

	public void accept(MttActionVisitor visitor) {
		visitor.visit(this);
	}
}
