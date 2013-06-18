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
package com.cubeia.firebase.server.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.TransientGameAction;
import com.cubeia.firebase.util.Serializer;

public abstract class Event<A extends Action> implements Externalizable {

	protected byte[] payload;
	protected Object attachment;
	protected int senderId;
	protected int[] targetIds;
	protected boolean isTransient;
	
	private transient A action;
	// private transient boolean forceSingleTarget;
	
	/**
	 * This method prepares the event for serialization by
	 * converting the attached action object to binary data. This
	 * must be called on the event before the event is put to
	 * the message bus.
	 * 
	 * @throws IOException If the serialization fails
	 */
	public void wrapForTransport() throws IOException {
		if(action != null) {
			payload = new Serializer().serialize(action);
		}
	}
	
	/**
	 * This method deserializes the attached action object, using an
	 * optional target class loader. Eg., for events targeting games, the
	 * given class loader should probably be the GAR class loader.
	 * 
	 * @param targetClassLoader Class loader to use, may be null
	 * @throws Exception On class loading issues
	 */
	@SuppressWarnings("unchecked")
	public void unwrapForTarget(ClassLoader targetClassLoader) throws Exception {
		if(payload != null) {
			action = (A)new Serializer(targetClassLoader).deserialize(payload);
		}
	}
	
	/**
	 * An event is transient if the action contained is an instance
	 * of {@link TransientGameAction}.
	 * 
	 * @return True if the event is transient
	 */
	public boolean isTransient() {
		return isTransient;
	}
	
	public A getAction() {
		return action;
	}
	
	public void setAction(A action) {
		this.action = action;
		if(action instanceof TransientGameAction) {
			isTransient = true;
		} else {
			isTransient = false;
		}
	}
    
    protected void setSenderId(int senderId) {
		this.senderId = senderId;
	}
    
    public int getSenderId() {
		return senderId;
	}
    
    protected void setTargetIds(int[] targetIds) {
		this.targetIds = targetIds;
	}

	public int getFirstTargetId() {
		return (targetIds == null || targetIds.length == 0 ? -1 : targetIds[0]);
	}
    
    public int[] getTargetIds() {
		return targetIds;
	}

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    protected byte[] getPayload() {
		return payload;
	}
   
    protected void setPayload(byte[] payload) {
		this.payload = payload;
	}
    
    /*
     * If this boolean is set, the mbus will attempt to 
     * single-cast the message. In other words, only target 
     * a single node for delivery. Please be careful setting
     * this boolean as it may destroy fail-over.
     * 
     * @param force True to bypass fail-over sending, false otherwise
     */
    /*public void setForceSingleTarget(boolean forceSingleTarget) {
		this.forceSingleTarget = forceSingleTarget;
	}*/
    
    /*
     * If this method returns true, the mbus will attempt to bypass
     * fail-over replication and only send the message to a single
     * target.
     * 
     * @return True to bypass fail-over sending, false otherwise
     */
    /*public boolean getForceSingleTarget() {
		return forceSingleTarget;
	}*/

    
    // --- EXTERNALIZABLE --- //
    
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	attachment = in.readObject();
    	payload = (byte[])in.readObject();
    	senderId = in.readInt();
    	targetIds = (int[])in.readObject();
    	isTransient = in.readBoolean();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeObject(attachment);
    	out.writeObject(payload);
    	out.writeInt(senderId);
    	out.writeObject(targetIds);
    	out.writeBoolean(isTransient);
    }
}