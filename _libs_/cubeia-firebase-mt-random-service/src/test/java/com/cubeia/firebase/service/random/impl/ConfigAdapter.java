/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.service.random.impl;

import com.cubeia.firebase.service.random.api.RandomServiceConfig;

public class ConfigAdapter implements RandomServiceConfig {

	@Override
	public boolean enableBackgroundPolling() {
		return true;
	}

	@Override
	public long getBackgroundPollingInterval() {
		return 1000;
	}

	@Override
	public int getBackgroundPollingMaxDiscarded() {
		return 10;
	}

	@Override
	public boolean enableDiscardedDraw() {
		return false;
	}

	@Override
	public int getDiscardedDrawMaxDiscarded() {
		return 5;
	}
}
