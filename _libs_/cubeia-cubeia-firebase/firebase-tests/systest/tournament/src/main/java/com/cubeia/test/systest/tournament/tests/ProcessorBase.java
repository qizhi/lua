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
package com.cubeia.test.systest.tournament.tests;

import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.test.systest.tournament.TournamentTestProcessor;

public abstract class ProcessorBase implements TournamentTestProcessor {
	
	private static final long serialVersionUID = 1797531516740059118L;
	
	protected volatile MTTSupport support;
	
	public void postMethod() {
		support = null;
	}
	
	public void preMethod(MTTSupport support) {
		this.support = support;
	}
	
	protected MTTSupport getMTTSupport() {
		return support;
	}
}
