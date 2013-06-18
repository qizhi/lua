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

package com.cubeia.backend.firebase;

import com.cubeia.backend.cashgame.Asynchronous;
import com.cubeia.backend.cashgame.CashGamesBackend;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.games.poker.common.money.Currency;

/**
 * This contract mimics the {@link CashGamesBackend} with 
 * the exception of announce, open session and reserve which
 * are all asynchronous and to which the answer will be sent
 * as an object action to the tables. All such action are
 * marched as {@link Asynchronous} below.
 * 
 * @see CashGamesBackend
 */
public interface CashGamesBackendService extends AsynchronousCashGamesBackend, Contract {

    public static final String MARKET_TABLE_REFERENCE_KEY = "MARKET_TABLE_REFERENCE";

    public static final String MARKET_TABLE_SESSION_REFERENCE_KEY = "MARKET_TABLE_SESSION_REFERENCE";

}
