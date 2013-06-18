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
package com.cubeia.firebase.service.messagebus;

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.layout.ClusterLayout;

public class MbusContractAdapter implements MBusContract {

	public void addMBusListener(MBusListener list) { }

	public void addRedistributorListener(RedistributionListener list) { }

	public Receiver<ChannelEvent> createReceiver(EventType type, String partition, String ownerId) throws MBusException {
		return null;
	}

	public Sender<Event<?>> createSender(EventType type, String ownerId) throws MBusException {
		return null;
	}

	public PartitionMap getCurrentPartitionMap() {
		return null;
	}

	public MBusDetails getMBusDetails() {
		return null;
	}

	public MBusRedistributor getRedistributor() {
		return null;
	}

	public void halt() { }

	public void removeMBusListener(MBusListener list) { }

	public void removeRedistributorListener(RedistributionListener list) { }

	public void resume(ClusterLayout lay) { }

	/*public boolean supports(Feature feat) {
		return false;
	}*/
}
