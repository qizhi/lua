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

package com.cubeia.poker.action;

import com.cubeia.poker.handhistory.api.Amount;
import com.cubeia.poker.handhistory.api.PlayerAction;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * Represents an action performed by a player.
 */
public class PokerAction implements Serializable {

    private static final long serialVersionUID = -3457732987197089379L;

    private final Integer playerId;

    private final PokerActionType actionType;

    private boolean timeout = false;

    private static final BigDecimal MINUS_ONE = new BigDecimal(-1) ;

    private BigDecimal betAmount = MINUS_ONE;

    private BigDecimal raiseAmount = MINUS_ONE;

    private BigDecimal stackAmount = MINUS_ONE;



    public PokerAction(Integer playerId, PokerActionType actionType) {
        this.playerId = playerId;
        this.actionType = actionType;
    }

    /**
     * @param playerToAct
     * @param check
     * @param amount
     */
    public PokerAction(int playerToAct, PokerActionType check, BigDecimal amount) {
        this(playerToAct, check);
        this.betAmount = amount;
    }

    /**
     * @param playerToAct
     * @param check
     * @param timeout,    true if this is a result of a timeout
     */
    public PokerAction(int playerToAct, PokerActionType check, boolean timeout) {
        this(playerToAct, check);
        this.timeout = timeout;
    }

    public PlayerAction translate() {
        PlayerAction a = new PlayerAction(getPlayerId());
        a.setAction(getActionType().translate());
        if (getBetAmount().compareTo(MINUS_ONE) != 0) {
            a.setAmount(Amount.bet(getBetAmount()));
        } else if (getRaiseAmount().compareTo(MINUS_ONE) != 0) {
            a.setAmount(Amount.raise(getRaiseAmount()));
        } else if (getStackAmount().compareTo(MINUS_ONE) != 0) {
            a.setAmount(Amount.stack(getStackAmount()));
        }
        a.setTimeout(isTimeout());
        return a;
    }

    public String toString() {
        return "PokerAction - pid[" + playerId + "] type[" + actionType + "] timeout[" + timeout + "] amount[" + betAmount + "]";
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public PokerActionType getActionType() {
        return actionType;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal amount) {
        this.betAmount = amount;
    }

    /**
     * Is this action a result of a timeout?
     *
     * @return true if this is a timeout
     */
    public boolean isTimeout() {
        return timeout;
    }

    public BigDecimal getRaiseAmount() {
        return raiseAmount;
    }

    public void setRaiseAmount(BigDecimal raiseAmount) {
        this.raiseAmount = raiseAmount;
    }

    public BigDecimal getStackAmount() {
        return stackAmount;
    }

    public void setStackAmount(BigDecimal stackAmount) {
        this.stackAmount = stackAmount;
    }
}
