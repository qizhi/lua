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

package com.cubeia.backoffice.accounting.core.entity;

import static java.lang.Math.max;
import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_DOWN;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents an exchange rate from one currency to another.
 * The rate represents the amount in target currency you get by one unit of source currency:
 * amount_source * rate = amount_target
 * 
 * @author w
 */
@Entity
public class CurrencyRate {
    
    private static final int SCALE = 7;

    private Long id;
    
    private String sourceCurrencyCode;
    private String targetCurrencyCode;
    
    private BigDecimal rate;
    
    private Date timestamp;
    
    private boolean calculated = false;
    
    public CurrencyRate() {}
    
    public CurrencyRate(String sourceCurrencyCode, String targetCurrencyCode, BigDecimal rate, Date timestamp) {
        super();
        this.sourceCurrencyCode = sourceCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        checkRateIsNotZero(rate);
        this.rate = rate;
        this.timestamp = timestamp;
    }

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The "from" currency code.
     * @return currency code
     */
    @Column(name = "sourceCC", nullable = false)
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
    @Column(name = "targetCC", nullable = false)
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
    @Column(nullable = false, precision=10, scale=SCALE)
    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
    	checkRateIsNotZero(rate);
        this.rate = rate;
    }
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Inverts the given rate by switching the source and target currency codes and inverting the 
     * rate value. The scale of the new rate value will be at least {@value #SCALE}.
     * The {@link #isCalculated()} of the returned rate will be true.
     * @return the inverted rate
     */
    @Transient
    public CurrencyRate invert() {
        BigDecimal invertedRate = ONE.divide(getRate(), max(getRate().scale(), SCALE), HALF_DOWN);
        CurrencyRate newRate = new CurrencyRate(targetCurrencyCode, sourceCurrencyCode, invertedRate, timestamp);
        newRate.calculated = true;
        return newRate;
    }
    
    /**
     * Combines this rate with the given rate and returns the new calculated rate.
     * The {@link #getTargetCurrencyCode()} of this rate must be equal to the {@link #getSourceCurrencyCode()} of
     * the other rate.
     * The returned rate will have {@link #isCalculated()} true.
     * The timestamp will be the earliest of the given rates timestamps.
     * The scale of the calculates rate is {@value #SCALE}.
     * @param otherRate the rate to combine with
     * @return the combined rate
     */
    @Transient
    public CurrencyRate combine(CurrencyRate otherRate) {
        if (!targetCurrencyCode.equals(otherRate.sourceCurrencyCode)) {
            throw new IllegalArgumentException("this target currency doesn't match other rates source currency");
        }
        
        BigDecimal combinedRate = getRate().multiply(otherRate.getRate()).setScale(SCALE, HALF_DOWN);
        CurrencyRate newRate = new CurrencyRate(sourceCurrencyCode, otherRate.targetCurrencyCode, combinedRate, 
                new Date(Math.min(timestamp.getTime(), otherRate.getTimestamp().getTime())));
        newRate.calculated = true;
        return newRate;
    }
    
    @Transient
    public boolean isCalculated() {
        return calculated;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rate == null) ? 0 : rate.hashCode());
        result = prime * result + ((sourceCurrencyCode == null) ? 0 : sourceCurrencyCode.hashCode());
        result = prime * result + ((targetCurrencyCode == null) ? 0 : targetCurrencyCode.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        CurrencyRate other = (CurrencyRate) obj;
        if (rate == null) {
            if (other.rate != null)
                return false;
        } else if (!rate.equals(other.rate))
            return false;
        if (sourceCurrencyCode == null) {
            if (other.sourceCurrencyCode != null)
                return false;
        } else if (!sourceCurrencyCode.equals(other.sourceCurrencyCode))
            return false;
        if (targetCurrencyCode == null) {
            if (other.targetCurrencyCode != null)
                return false;
        } else if (!targetCurrencyCode.equals(other.targetCurrencyCode))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }

    
    // --- PRIVATE METHODS --- //

    private static void checkRateIsNotZero(BigDecimal rate) {
		if(rate == null || rate.compareTo(new BigDecimal("0")) == 0) {
			throw new IllegalArgumentException("Rate cannot be null or zero");
		}
	}
}
