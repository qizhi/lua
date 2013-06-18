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

package com.cubeia.poker.pot;

import com.cubeia.poker.player.PokerPlayer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Transition of money from a player to a pot.
 * If the amount is negative the direction is from pot to player.
 *
 * @author w
 */
@SuppressWarnings("serial")
public class PotTransition implements Serializable {

    private final PokerPlayer player;
    private final Pot pot;
    private final BigDecimal amount;
    private final boolean fromChipStackToPlayer;

    /**
     * Needed by JBoss Serialization
     */
    @SuppressWarnings("unused")
    private PotTransition() {
        player = null;
        pot = null;
        amount = null;
        fromChipStackToPlayer = false;
    }

    public PotTransition(PokerPlayer player, Pot pot, BigDecimal amount) {
        this.player = player;
        this.pot = pot;
        this.amount = amount;
        fromChipStackToPlayer = false;
    }

    private PotTransition(PokerPlayer player, BigDecimal amount) {
        fromChipStackToPlayer = true;
        this.amount = amount;
        this.player = player;
        this.pot = null;
    }

    public boolean isFromPlayerToPot() {
        return getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isFromBetStackToPlayer() {
        return fromChipStackToPlayer;
    }

    public PokerPlayer getPlayer() {
        return player;
    }

    public Pot getPot() {
        return pot;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PotTransition that = (PotTransition) o;

        if (fromChipStackToPlayer != that.fromChipStackToPlayer) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (player != null ? !player.equals(that.player) : that.player != null) return false;
        if (pot != null ? !pot.equals(that.pot) : that.pot != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (pot != null ? pot.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (fromChipStackToPlayer ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {


        if (fromChipStackToPlayer) {
            return "pot transition player " + player.getId() + ": amount " + amount + " -> pot: <null> fromChipStackToPlayer: true";
        } else {
            return "pot transition player " + player.getId() + ": amount " + amount + " -> pot: " + pot.getId() + " fromChipStackToPlayer: false";
        }

    }

    public static PotTransition createTransitionFromBetStackToPlayer(PokerPlayer player, BigDecimal amount) {
        return new PotTransition(player, amount);
    }
}
