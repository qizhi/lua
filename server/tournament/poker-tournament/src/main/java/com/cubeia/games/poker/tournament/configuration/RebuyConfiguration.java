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

package com.cubeia.games.poker.tournament.configuration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * See {@link com.cubeia.games.poker.tournament.rebuy.RebuySupport} for more information about rebuys.
 *
 */
@Entity
public class RebuyConfiguration implements Serializable {

    @Id
    @GeneratedValue
    private int id;

    private int numberOfRebuysAllowed;

    private boolean addOnsEnabled;

    private int numberOfLevelsWithRebuys;

    private BigDecimal rebuyCost;

    private BigDecimal chipsForRebuy;

    private BigDecimal addOnCost;

    private BigDecimal chipsForAddOn;

    private BigDecimal maxStackForRebuy;

    public RebuyConfiguration() {
    }

    public RebuyConfiguration(int numberOfRebuysAllowed, boolean addOnsEnabled, int numberOfLevelsWithRebuys, BigDecimal rebuyCost, BigDecimal chipsForRebuy,
                              BigDecimal addOnCost, BigDecimal chipsForAddOn) {
        this.numberOfRebuysAllowed = numberOfRebuysAllowed;
        this.addOnsEnabled = addOnsEnabled;
        this.numberOfLevelsWithRebuys = numberOfLevelsWithRebuys;
        this.rebuyCost = rebuyCost;
        this.chipsForRebuy = chipsForRebuy;
        this.addOnCost = addOnCost;
        this.chipsForAddOn = chipsForAddOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getMaxStackForRebuy() {
        return maxStackForRebuy;
    }

    public int getNumberOfRebuysAllowed() {
        return numberOfRebuysAllowed;
    }

    public void setNumberOfRebuysAllowed(int numberOfRebuysAllowed) {
        this.numberOfRebuysAllowed = numberOfRebuysAllowed;
    }

    public boolean isAddOnsEnabled() {
        return addOnsEnabled;
    }

    public void setAddOnsEnabled(boolean addOnsEnabled) {
        this.addOnsEnabled = addOnsEnabled;
    }

    public int getNumberOfLevelsWithRebuys() {
        return numberOfLevelsWithRebuys;
    }

    public void setNumberOfLevelsWithRebuys(int numberOfLevelsWithRebuys) {
        this.numberOfLevelsWithRebuys = numberOfLevelsWithRebuys;
    }

    public BigDecimal getRebuyCost() {
        return rebuyCost;
    }

    public void setRebuyCost(BigDecimal rebuyCost) {
        this.rebuyCost = rebuyCost;
    }

    public BigDecimal getChipsForRebuy() {
        return chipsForRebuy;
    }

    public void setChipsForRebuy(BigDecimal chipsForRebuy) {
        this.chipsForRebuy = chipsForRebuy;
    }

    public BigDecimal getAddOnCost() {
        return addOnCost;
    }

    public void setAddOnCost(BigDecimal addOnCost) {
        this.addOnCost = addOnCost;
    }

    public BigDecimal getChipsForAddOn() {
        return chipsForAddOn;
    }

    public void setChipsForAddOn(BigDecimal chipsForAddOn) {
        this.chipsForAddOn = chipsForAddOn;
    }

    public void setMaxStackForRebuy(BigDecimal maxStackForRebuy) {
        this.maxStackForRebuy = maxStackForRebuy;
    }
}
