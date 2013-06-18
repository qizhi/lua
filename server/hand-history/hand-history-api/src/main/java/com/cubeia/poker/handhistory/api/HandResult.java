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

/**
 * This is the hand result of a single player. It contains
 * winnings, rake contribution and bet size.
 *
 * @author Lars J. Nilsson
 */
public class HandResult implements Serializable {

    private static final long serialVersionUID = 7495444478185154491L;

    private int playerId;
    private BigDecimal netWin;
    private BigDecimal totalWin;
    private BigDecimal rake;
    private BigDecimal totalBet;
    private String transactionId;

    public HandResult() {
    }

    public HandResult(int playerId, BigDecimal netWin, BigDecimal totalWin, BigDecimal rake, BigDecimal totalBet) {
        this.playerId = playerId;
        this.netWin = netWin;
        this.totalWin = totalWin;
        this.rake = rake;
        this.totalBet = totalBet;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public BigDecimal getNetWin() {
        return netWin;
    }

    public void setNetWin(BigDecimal netWin) {
        this.netWin = netWin;
    }

    public BigDecimal getTotalWin() {
        return totalWin;
    }

    public void setTotalWin(BigDecimal totalWin) {
        this.totalWin = totalWin;
    }

    public BigDecimal getRake() {
        return rake;
    }

    public void setRake(BigDecimal rake) {
        this.rake = rake;
    }

    public BigDecimal getTotalBet() {
        return totalBet;
    }

    public void setTotalBet(BigDecimal totalBet) {
        this.totalBet = totalBet;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HandResult that = (HandResult) o;

        if (playerId != that.playerId) return false;
        if (netWin != null ? !netWin.equals(that.netWin) : that.netWin != null) return false;
        if (rake != null ? !rake.equals(that.rake) : that.rake != null) return false;
        if (totalBet != null ? !totalBet.equals(that.totalBet) : that.totalBet != null) return false;
        if (totalWin != null ? !totalWin.equals(that.totalWin) : that.totalWin != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = playerId;
        result = 31 * result + (netWin != null ? netWin.hashCode() : 0);
        result = 31 * result + (totalWin != null ? totalWin.hashCode() : 0);
        result = 31 * result + (rake != null ? rake.hashCode() : 0);
        result = 31 * result + (totalBet != null ? totalBet.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HandResult{" +
                "playerId=" + playerId +
                ", netWin=" + netWin +
                ", totalWin=" + totalWin +
                ", rake=" + rake +
                ", totalBet=" + totalBet +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
