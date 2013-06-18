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

package com.cubeia.games.poker.integration.api;

import com.cubeia.games.poker.common.money.Money;

public interface WalletService {

    /**
     * Get the balance of the poker wallet.
     */
    public Money getBalance(long playerId);

    /**
     * Withdraw money from the poker wallet to
     * use in the poker server. This will be called
     * on buy-ins, registrations, re-buy etc.
     */
    public Response withdraw(Request req);

    /**
     * Deposit money to the poker wallet. This
     * will be called on cash-out, tournament wins
     * etc.
     */
    public Response deposit(DepositRequest req);
}
