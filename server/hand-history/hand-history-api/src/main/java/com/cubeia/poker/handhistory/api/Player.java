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

package com.cubeia.poker.handhistory.api;

import java.io.Serializable;
import java.math.BigDecimal;

public class Player implements Serializable {

    private static final long serialVersionUID = -4641892328326525634L;

    private int playerId;
    private BigDecimal initialBalance;
    private int seatId;
    private String name;

    public Player() {
    }

    public Player(int playerId) {
        this.playerId = playerId;
    }

    public Player(int playerId, int seatId, BigDecimal initialBalance, String name) {
        this.playerId = playerId;
        this.seatId = seatId;
        this.initialBalance = initialBalance;
        this.name = name;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal balance) {
        this.initialBalance = balance;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getSeatId() {
        return seatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (playerId != player.playerId) return false;
        if (seatId != player.seatId) return false;
        if (initialBalance != null ? !initialBalance.equals(player.initialBalance) : player.initialBalance != null)
            return false;
        if (name != null ? !name.equals(player.name) : player.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = playerId;
        result = 31 * result + (initialBalance != null ? initialBalance.hashCode() : 0);
        result = 31 * result + seatId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Player [playerId=" + playerId + ", initialBalance=" + initialBalance
                + ", seatId=" + seatId + ", name=" + name + "]";
    }
}
