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

package com.cubeia.games.poker.tournament.rebuy;

import com.cubeia.games.poker.tournament.history.HistoryPersister;
import com.cubeia.games.poker.tournament.messages.AddOnPeriodClosed;
import com.cubeia.games.poker.tournament.messages.AddOnsAvailableDuringBreak;
import com.cubeia.games.poker.tournament.messages.OfferRebuy;
import com.cubeia.games.poker.tournament.util.SerializablePredicate;
import com.cubeia.games.poker.tournament.util.TableNotifier;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptySet;

/**
 * This class holds all rebuy related data for a tournament.
 * <p/>
 * Here are some basic properties of rebuy tournaments.
 * <p/>
 * 1. If a tournament is a rebuy tournament, it will allow rebuys during a rebuy period.
 * 2. This period is defined by the number of blinds level during which rebuys are available.
 * 3. Rebuys become available once we reach the first blinds level greater than the defined number of levels with rebuys
 * OR WHEN THE TOURNAMENT REACHES THE MONEY.
 * 4. That is, as soon as a player is out and gets money, no more rebuys are allowed.
 * 5. This is because a new rebuy would change the payouts, but we have already paid out money to at least one player.
 * 6. A rebuy tournament may have an add-on period after the rebuy period is finished.
 * 7. This period should be a break. So if you want rebuys during 60 minutes, followed by add-ons for 5 minutes,
 * you need to define for example 6 blinds levels of 10 minutes and the 7th level AS A BREAK of 5 minutes.
 * 8. If the tournament is configured incorrectly, for example if the 7th level is not a break, there'll be no add-ons.
 * 9. If we are already in the money when the add-on period starts, there'll be no add-ons, for the same reasons as in point 5.
 */
public class RebuySupport implements Serializable {

    public static final RebuySupport NO_REBUYS = new RebuySupport(false, BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO, false, 0, ZERO, ZERO);

    private static final Logger log = Logger.getLogger(RebuySupport.class);

    /**
     * The amount of chips you get when doing a rebuy.
     */
    private BigDecimal rebuyChipsAmount;

    /**
     * The amount of chips you get when doing a add-on.
     */
    private BigDecimal addOnChipsAmount;

    /**
     * The number of rebuys allowed in this tournament. 0 means no rebuys. MAX_INT means unlimited (go ahead and prove me wrong).
     */
    private int maxRebuys = 0;

    /**
     * The maximum stack allowed when performing a rebuy. Usually equal to the starting stack.
     */
    private BigDecimal maxStackForRebuy;

    private boolean rebuysAvailable = false;

    /** Indicates whether this tournament will have an add-on period. */
    private boolean addOnsEnabled = false;

    private int numberOfLevelsWithRebuys = 0;

    private BigDecimal rebuyCost;

    private BigDecimal addOnCost;

    /**
     * Maps playerId to the number of rebuys performed by that player.
     */
    private Map<Integer, Integer> numberOfRebuysPerformed = newHashMap();

    /**
     * Keeps track of the players who have performed an add-on.
     */
    private Set<Integer> performedAddOns = new HashSet<Integer>();

    /**
     * Maps a tableId to the players at that table who have been asked to perform a rebuy.
     */
    private Map<Integer, Set<Integer>> rebuyRequestsPerTable = newHashMap();

    private boolean inTheMoney;

    /** Keeps track of tables that are waiting to start the next hand until any rebuys are settled. */
    private Set<Integer> tablesWaitingForRebuys = new HashSet<Integer>();

    /** Indicates whether the add-on period is active. */
    private boolean addOnPeriodActive = false;

    private transient HistoryPersister historyPersister;
    private transient TableNotifier tableNotifier;

    public RebuySupport(boolean rebuysAvailable, BigDecimal rebuyChipsAmount, BigDecimal addOnChipsAmount, int maxRebuys, BigDecimal maxStackForRebuy, boolean addOnsEnabled,
                        int numberOfLevelsWithRebuys, BigDecimal rebuyCost, BigDecimal addOnCost) {
        this.rebuysAvailable = rebuysAvailable;
        this.rebuyChipsAmount = rebuyChipsAmount;
        this.addOnChipsAmount = addOnChipsAmount;
        this.maxRebuys = maxRebuys;
        this.maxStackForRebuy = maxStackForRebuy;
        this.addOnsEnabled = addOnsEnabled;
        this.numberOfLevelsWithRebuys = numberOfLevelsWithRebuys;
        this.rebuyCost = rebuyCost;
        this.addOnCost = addOnCost;
    }

    private Predicate<Integer> rebuyAllowed = new SerializablePredicate<Integer>() {
        @Override
        public boolean apply(@Nullable Integer playerId) {
            if (inTheMoney || !rebuysAvailable) {
                return false;
            } else {
                return maxRebuys == -1 || numberOfRebuysPerformedBy(playerId) < maxRebuys;
            }
        }
    };

