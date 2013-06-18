/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.backend.cashgame.dto;

import com.cubeia.games.poker.common.money.Money;

import java.io.Serializable;

public class OpenSessionRequest implements Serializable {
    private static final long serialVersionUID = 74126213720786784L;
    public final int playerId;
    public final String objectId;
    public final Money openingBalance;
    private String accountName;

    public OpenSessionRequest(int playerId, String objectId, Money openingBalance) {
        this.playerId = playerId;
        this.objectId = objectId;
        this.openingBalance = openingBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Money getOpeningBalance() {
        return openingBalance;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OpenSessionRequest");
        sb.append("{playerId=").append(playerId);
        sb.append(", objectId='").append(objectId).append('\'');
        sb.append(", openingBalance=").append(openingBalance);
        sb.append('}');
        return sb.toString();
    }
}
