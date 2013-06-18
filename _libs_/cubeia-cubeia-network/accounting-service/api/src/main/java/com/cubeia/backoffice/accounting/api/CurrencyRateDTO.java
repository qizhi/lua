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

package com.cubeia.backoffice.accounting.api;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represents an exchange rate from one currency to another.
 * The rate represents the amount in target currency you get by one unit of source currency:
 * amount_source * rate = amount_target
 * 
 * @author w
 */
@XmlRootElement(name="currencyRate")
public class CurrencyRateDTO {
    
    private String sourceCurrencyCode;
    private String targetCurrencyCode;
    
    private BigDecimal rate;
    
    private Date timestamp;
    
    public CurrencyRateDTO() {};
    
    public CurrencyRateDTO(String sourceCurrencyCode, String targetCurrencyCode, BigDecimal rate, Date timestamp) {
        this.sourceCurrencyCode = sourceCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    /**
     * The "from" currency code.
     * @return currency code
     */
    @XmlElement(name = "sourceCC", required = true)
    public String getSourceCurrencyCode() {
        return sourceCurrencyCode;
    }

    public void setSourceCurrencyCode(String sourceCurrencyCode) {
        this.sourceCurrencyCode = sourceCurrencyCode;
    }

    /**
     * The "to" currency code.
     * @return currency code
     */
    @XmlElement(name = "targetCC", required = true)
    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }

    public void setTargetCurrencyCode(String targetCurrencyCode) {
        this.targetCurrencyCode = targetCurrencyCode;
    }

    /**
     * The exchange rate. The exchange rate is the value in the target currency for one unit of source currency.
     * Invariant: amountInSourceCurrency * rate = amountInTargetCurrency 
     * @return the exchange rate
     */
    @XmlElement(name = "rate", required = true)
    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    @XmlElement(name = "timestamp", required = true)
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
}