    /**
     * Checks if add-ons are available during this break.
     * <p/>
     * If a tournament has add-ons available, they will be so during the break which occurs just after
     * the rebuys have finished. That is, if we have rebuys during the first 6 blinds levels, then the 7th level
     * should be a break and during this break add-ons will be available.
     */
    public boolean addOnsAvailableDuringBreak(int currentBlindsLevelNr) {
        // This works because currentBlindsLevelNr is zero indexed.
        return !inTheMoney && addOnsEnabled && currentBlindsLevelNr == numberOfLevelsWithRebuys;
    }

    public void addRebuyRequestsForTable(int tableId, Set<Integer> playersWithRebuyOption) {
        if (!playersWithRebuyOption.isEmpty()) {
            rebuyRequestsPerTable.put(tableId, newHashSet(playersWithRebuyOption));
        }
    }

    public void breakFinished() {
        finishAddOnPeriod();
    }

    public BigDecimal getRebuyCost() {
        return rebuyCost;
    }

    public void increaseRebuyCount(int playerId) {
        int numberOfRebuys = numberOfRebuysPerformedBy(playerId);
        numberOfRebuysPerformed.put(playerId, numberOfRebuys + 1);
    }

    public boolean isPlayerAllowedToPerformAddOn(int playerId) {
        return addOnPeriodActive && !performedAddOns.contains(playerId);
    }

    public boolean isPlayerAllowedToRebuy(int playerId, BigDecimal playerBalance) {
        return rebuyAllowed.apply(playerId) && playerBalance.compareTo(maxStackForRebuy) < 0;
    }

    public void removeRebuyRequestsForTable(int tableId) {
        rebuyRequestsPerTable.remove(tableId);
    }

    public BigDecimal getAddOnChipsAmount() {
        return addOnChipsAmount;
    }

    public void notifyInTheMoney() {
        // If we are in the money, close the rebuy period.
        inTheMoney = true;
        rebuysAvailable = false;
    }

    public void notifyNewLevelStarted(int currentBlindsLevelNr, boolean isBreak, TableNotifier tableNotifier) {
        // Adding one since currentBlindsLevelNr is 0 indexed.
        if (currentBlindsLevelNr + 1 > (numberOfLevelsWithRebuys)) {
            rebuysAvailable = false;
            historyPersister.rebuyPeriodFinished();
        }
        if (isBreak && addOnsAvailableDuringBreak(currentBlindsLevelNr)) {
            startAddOnPeriod();
            tableNotifier.notifyAllTables(new AddOnsAvailableDuringBreak(getAddOnChipsAmount(), getAddOnCost()));
        }
    }

    public void addOnPerformed(int playerId) {
        performedAddOns.add(playerId);
    }

    public void startAddOnPeriod() {
        log.debug("Starting add-on period.");
        rebuysAvailable = false;
        addOnPeriodActive = true;
        historyPersister.addOnPeriodStarted();
    }

    public void finishAddOnPeriod() {
        if (addOnPeriodActive) {
            tableNotifier.notifyAllTables(new AddOnPeriodClosed());
            historyPersister.addOnPeriodFinished();
            log.debug("Add-on period finished.");
        }
        addOnPeriodActive = false;
    }

    public boolean tableHasPendingRequests(int tableId) {
        return !getRebuyRequestsForTable(tableId).isEmpty();
    }

    private int numberOfRebuysPerformedBy(Integer playerId) {
        if (numberOfRebuysPerformed.containsKey(playerId)) {
            return numberOfRebuysPerformed.get(playerId);
        } else {
            return 0;
        }
    }

    public BigDecimal getRebuyChipsAmount() {
        return rebuyChipsAmount;
    }

    public Set<Integer> getRebuyRequestsForTable(Integer tableId) {
        if (rebuyRequestsPerTable.containsKey(tableId)) {
            return rebuyRequestsPerTable.get(tableId);
        } else {
            return emptySet();
        }
    }

    public BigDecimal getAddOnCost() {
        return addOnCost;
    }

    public Set<Integer> requestRebuys(int tableId, Set<Integer> playersOut) {
        Set<Integer> playersWithRebuyOption = newHashSet(filter(playersOut, rebuyAllowed));
        if (!playersWithRebuyOption.isEmpty()) {
            log.debug("Requesting rebuys from players: " + playersWithRebuyOption);
            tableNotifier.notifyTable(tableId, new OfferRebuy(playersWithRebuyOption, format(rebuyCost), format(rebuyChipsAmount)));
            addRebuyRequestsForTable(tableId, playersWithRebuyOption);
            tablesWaitingForRebuys.add(tableId);
            historyPersister.rebuysRequested(playersWithRebuyOption);
        }
        return playersWithRebuyOption;
    }

    public boolean tableIsWaitingForRebuys(int tableId) {
        return tablesWaitingForRebuys.contains(tableId);
    }

    public void removeTableWaitingForRebuys(int tableId) {
        tablesWaitingForRebuys.remove(tableId);
    }

    public void injectTransientDependencies(TableNotifier tableNotifier, HistoryPersister historyPersister) {
        this.tableNotifier = tableNotifier;
        this.historyPersister = historyPersister;
    }

    public void tournamentFinished() {
        tablesWaitingForRebuys.clear();
        rebuyRequestsPerTable.clear();
        rebuysAvailable = false;
        addOnPeriodActive = false;
    }
}
