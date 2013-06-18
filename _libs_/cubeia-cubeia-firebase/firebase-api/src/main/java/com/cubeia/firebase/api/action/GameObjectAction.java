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
 * Action that contains game specific data.
 * This action will primarily be used for scheduling purposes.
 *  
 * @author Fredrik
 *
 */
public class GameObjectAction extends AbstractGameAction implements IdentifiableAction {

	private static final long serialVersionUID = 1L;
	
    /** The attached object. This is the actual data that the receiver is interested in. */
	private Object attachment;

    /** A unique identifier for this action. */
    private UUID identifier;
	
	public GameObjectAction(int tableId) {
		super(tableId);
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public String toString() {
		return "Game Object Action : table["+getTableId()+"] Attachment["+attachment+"]";
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.action.IdentifiableAction#getIdentifier()
     */
    public UUID getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.action.IdentifiableAction#setIdentifier(java.util.UUID)
     */
    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }
    
}
