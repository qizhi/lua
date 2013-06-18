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
package com.cubeia.firebase.mtt.comm;

import com.cubeia.firebase.server.commands.Types;
import com.cubeia.firebase.service.messagebus.Partition;

public class MttRemoved extends MttCommand<Object> {

	private static final long serialVersionUID = 5844631062673301049L;
	
	private final Partition p;
	private final boolean pre;
	
	public MttRemoved(int mttId, Partition p, boolean pre) {
		super(Types.MTT_REMOVED.ordinal(), Type.NAN, mttId, -1, -1);
		this.p = p;
		this.pre = pre;
	}
	
	public boolean isPre() {
		return pre;
	}
	
	public Partition getPartition() {
		return p;
	}
}
