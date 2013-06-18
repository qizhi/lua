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

package com.cubeia.games.poker;

import com.cubeia.game.poker.config.api.HandHistoryConfig;
import com.cubeia.game.poker.config.api.PokerActivatorConfig;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.game.poker.config.api.PokerSystemConfig;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;

import java.math.BigDecimal;

public class PokerConfigServiceMock implements PokerConfigurationService {

	@Override
	public HandHistoryConfig getHandHistoryConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public PokerActivatorConfig getActivatorConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PokerSystemConfig getSystemConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
