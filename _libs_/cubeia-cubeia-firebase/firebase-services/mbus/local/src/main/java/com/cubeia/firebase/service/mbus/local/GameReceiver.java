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
package com.cubeia.firebase.service.mbus.local;

import com.cubeia.firebase.service.mbus.common.StrictPooledHandoff;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;

public class GameReceiver extends HandoffReceiver {

	public GameReceiver(PartitionedQueues parent) {
		super(parent);
	}

	@Override
	protected StrictPooledHandoff selectHandoff(PartitionedQueues parent) {
		return parent.getGameHandoff();
	}

	@Override
	protected void setOrphanListener(PartitionedQueues parent, OrphanEventListener<ChannelEvent> list) {
		parent.setGameOrphanListener(list);
	}
}
