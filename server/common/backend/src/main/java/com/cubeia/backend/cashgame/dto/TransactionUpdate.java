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

package com.cubeia.backend.cashgame.dto;

import java.io.Serializable;

import com.cubeia.backend.cashgame.TransactionId;

public class TransactionUpdate implements Serializable {

    private static final long serialVersionUID = -7165224449107966435L;

    private final BalanceUpdate balance;
    private final TransactionId transactionId;

    public TransactionUpdate(TransactionId transactionId, BalanceUpdate balance) {
        this.transactionId = transactionId;
        this.balance = balance;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public BalanceUpdate getBalance() {
        return balance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((balance == null) ? 0 : balance.hashCode());
        result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionUpdate other = (TransactionUpdate) obj;
        if (balance == null) {
            if (other.balance != null) {
                return false;
            }
        } else if (!balance.equals(other.balance)) {
            return false;
        }
        if (transactionId == null) {
            if (other.transactionId != null) {
                return false;
            }
        } else if (!transactionId.equals(other.transactionId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TransactionUpdate");
        sb.append("{balance=").append(balance);
        sb.append(", transactionId=").append(transactionId);
        sb.append('}');
        return sb.toString();
    }
}
