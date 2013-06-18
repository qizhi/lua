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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ServiceGameEvent extends GameEvent {

	private static final long serialVersionUID = 5891425221741380172L;

	private String serviceId;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	@Override
	public String toString() {
		return super.toString() + " SID[" + serviceId + "]";
	}
	
	// --- EXTERNALIZABLE --- //
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		if(in.readBoolean()) {
			serviceId = in.readUTF();
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		if(serviceId != null) {
			out.writeBoolean(true);
			out.writeUTF(serviceId);
		} else {
			out.writeBoolean(false);
		}
	}
}
