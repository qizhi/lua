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
package com.cubeia.firebase.server.mtt.processor;

import com.cubeia.firebase.mtt.MttNotifierImpl;
import com.cubeia.firebase.transaction.CoreResourceAdapter;
import com.cubeia.firebase.transaction.ResourceException;

public class NotifierResource extends CoreResourceAdapter {

	private final MttNotifierImpl notifier;

	public NotifierResource(MttNotifierImpl notifier) {
		this.notifier = notifier;
	}
	
	@Override
	public void commit() throws ResourceException {
		notifier.commit();
	}
}
