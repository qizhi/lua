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

package com.cubeia.games.poker.common.money;

import java.math.BigDecimal;
import java.util.Locale;

public class MoneyFormatter {

    /**
     * Formats an amount in cents as (CURRENCY)amount/100, rounding to an integer if the amount is divisible
     * by 100 and showing two decimals if not. That is:
     *
     * 2012 => $20.12
     * 2000 => $20
     * 2010 => $20.10
     *
     * @param currency the currency to prepend
     * @param moneyInCents the amount, in cents
     * @return a string representation of the money, nicely formatted
     */
    public static String format(String currency, long moneyInCents) {
        String format;
        Number value;
        if (moneyInCents % 100 == 0) {
            format = "%s%d";
            value = moneyInCents / 100;
        } else {
            format = "%s%.2f";
            value = moneyInCents / 100.0;
        }
        return String.format(Locale.US, format, currency, value);
    }

    public static String format(long moneyInCents) {
        return format("", moneyInCents);
    }

    public static String format(BigDecimal valueInCurrency) {
        if (valueInCurrency == null) return "";
        return valueInCurrency.toPlainString();
    }

    public static String format(double amountInCents) {
        return format(BigDecimal.valueOf(amountInCents).movePointLeft(2));
    }

    public static String format(Money money) {
        return format(money.getAmount());
    }
}
