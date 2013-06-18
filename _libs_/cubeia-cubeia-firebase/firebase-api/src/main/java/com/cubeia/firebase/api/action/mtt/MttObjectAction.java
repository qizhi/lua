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

import com.cubeia.firebase.api.action.IdentifiableAction;
import com.cubeia.firebase.api.action.visitor.MttActionVisitor;

/**
 * Action that contains game specific data.
 * This action will primarily be used for scheduling purposes.
 *  
 * @author Fredrik
 *
 */
public class MttObjectAction extends AbstractMttAction implements IdentifiableAction {

	private static final long serialVersionUID = 1L;

	/** Is this action generated from internal class */
	private boolean internal = true;
	
    /** The attached object. This is the actual data that the receiver is interested in. */
	private Object attachment;

    /** A unique identifier for this action. */
    private UUID identifier;
	
    /**
     * Constructor creating an action with the given mtt instance as recipient and the given attachment.
     * @param mttId The tournament instance id. 
     * @param attachment the attachment
     */
	public MttObjectAction(int mttId, Object attachment) {
	    super(mttId);
	    setAttachment(attachment);
	}

    public void accept(MttActionVisitor visitor) {
        visitor.visit(this);
    }
	
	public String toString() {
		return "Mtt Object Action : table["+getMttId()+"] Attachment["+attachment+"]";
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
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
