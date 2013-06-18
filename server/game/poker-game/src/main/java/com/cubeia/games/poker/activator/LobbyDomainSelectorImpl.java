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

package com.cubeia.games.poker.activator;

import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.google.inject.Singleton;

@Singleton
public class LobbyDomainSelectorImpl implements LobbyDomainSelector {

	@Override
	public String selectLobbyDomainFor(TableConfigTemplate templ) {
		return getGameShortName(templ) + "/cashgame/REAL_MONEY/" + templ.getSeats();
	}

	private String getGameShortName(TableConfigTemplate templ) {
		switch(templ.getVariant()) {
			case TELESINA : return "telesina";
			case TEXAS_HOLDEM : return "texas";
		}
		throw new IllegalArgumentException("Unknown variant: " + templ.getVariant());
	}
}
