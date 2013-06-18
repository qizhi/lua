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

public class Amount implements Serializable {

    private static final long serialVersionUID = -1731897823595502084L;

	public static enum Type {
        BET,
        RAISE,
        STACK,
        OTHER
    }

    public Amount() {
    }

    public static Amount bet(BigDecimal amount) {
        return new Amount(Type.BET, amount);
    }

    public static Amount raise(BigDecimal amount) {
        return new Amount(Type.RAISE, amount);
    }

    public static Amount stack(BigDecimal amount) {
        return new Amount(Type.STACK, amount);
    }

    public static Amount other(BigDecimal amount) {
        return new Amount(Type.OTHER, amount);
    }

    private Type type;
    private BigDecimal amount;

    private Amount(Type type, BigDecimal amount) {
        this.type = type;
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Amount amount1 = (Amount) o;

        if (amount != null ? !amount.equals(amount1.amount) : amount1.amount != null) return false;
        if (type != amount1.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Amount{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }
}
