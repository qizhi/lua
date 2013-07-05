package com.cubeia.games.poker.common.money;

import java.io.Serializable;

public class Currency implements Serializable {

    private static final long serialVersionUID = 7629035689178079163L;

    private final String code;
    private final int fractionalDigits;

    public Currency(String code, int fractionalDigits) {
        this.code = code;
        this.fractionalDigits = fractionalDigits;
    }

    public String getCode() {
        return code;
    }

    public int getFractionalDigits() {
        return fractionalDigits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        if (fractionalDigits != currency.fractionalDigits) return false;
        if (code != null ? !code.equals(currency.code) : currency.code != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + fractionalDigits;
        return result;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", fractionalDigits=" + fractionalDigits +
                '}';
    }
}
