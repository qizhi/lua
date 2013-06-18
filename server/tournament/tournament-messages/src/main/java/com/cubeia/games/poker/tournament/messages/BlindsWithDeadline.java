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

package com.cubeia.games.poker.tournament.messages;

import java.io.Serializable;
import java.math.BigDecimal;

public class BlindsWithDeadline implements Serializable {

    private final BigDecimal smallBlindAmount;
    private final BigDecimal bigBlindAmount;
    private final BigDecimal anteAmount;
    private final int durationInMinutes;
    private final boolean isBreak;
    private final long deadline;

    public BlindsWithDeadline(BigDecimal smallBlindAmount, BigDecimal bigBlindAmount, BigDecimal anteAmount, int durationInMinutes, boolean isBreak, long deadline) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.anteAmount = anteAmount;
        this.durationInMinutes = durationInMinutes;
        this.isBreak = isBreak;
        this.deadline = deadline;
    }

    public BigDecimal getAnteAmount() {
        return anteAmount;
    }

    public BigDecimal getBigBlindAmount() {
        return bigBlindAmount;
    }

    public long getDeadline() {
        return deadline;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public BigDecimal getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public boolean isBreak() {
        return isBreak;
    }
}
