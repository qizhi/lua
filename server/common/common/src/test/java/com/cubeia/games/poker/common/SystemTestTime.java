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

package com.cubeia.games.poker.common;

import java.util.concurrent.atomic.AtomicLong;

import com.cubeia.games.poker.common.time.SystemTime;
import org.joda.time.DateTime;

import com.google.inject.Singleton;

@Singleton
public class SystemTestTime implements SystemTime {

	private final AtomicLong time = new AtomicLong(0);
	
	public void set(long t) {
		time.set(t);
	}
	
	@Override
	public DateTime date() {
		return new DateTime(now());
	}

	@Override
	public long now() {
		return time.get();
	}
}
