/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.events.event.operator;

import com.cubeia.events.event.OperatorEvent;
import com.cubeia.events.event.OperatorEventType;

public class OperatorEvents {
	
	public static OperatorEvent createAuthenticationError(Long operatorId, String message, String url) {
		OperatorEvent event = new OperatorEvent();
		event.type = OperatorEventType.authenticationError.name();
		event.operator = operatorId;
		event.attributes.put("message", message);
		event.attributes.put("url", url);
		return event;
	}
	
}
