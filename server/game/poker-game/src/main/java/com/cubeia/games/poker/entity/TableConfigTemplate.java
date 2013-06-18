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

package com.cubeia.games.poker.entity;

import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.variant.PokerVariant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
public class TableConfigTemplate implements Serializable {

    private static final long serialVersionUID = -2416951532409186240L;

    public static final int DEF_MIN_BUY_IN_ANTE_MULTIPLIER = 10;
    public static final int DEF_MAX_BUY_IN_ANTE_MULTIPLIER = 100;

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private PokerVariant variant;

    @Column(nullable = false)
    private BigDecimal ante;

    @Column(nullable = false)
    private BigDecimal smallBlind;

    @Column(nullable = false)
    private BigDecimal bigBlind;

    @Column(nullable = false)
    private int minBuyInMultiplier = DEF_MIN_BUY_IN_ANTE_MULTIPLIER;

    @Column(nullable = false)
    private int maxBuyInMultiplier = DEF_MAX_BUY_IN_ANTE_MULTIPLIER;

    @Column(nullable = false)
    private BigDecimal minBuyIn;

    @Column(nullable = true)
    private BigDecimal maxBuyIn;

    @Column(nullable = false)
    private int minEmptyTables;

    @Column(nullable = false)
    private int minTables;

    @Column(nullable = false)
    private int seats;

    @Column(nullable = false)
    private BetStrategyType betStrategy;

    @ManyToOne()
    private TimingProfile timing;

    @ManyToOne()
    private RakeSettings rakeSettings;

    @Column(nullable = false)
    private long ttl;

    @Column
    private String currency;

    public long getTTL() {
        return ttl;
    }

    public void setTTL(long ttl) {
        this.ttl = ttl;
    }

    public RakeSettings getRakeSettings() {
        return rakeSettings;
    }

    public void setRakeSettings(RakeSettings rakeSettings) {
        this.rakeSettings = rakeSettings;
    }

    public TimingProfile getTiming() {
        return timing;
    }

    public void setTiming(TimingProfile timing) {
        this.timing = timing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PokerVariant getVariant() {
        return variant;
    }

    public void setVariant(PokerVariant variant) {
        this.variant = variant;
    }

    public BigDecimal getAnte() {
        if (ante != null) {
        	return ante;
        } else {
        	return BigDecimal.ZERO;
        }
    }

    public void setAnte(BigDecimal ante) {
        this.ante = ante;
    }

    public int getMinBuyInMultiplier() {
        return minBuyInMultiplier;
    }

    public void setMinBuyInMultiplier(int minBuyInMultiplier) {
        this.minBuyInMultiplier = minBuyInMultiplier;
    }

    public int getMaxBuyInMultiplier() {
        return maxBuyInMultiplier;
    }

    public void setMaxBuyInMultiplier(int maxBuyInMultiplier) {
        this.maxBuyInMultiplier = maxBuyInMultiplier;
    }

    public int getMinEmptyTables() {
        return minEmptyTables;
    }

    public void setMinEmptyTables(int minEmptyTables) {
        this.minEmptyTables = minEmptyTables;
    }

    public int getMinTables() {
        return minTables;
    }

    public void setMinTables(int minTables) {
        this.minTables = minTables;
    }

    public BigDecimal getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(BigDecimal smallBlind) {
        this.smallBlind = smallBlind;
    }

    public BigDecimal getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(BigDecimal bigBlind) {
        this.bigBlind = bigBlind;
    }

    public BetStrategyType getBetStrategy() {
        return betStrategy;
    }

    public void setBetStrategy(BetStrategyType betStrategy) {
        this.betStrategy = betStrategy;
    }




    @Override
    public String toString() {
        return "TableConfigTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", variant=" + variant +
                ", ante=" + ante +
                ", smallBlind=" + smallBlind +
                ", bigBlind=" + bigBlind +
                ", minBuyInMultiplier=" + minBuyInMultiplier +
                ", maxBuyInMultiplier=" + maxBuyInMultiplier +
                ", minEmptyTables=" + minEmptyTables +
                ", minTables=" + minTables +
                ", seats=" + seats +
                ", betStrategy=" + betStrategy +
                ", timing=" + timing +
                ", rakeSettings=" + rakeSettings +
                ", ttl=" + ttl +
                '}';
    }

    public BigDecimal getMinBuyIn() {
        return minBuyIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableConfigTemplate that = (TableConfigTemplate) o;

        if (id != that.id) return false;
        if (maxBuyInMultiplier != that.maxBuyInMultiplier) return false;
        if (minBuyInMultiplier != that.minBuyInMultiplier) return false;
        if (minEmptyTables != that.minEmptyTables) return false;
        if (minTables != that.minTables) return false;
        if (seats != that.seats) return false;
        if (ttl != that.ttl) return false;
        if (ante != null ? !ante.equals(that.ante) : that.ante != null) return false;
        if (betStrategy != that.betStrategy) return false;
        if (bigBlind != null ? !bigBlind.equals(that.bigBlind) : that.bigBlind != null) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (maxBuyIn != null ? !maxBuyIn.equals(that.maxBuyIn) : that.maxBuyIn != null) return false;
        if (minBuyIn != null ? !minBuyIn.equals(that.minBuyIn) : that.minBuyIn != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rakeSettings != null ? !rakeSettings.equals(that.rakeSettings) : that.rakeSettings != null) return false;
        if (smallBlind != null ? !smallBlind.equals(that.smallBlind) : that.smallBlind != null) return false;
        if (timing != null ? !timing.equals(that.timing) : that.timing != null) return false;
        if (variant != that.variant) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (variant != null ? variant.hashCode() : 0);
        result = 31 * result + (ante != null ? ante.hashCode() : 0);
        result = 31 * result + (smallBlind != null ? smallBlind.hashCode() : 0);
        result = 31 * result + (bigBlind != null ? bigBlind.hashCode() : 0);
        result = 31 * result + minBuyInMultiplier;
        result = 31 * result + maxBuyInMultiplier;
        result = 31 * result + (minBuyIn != null ? minBuyIn.hashCode() : 0);
        result = 31 * result + (maxBuyIn != null ? maxBuyIn.hashCode() : 0);
        result = 31 * result + minEmptyTables;
        result = 31 * result + minTables;
        result = 31 * result + seats;
        result = 31 * result + (betStrategy != null ? betStrategy.hashCode() : 0);
        result = 31 * result + (timing != null ? timing.hashCode() : 0);
        result = 31 * result + (rakeSettings != null ? rakeSettings.hashCode() : 0);
        result = 31 * result + (int) (ttl ^ (ttl >>> 32));
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        return result;
    }

    public void setMinBuyIn(BigDecimal minBuyIn) {

        this.minBuyIn = minBuyIn;
    }

    public BigDecimal getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setMaxBuyIn(BigDecimal maxBuyIn) {
        this.maxBuyIn = maxBuyIn;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
