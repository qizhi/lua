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
package com.cubeia.firebase.api.action.local;

import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Action for waiting lists and join by filters.
 * 
 * @author Fredrik
 *
 */
public class FilteredJoinCancelResponseAction implements LocalAction {

	private long requestId = 0;
	
	private int status = Status.OK.ordinal();
	
	public FilteredJoinCancelResponseAction(long seq) {
		this.requestId = seq;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long seq) {
		this.requestId = seq;
	}

	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}
	
}
