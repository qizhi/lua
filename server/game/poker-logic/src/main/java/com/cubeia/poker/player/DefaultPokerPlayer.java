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

package com.cubeia.poker.player;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.blinds.MissedBlindsStatus;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultPokerPlayer implements PokerPlayer {

    private static final Logger log = LoggerFactory.getLogger(DefaultPokerPlayer.class);

    protected static final long serialVersionUID = 74353817602536715L;

    protected ActionRequest actionRequest;

    protected int playerId;
    
    protected String screenname;
    
    protected int operatorId;

    protected int seatId;

    protected Hand pocketCards = new Hand();

    protected Set<Card> publicPocketCards = new HashSet<Card>();

    protected Set<Card> privatePocketCards = new HashSet<Card>();

    protected boolean hasActed;

    protected boolean hasFolded;

    protected boolean hasOption;

    protected boolean sittingOutNextHand = false;

    protected SitOutStatus sitOutStatus;

    protected boolean hasPostedEntryBet;

    protected boolean exposingPocketCards;

    private BigDecimal requestedBuyInAmount  = BigDecimal.ZERO;

    protected boolean disconnectTimeoutUsed = false;

    /**
     * the unused amount of chips kept by the player
     */
    private BigDecimal balance = BigDecimal.ZERO;

    private BigDecimal startingBalance = BigDecimal.ZERO;

    /**
     * the money reserved in wallet but not yet available to the player
     */
    private BigDecimal balanceNotInHand = BigDecimal.ZERO;

    /**
     * the amount reserved for a betting action
     */
    protected BigDecimal betStack = BigDecimal.ZERO;

    private boolean sitInAfterSuccessfulBuyIn;

    private Long sitOutTimestamp;

    private boolean buyInRequestActive;

    /**
     * Indicates whether this player has any missed blinds.
     */
    private MissedBlindsStatus missedBlindsStatus = MissedBlindsStatus.NOT_ENTERED_YET;

    /**
     * This flag keeps track of whether this player is allowed to raise or not. It's used to cover the
     * special case where a player who has acted is not allowed to raise when there's an incomplete bet.
     */
    private boolean canRaise = false;

    private boolean away;

    public DefaultPokerPlayer(int id) {
        playerId = id;
    }

    public String toString() {
        String sitOutSince = sitOutTimestamp == null ? "" : ":" + (System.currentTimeMillis() - sitOutTimestamp + "ms");
        return "pid[" + playerId + "] seat[" + seatId + "] " +
                "balance[" + balance + "] balanceNotInHand[" + balanceNotInHand + "] " +
                "buyInRequestActive[" + buyInRequestActive + "] " +
                "requestedBuyInAmount[" + requestedBuyInAmount + "] " +
                "sitout[" + getSitOutStatus() + sitOutSince + "] sitOutStatus[" + sitOutStatus + "] " +
                "folded[" + hasFolded + "] hasActed[" + hasActed + "] allIn[" + isAllIn() + "] " +
                "hasPostedEntryBet[" + hasPostedEntryBet + "]";
    }

    public void clearActionRequest() {
        // log.trace("Clear Action for player "+playerId);
        actionRequest = new ActionRequest();
        actionRequest.setPlayerId(playerId);
    }

    public int getId() {
        return playerId;
    }
    
    @Override
    public String getScreenname() {
		return screenname;
	}
    
    public void setScreenname(String screenname) {
		this.screenname = screenname;
	}
    
    public Hand getPocketCards() {
        return new Hand(pocketCards);
    }

    @Override
    public Set<Card> getPublicPocketCards() {
        return new HashSet<Card>(publicPocketCards);
    }

    public Set<Card> getPrivatePocketCards() {
        return privatePocketCards;
    }

    public ActionRequest getActionRequest() {
        return actionRequest;
    }

    public int getSeatId() {
        return seatId;
    }

    @Override
    public int getOperatorId() {
        return operatorId;
    }

    @Override
    public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}
    
    @Override
    public boolean isSittingIn() {
        return !isSittingOut();
    }

    @Override
    public boolean isSittingOut() {
        return sitOutStatus == SitOutStatus.SITTING_OUT;
    }

    @Override
    public boolean hasActed() {
        return hasActed;
    }

    @Override
    public boolean hasFolded() {
        return hasFolded;
    }

    @Override
    public void setActionRequest(ActionRequest actionRequest) {
        // log.trace("Setting action request " + actionRequest + " on player " + getTableId());
        this.actionRequest = actionRequest;
    }

    @Override
    public void setHasActed(boolean b) {
        this.hasActed = b;
    }

    @Override
    public void setHasFolded(boolean b) {
        this.hasFolded = b;
    }

    @Override
    public void setHasOption(boolean b) {
        hasOption = b;
    }

    @Override
    public boolean hasOption() {
        return hasOption;
    }

    @Override
    public void addPocketCard(Card card, boolean publicCard) {
        pocketCards.addCard(card);
        if (publicCard) {
            publicPocketCards.add(card);
        } else {
            privatePocketCards.add(card);
        }
    }

    @Override
    public void discard(List<Integer> cardsToDiscard) {
        log.debug("Cards before discarding: " + getPocketCards());
        for (Integer cardNumber : cardsToDiscard) {
            discardCard(cardNumber);
        }
        log.debug("Cards after discarding: " + getPocketCards());
    }

    private void discardCard(Integer cardNumber) {
        pocketCards.removeCard(cardNumber);
    }

    @Override
    public void clearHand() {
        pocketCards.clear();
        publicPocketCards.clear();
        privatePocketCards.clear();
        exposingPocketCards = false;
    }

    @Override
    public void enableOption(PossibleAction option) {
        if (actionRequest == null) {
            actionRequest = new ActionRequest();
            actionRequest.setPlayerId(playerId);
        }

        actionRequest.enable(option);
    }

    @Override
    public SitOutStatus getSitOutStatus() {
        return sitOutStatus;
    }

    @Override
    public boolean hasPostedEntryBet() {
        return hasPostedEntryBet && missedBlindsStatus == MissedBlindsStatus.NO_MISSED_BLINDS;
    }

    /**
     * If the player was not already sitting out we will
     * not only set the sit out status, but also set the
     * time stamp for sitting out to the time when this
     * method was called.
     */
    @Override
    public void setSitOutStatus(SitOutStatus status) {
        this.sitOutStatus = status;
        if (status == SitOutStatus.SITTING_OUT) {
            log.debug("Player " + playerId + " is now sitting out.");
            sitOutTimestamp = System.currentTimeMillis();
            sittingOutNextHand = false;
        }
    }

    @Override
    public void setHasPostedEntryBet(boolean status) {
        hasPostedEntryBet = status;
        missedBlindsStatus = MissedBlindsStatus.NO_MISSED_BLINDS;
    }

    @Override
    public void sitIn() {
        sitOutStatus = SitOutStatus.SITTING_IN;
        sitOutTimestamp = null;
    }

    @Override
    public void clearBalance() {
        this.balance = BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public void addChips(BigDecimal chips) {
        checkArgument(chips.compareTo(BigDecimal.ZERO) >= 0, "PokerPlayer[" + playerId + "] - " + String.format("Tried to add negative amount of chips (%s)", chips));
        this.balance = balance.add(chips);
    }

    /**
     * Takes chips from the given player, without adding them to his bet stack.
     *
     */
    @Override
    public void takeChips(BigDecimal amount) {
        checkArgument(amount.compareTo(balance) <= 0, "PokerPlayer[" + playerId + "] - " + String.format("Amount (%s) is bigger than balance (%s)", amount, balance));
        checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0, "Chips must be positive, was " + amount);
        balance = balance.subtract(amount);
    }

    @Override
    public BigDecimal takeChipsOrGoAllIn(BigDecimal amount) {
        if (amount.compareTo(balance) >= 0) {
            log.debug("Balance {} >= amount {}, going all-in.", balance, amount);
            amount = balance;
        }
        takeChips(amount);
        return amount;
    }

    @Override
    public void addBet(BigDecimal bet) {
        checkArgument(bet.compareTo(balance) <= 0, "PokerPlayer[" + playerId + "] - " + String.format("Bet (%s) is bigger than balance (%s)", bet, balance));
        checkArgument(bet.compareTo(BigDecimal.ZERO) >= 0, "Chips must be positive, was " + bet);
        balance= balance.subtract(bet);
        betStack = betStack.add(bet);
    }

    @Override
    public void addBetOrGoAllIn(BigDecimal amount) {
        if (amount.compareTo(balance)>= 0) {
            log.debug("Balance {} >= amount {}, going all-in.", balance, amount);
            amount = balance;
        }
        addBet(amount);
    }

    @Override
    public void saveStartingBalance() {
        this.startingBalance = balance;
    }

    @Override
    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    @Override
    public BigDecimal getBetStack() {
        return betStack;
    }

    @Override
    public void removeFromBetStack(BigDecimal amount) {
        if (amount.compareTo(betStack) > 0) {
            throw new IllegalArgumentException("PokerPlayer[" + playerId + "] - " + String.format("Amount to remove from bet (%s) is bigger than bet stack (%s)", amount, betStack));
        }
        betStack = betStack.subtract(amount);
    }


    @Override
    public void returnBetStackToBalance() {
        balance = balance.add(betStack);
        betStack = BigDecimal.ZERO;
    }

    @Override
    public void returnBetStackAmountToBalance(BigDecimal amount) {
        if (amount.compareTo(betStack) > 0) {
            throw new IllegalArgumentException("PokerPlayer[" + playerId + "] - " + String.format("Amount to return from bet (%s) is bigger than bet stack (%s)", amount, betStack));
        }
        balance = balance.add(amount);
        betStack = betStack.subtract(amount);
    }

    @Override
    public boolean isAllIn() {
        return getBalance().compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean isSittingOutNextHand() {
        return sittingOutNextHand;
    }

    @Override
    public void setCanRaise(boolean canRaise) {
        this.canRaise = canRaise;
    }

    @Override
    public boolean canRaise() {
        return canRaise;
    }

    @Override
    public void setSittingOutNextHand(boolean b) {
        sittingOutNextHand = b;
    }

    @Override
    public boolean setAway(boolean away) {
        return this.away = away;
    }

    @Override
    public boolean isAway() {
        return away;
    }

    @Override
    public BigDecimal getBalanceNotInHand() {
        return balanceNotInHand;
    }

    @Override
    public void addNotInHandAmount(BigDecimal amount) {
        balanceNotInHand = balanceNotInHand.add(amount);
    }

    /**
     *
     * @param maxBuyIn, the total resulting balance should not be higher than this
     * @return true if the player's balance was updated, false otherwise.
     */
    @Override
    public boolean commitBalanceNotInHand(BigDecimal maxBuyIn) {
        log.debug("Committing balance not in hand, maxBuyin: " + maxBuyIn);
        // TODO: This is broken. If we allow the player to perform an add-on, but then the player happens to win a lot of chips during that hand,
        //       these chips will be stuck as "balanceNotInHand" until his balance drops low enough, at which point suddenly the player would get
        //       those chips. Madness.
        boolean hasPending = balanceNotInHand.compareTo(BigDecimal.ZERO) > 0;
        if (hasPending && balance.compareTo(maxBuyIn) < 0) {
            BigDecimal allowedAmount = maxBuyIn.subtract(balance);
            if (balanceNotInHand.compareTo(allowedAmount) > 0) {
                balance = balance.add(allowedAmount);
                balanceNotInHand = balanceNotInHand.subtract(allowedAmount);
                log.debug("committing pending balance for player: " + playerId + " committedValue: " + allowedAmount + " new balance: " + balance + " new pending balance: " + balanceNotInHand);
            } else {
                balance = balance.add(balanceNotInHand);
                log.debug("committing all pending balance for player: " + playerId + " committedValue: " + balanceNotInHand + " new balance: " + balance + " new pending balance: " + 0);
                balanceNotInHand = BigDecimal.ZERO;
            }
            saveStartingBalance();
            return true;
        }
        return false;
    }

    @Override
    public BigDecimal getPendingBalanceSum() {
        return getBalanceNotInHand().add(getRequestedBuyInAmount());
    }

    @Override
    public boolean isSitInAfterSuccessfulBuyIn() {
        return sitInAfterSuccessfulBuyIn;
    }

    @Override
    public void setSitInAfterSuccessfulBuyIn(boolean sitIn) {
        this.sitInAfterSuccessfulBuyIn = sitIn;
    }

    @Override
    public Long getSitOutTimestamp() {
        return sitOutTimestamp;
    }

    @Override
    public boolean isExposingPocketCards() {
        return exposingPocketCards;
    }

    public void setExposingPocketCards(boolean exposingPocketCards) {
        this.exposingPocketCards = exposingPocketCards;
    }

    @Override
    public void resetBeforeNewHand() {
        clearActionRequest();
        clearHand();
        setHasActed(false);
        setHasFolded(false);
    }

    @Override
    public BigDecimal getRequestedBuyInAmount() {
        return requestedBuyInAmount;
    }

    @Override
    public void addRequestedBuyInAmount(BigDecimal buyInAmount) {
        requestedBuyInAmount = requestedBuyInAmount.add(buyInAmount);
        log.debug("added {} as future buy in amount for player {}, total future buy in amount = {}",
                new Object[]{buyInAmount, playerId, requestedBuyInAmount});
    }

    @Override
    public void setRequestedBuyInAmount(BigDecimal amount) {
        log.debug("setting buy in amount for player {} to: {}, was: {}",
                new Object[]{playerId, amount, requestedBuyInAmount});
        requestedBuyInAmount = amount;
    }

    @Override
    public void clearRequestedBuyInAmountAndRequest() {
        requestedBuyInAmount = BigDecimal.ZERO;
        buyInRequestActive = false;
    }

    @Override
    public void buyInRequestActive() {
        buyInRequestActive = true;
    }

    @Override
    public boolean isBuyInRequestActive() {
        return buyInRequestActive;
    }

    @Override
    public void setMissedBlindsStatus(MissedBlindsStatus missedBlindsStatus) {
        this.missedBlindsStatus = missedBlindsStatus;
    }

    @Override
    public MissedBlindsStatus getMissedBlindsStatus() {
        return missedBlindsStatus;
    }

    @VisibleForTesting
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
