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
package com.cubeia.firebase.service.messagebus.util;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;

/**
 * This is a sender adapter, that first wrap an event before
 * sending it to its adapted class.
 * 
 * @author Larsan
 */
public class WrappingSender<E extends Event<?>> implements Sender<E> {

	private final Sender<E> target;

	public WrappingSender(Sender<E> target) {
		Arguments.notNull(target, "target");
		this.target = target;
	}
	
	public void destroy() {
		target.destroy();
	}

	public void dispatch(E event) throws ChannelNotFoundException {
		tryWrap(event);
		forward(event);
	}

	public String getOwnerId() {
		return target.getOwnerId();
	}
	
	
	// --- PRIVATE METHODS --- //

	private void forward(E event) throws ChannelNotFoundException {
		target.dispatch(event);
	}

	private void tryWrap(E event) {
		try {
			event.wrapForTransport();
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to wrap event", e);
		}
	}
}
