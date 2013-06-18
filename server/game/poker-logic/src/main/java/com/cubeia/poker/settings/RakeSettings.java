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

package com.cubeia.poker.settings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
public class RakeSettings implements Serializable {

    private static final long serialVersionUID = -5351869522070637864L;

    public static final BigDecimal DEFAULT_RAKE_FRACTION = new BigDecimal("0.01");
    public static final BigDecimal DEFAULT_RAKE_LIMIT = new BigDecimal("5");
    public static final BigDecimal DEFAULT_RAKE_LIMIT_HEADS_UP = new BigDecimal("1.50");

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal rakeFraction2Plus = DEFAULT_RAKE_FRACTION;

    @Column(nullable = false)
    private BigDecimal rakeLimit2Plus = DEFAULT_RAKE_LIMIT_HEADS_UP;

    @Column(nullable = false)
    private BigDecimal rakeFraction3Plus = DEFAULT_RAKE_FRACTION;

    @Column(nullable = false)
    private BigDecimal rakeLimit3Plus = DEFAULT_RAKE_LIMIT;

    @Column(nullable = false)
    private BigDecimal rakeFraction5Plus = DEFAULT_RAKE_FRACTION;

    @Column(nullable = false)
    private BigDecimal rakeLimit5Plus = DEFAULT_RAKE_LIMIT;

    private boolean archived;

    public RakeSettings() {
    }

    public RakeSettings(BigDecimal fraction, BigDecimal limit, BigDecimal headsUpLimit) {
        rakeFraction2Plus = rakeFraction3Plus = rakeFraction5Plus = fraction;
        rakeLimit2Plus = headsUpLimit;
        rakeLimit3Plus = rakeLimit5Plus = limit;
    }

    public static RakeSettings createDefaultRakeSettings(BigDecimal rakeFraction) {
        RakeSettings settings = new RakeSettings();
        settings.rakeFraction2Plus = settings.rakeFraction3Plus = settings.rakeFraction5Plus = rakeFraction;
        settings.rakeLimit2Plus = settings.rakeLimit3Plus = settings.rakeLimit5Plus = new BigDecimal(Long.MAX_VALUE);
        settings.name = "temporarySetting";
        return settings;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRakeFraction2Plus() {
        return rakeFraction2Plus;
    }

    public void setRakeFraction2Plus(BigDecimal rakeFraction2Plus) {
        this.rakeFraction2Plus = rakeFraction2Plus;
    }

    public BigDecimal getRakeLimit2Plus() {
        return rakeLimit2Plus;
    }

    public void setRakeLimit2Plus(BigDecimal rakeLimit2Plus) {
        this.rakeLimit2Plus = rakeLimit2Plus;
    }

    public BigDecimal getRakeFraction3Plus() {
        return rakeFraction3Plus;
    }

    public void setRakeFraction3Plus(BigDecimal rakeFraction3Plus) {
        this.rakeFraction3Plus = rakeFraction3Plus;
    }

    public BigDecimal getRakeLimit3Plus() {
        return rakeLimit3Plus;
    }

    public void setRakeLimit3Plus(BigDecimal rakeLimit3Plus) {
        this.rakeLimit3Plus = rakeLimit3Plus;
    }

    public BigDecimal getRakeFraction5Plus() {
        return rakeFraction5Plus;
    }

    public void setRakeFraction5Plus(BigDecimal rakeFraction5Plus) {
        this.rakeFraction5Plus = rakeFraction5Plus;
    }

    public BigDecimal getRakeLimit5Plus() {
        return rakeLimit5Plus;
    }

    public void setRakeLimit5Plus(BigDecimal rakeLimit5Plus) {
        this.rakeLimit5Plus = rakeLimit5Plus;
    }

    public void setRakeFraction(int playersCount, BigDecimal rakeFraction) {
        switch (playersCount) {
            case 2:
                rakeFraction2Plus = rakeFraction;
                return;
            case 3:
            case 4:
                rakeFraction3Plus = rakeFraction;
                return;
            case 5:
            default:
                rakeFraction5Plus = rakeFraction;
        }
    }

    public BigDecimal getRakeFraction(int playersCount) {
        switch (playersCount) {
            case 2:
                return rakeFraction2Plus;
            case 3:
            case 4:
                return rakeFraction3Plus;
            case 5:
            default:
                return rakeFraction5Plus;
        }
    }

    public void setRakeLimit(int playersCount, BigDecimal rakeLimit) {
        switch (playersCount) {
            case 2:
                rakeLimit2Plus = rakeLimit;
                return;
            case 3:
            case 4:
                rakeLimit3Plus = rakeLimit;
                return;
            case 5:
            default:
                rakeLimit5Plus = rakeLimit;
        }
    }

    public BigDecimal getRakeLimit(int playersCount) {
        switch (playersCount) {
            case 2:
                return rakeLimit2Plus;
            case 3:
            case 4:
                return rakeLimit3Plus;
            case 5:
            default:
                return rakeLimit5Plus;
        }
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RakeSettings that = (RakeSettings) o;

        if (archived != that.archived) return false;
        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rakeFraction2Plus != null ? !rakeFraction2Plus.equals(that.rakeFraction2Plus) : that.rakeFraction2Plus != null)
            return false;
        if (rakeFraction3Plus != null ? !rakeFraction3Plus.equals(that.rakeFraction3Plus) : that.rakeFraction3Plus != null)
            return false;
        if (rakeFraction5Plus != null ? !rakeFraction5Plus.equals(that.rakeFraction5Plus) : that.rakeFraction5Plus != null)
            return false;
        if (rakeLimit2Plus != null ? !rakeLimit2Plus.equals(that.rakeLimit2Plus) : that.rakeLimit2Plus != null)
            return false;
        if (rakeLimit3Plus != null ? !rakeLimit3Plus.equals(that.rakeLimit3Plus) : that.rakeLimit3Plus != null)
            return false;
        if (rakeLimit5Plus != null ? !rakeLimit5Plus.equals(that.rakeLimit5Plus) : that.rakeLimit5Plus != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (rakeFraction2Plus != null ? rakeFraction2Plus.hashCode() : 0);
        result = 31 * result + (rakeLimit2Plus != null ? rakeLimit2Plus.hashCode() : 0);
        result = 31 * result + (rakeFraction3Plus != null ? rakeFraction3Plus.hashCode() : 0);
        result = 31 * result + (rakeLimit3Plus != null ? rakeLimit3Plus.hashCode() : 0);
        result = 31 * result + (rakeFraction5Plus != null ? rakeFraction5Plus.hashCode() : 0);
        result = 31 * result + (rakeLimit5Plus != null ? rakeLimit5Plus.hashCode() : 0);
        result = 31 * result + (archived ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RakeSettings{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rakeFraction2Plus=" + rakeFraction2Plus +
                ", rakeLimit2Plus=" + rakeLimit2Plus +
                ", rakeFraction3Plus=" + rakeFraction3Plus +
                ", rakeLimit3Plus=" + rakeLimit3Plus +
                ", rakeFraction5Plus=" + rakeFraction5Plus +
                ", rakeLimit5Plus=" + rakeLimit5Plus +
                ", archived=" + archived +
                '}';
    }
}
