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

package com.cubeia.games.poker.tournament.state;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.max;
import static org.joda.time.Seconds.secondsBetween;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cubeia.games.poker.common.money.Currency;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.io.protocol.TournamentPlayerList;
import com.cubeia.games.poker.io.protocol.TournamentStatistics;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import com.cubeia.games.poker.tournament.rebuy.RebuySupport;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingFactory;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;

public class PokerTournamentState implements Serializable {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = Logger.getLogger(PokerTournamentState.class);

    private TimingProfile timing = TimingFactory.getRegistry().getDefaultTimingProfile();

    private int tablesToCreate;

    private PokerTournamentStatus status;

    // Timestamps for profiling
    private long firstRegisteredTime = 0;

    private long lastRegisteredTime = 0;

    /** Maps playerId to balance */
    private Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();

//    private Set<Integer> pendingRegistrations = newHashSet();

    private BlindsStructure blindsStructure;

    private int currentBlindsLevelNr;

    private Level currentBlindsLevel;

    /**  This id is used in the tournament history for identifying this tournament instance uniquely. */
    private String historicId;

    private BigDecimal buyIn;

    private BigDecimal fee;

    private BigDecimal guaranteedPrizePool = BigDecimal.ZERO;

    /**  Maps playerId to PlayerSessionId */
    private Map<Integer, PlayerSessionId> playerSessions = newHashMap();

    private PayoutStructure payoutStructure;

    private Payouts payouts;

    private BigDecimal prizePool = BigDecimal.ZERO;

    // This is a session for the actual tournament. Used for transferring money from the users to the tournament account.
    private TournamentSessionId tournamentSessionId;

    private TournamentLifeCycle tournamentLifeCycle;

    private DateTime nextLevelStartTime = new DateTime(0);

    private DateTime startTime = new DateTime(0);

    private int minutesVisibleAfterFinished;

    /** Maps playerId to amount won. */
    private Map<Integer, BigDecimal> winMap = newHashMap();

    // Maps playerId -> MttPlayer. Transient to reduce serialized size.
    private transient Map<Integer, MttPlayer> playerMap;

    // Sorted player list which is shown in the tournament lobby. Transient to reduce serialized size.
    private transient TournamentPlayerList playerList;

    // Maps playerId -> tableId where he sits. Transient to reduce serialized size.
    private transient Map<Integer, Integer> playerToTableMap;

    private transient TournamentStatistics tournamentStatistics;

    private transient com.cubeia.games.poker.io.protocol.BlindsStructure blindsStructurePacket;

    private int templateId;

    private Set<Long> allowedOperators = new HashSet<Long>();
    
    private Set<HistoricPlayer> resurrectingPlayers = new HashSet<HistoricPlayer>();

    private Set<Integer> tablesNotReadyForBreak = new HashSet<Integer>();

    private PendingBackendRequests pendingRequests = new PendingBackendRequests();

    private boolean sitAndGo;

    private BetStrategyType betStrategy;

    private boolean shouldCancel = false;

    private boolean resurrectingTournament;

    private String startDateString;

    private DateTime registrationStartDate;

    private BigDecimal startingChips;

    private RebuySupport rebuySupport = RebuySupport.NO_REBUYS;

    private boolean inTheMoney = false;

    private Currency currency;

    /**
     * @return True if this tournament is limited to one or more operators
     */
    public boolean isPrivate() {
        return allowedOperators.size() > 0;
    }
    
    /**
     * @param operator Player operator to check
     * @return True if the operator is allowed in the tournament
     */
    public boolean isOperatorAllowed(Long operator) {
        return (allowedOperators.size() == 0 || operator == null || operator == -1 ? true : allowedOperators.contains(operator));
    }
    
    public boolean allTablesHaveBeenCreated(int tablesCreated) {
        return tablesCreated >= tablesToCreate;
    }
    
    public Set<Long> getAllowedOperators() {
        return allowedOperators;
    }
    
    public void setAllowedOperators(Set<Long> allowedOperators) {
        this.allowedOperators = allowedOperators;
    }

    public BetStrategyType getBetStrategy() {
        return betStrategy;
    }


    public PendingBackendRequests getPendingRequests() {
        return pendingRequests;
    }

    public BigDecimal getStartingChips() {
        return startingChips;
    }

    public boolean isResurrectingTournament() {
        return resurrectingTournament;
    }

    public boolean isSitAndGo() {
        return sitAndGo;
    }

    public void prepareBreak(Set<Integer> tables) {
        tablesNotReadyForBreak.addAll(tables);
    }

    public void setBetStrategy(BetStrategyType betStrategy) {
        this.betStrategy = betStrategy;
    }



    public boolean inTheMoney() {
        return inTheMoney;
    }

    public void setInTheMoney(boolean inTheMoney) {
        this.inTheMoney = inTheMoney;
    }

    public void setPayout(int playerId, BigDecimal payout) {
        winMap.put(playerId, payout);
    }

    public void setRebuySupport(RebuySupport rebuySupport) {
        if (rebuySupport == null) {
            this.rebuySupport = RebuySupport.NO_REBUYS;
        } else {
            this.rebuySupport = rebuySupport;
        }
    }

    public void setResurrectingTournament(boolean resurrectingTournament) {
        this.resurrectingTournament = resurrectingTournament;
    }

    public void setShouldCancel(boolean shouldCancel) {
        this.shouldCancel = shouldCancel;
    }

    public void setSitAndGo(boolean sitAndGo) {
        this.sitAndGo = sitAndGo;
    }

    public void setStartingChips(BigDecimal startingChips) {
        this.startingChips = startingChips;
    }

    public void setTablesToCreate(int tablesToCreate) {
        this.tablesToCreate = tablesToCreate;
    }

    public BigDecimal getPlayerBalance(int playerId) {
        if (!balances.containsKey(playerId)) return BigDecimal.ZERO;
        return balances.get(playerId);
    }

    public void setBalance(int playerId, BigDecimal balance) {
        balances.put(playerId, balance);
    }

    public PokerTournamentStatus getStatus() {
        return status;
    }

    public void setStatus(PokerTournamentStatus status) {
        this.status = status;
    }

    public TimingProfile getTiming() {
        return timing;
    }

    public void setTiming(TimingProfile timing) {
        this.timing = timing;
    }

    public long getFirstRegisteredTime() {
        return firstRegisteredTime;
    }

    public void setFirstRegisteredTime(long firstRegisteredTime) {
        this.firstRegisteredTime = firstRegisteredTime;
    }

    public long getLastRegisteredTime() {
        return lastRegisteredTime;
    }

    public void setLastRegisteredTime(long lastRegisteredTime) {
        this.lastRegisteredTime = lastRegisteredTime;
    }

    public BigDecimal getSmallBlindAmount() {
        return getCurrentBlindsLevel().getSmallBlindAmount();
    }

    public void setBlindsStructure(BlindsStructure blindsStructure) {
        this.blindsStructure = blindsStructure;
        currentBlindsLevelNr = 0;
        currentBlindsLevel = blindsStructure.getFirstBlindsLevel();
    }

    public Level getCurrentBlindsLevel() {
        return currentBlindsLevel;
    }

    public Level increaseBlindsLevel() {
        log.debug("Increasing blinds level.");
        currentBlindsLevel = blindsStructure.getBlindsLevel(++currentBlindsLevelNr);
        log.debug("Blinds level is now: " + currentBlindsLevelNr + ": " + currentBlindsLevel);
        return currentBlindsLevel;
    }

    public void setHistoricId(String id) {
        this.historicId = id;
    }

    public String getHistoricId() {
        return historicId;
    }

    public Money getBuyInAsMoney() {
        return convertToMoney(buyIn);
    }

    public Money getFeeAsMoney() {
        return convertToMoney(fee);
    }

    public Money getBuyInPlusFeeAsMoney() {
        return convertToMoney(buyIn.add(fee));
    }

    public Money convertToMoney(BigDecimal moneyInDecimalForm) {
        return new Money(moneyInDecimalForm, this.currency);
    }

    public void setBuyIn(BigDecimal buyIn) {
        this.buyIn = buyIn;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public void addPendingRegistration(int playerId) {
        pendingRequests.addPendingRegistration(playerId);
    }

    public void removePendingRequest(int playerId) {
        pendingRequests.removePendingRegistration(playerId);
    }

    public boolean hasPendingRegistrations() {
        return !pendingRequests.isEmpty();
    }

    public void addPlayerSession(PlayerSessionId sessionId) {
        playerSessions.put(sessionId.playerId, sessionId);
    }

    public boolean isOnBreak() {
        return currentBlindsLevel.isBreak();
    }

    public void addTableReadyForBreak(int tableId) {
        tablesNotReadyForBreak.remove(tableId);
    }

    public void addTablesReadyForBreak(Set<Integer> tables) {
        tablesNotReadyForBreak.removeAll(tables);
    }

    /**
     * Checks if all tables are ready to start the break. A table is ready for break if it has finished
     * a hand after the break was supposed to start, or if it has only one player.
     *
     * @return true if all tables are ready to start the break, false otherwise
     */
    public boolean allTablesReadyForBreak() {
        return tablesNotReadyForBreak.isEmpty();
    }

    public void breakFinished() {
        tablesNotReadyForBreak.clear();
    }

    public void setPayoutStructure(PayoutStructure payoutStructure, int minPlayers) {
        this.payoutStructure = payoutStructure;
        // Init the payouts assuming that there will be at least minPlayers players.
        setPayouts(minPlayers, 0);
    }

    /**
     * Sets the payouts to use given the number of players that participate in the tournament.
     *
     */
    public void setPayouts(int minPlayers, int registeredPlayersCount) {
        log.debug("Calculating payouts, minPlayers: " + minPlayers + " registered players: " + registeredPlayersCount);
        BigDecimal minRegPrizePool = buyIn.multiply(BigDecimal.valueOf(minPlayers));
        // Total prize pool is the maximum of the current prize pool and the prize pool we will have if at least minPlayers players register.
        BigDecimal totalPrizePool = minRegPrizePool.max(getPrizePool());
        log.debug("Total prize pool: " + totalPrizePool);
        this.payouts = payoutStructure.getPayoutsForEntrantsAndPrizePool(max(minPlayers, registeredPlayersCount), totalPrizePool, currency);
    }

    public void addBuyInToPrizePool() {
        addMoneyToPrizePool(buyIn);
    }

    public void addMoneyToPrizePool(BigDecimal money) {
        log.debug("Adding " + money + " to prize pool.");
        prizePool = prizePool.add(money);
        log.debug("Prize pool is now: " + prizePool);
    }

    public void removeBuyInFromPrizePool() {
        log.debug("Removing " + buyIn + " from prize pool.");
        prizePool = prizePool.subtract(buyIn);
        log.debug("Prize pool is now: " + prizePool);
    }

    public Payouts getPayouts() {
        return payouts;
    }

    public void setTournamentSessionId(PlayerSessionId sessionId) {
        this.tournamentSessionId = new TournamentSessionId(sessionId);
    }

    public PlayerSessionId getPlayerSession(Integer playerId) {
        return playerSessions.get(playerId);
    }

    public TournamentSessionId getTournamentSession() {
        return tournamentSessionId;
    }

    public Money createZeroMoney() {
        return new Money(BigDecimal.ZERO, this.currency);
    }

    public void removePlayerSession(int playerId) {
        playerSessions.remove(playerId);
    }

    public void setLifecycle(TournamentLifeCycle tournamentLifeCycle) {
        this.tournamentLifeCycle = tournamentLifeCycle;
    }

    public boolean shouldScheduleTournamentStart(DateTime now) {
        return tournamentLifeCycle.shouldScheduleTournamentStart(getStatus(), now);
    }

    public long getTimeUntilTournamentStart(DateTime now) {
        return tournamentLifeCycle.getTimeToTournamentStart(now);
    }

    public DateTime getStartTime() {
        if (status == PokerTournamentStatus.RUNNING) {
            return startTime;
        } else {
            return tournamentLifeCycle.getStartTime();
        }
    }

    public long getTimeUntilRegistrationStart(DateTime now) {
        return tournamentLifeCycle.getTimeToRegistrationStart(now);
    }

    public boolean shouldTournamentStart(DateTime now, int registeredPlayers, int minPlayers) {
        boolean lifeCycleSaysStart = tournamentLifeCycle.shouldStartTournament(now, registeredPlayers, minPlayers);
        return lifeCycleSaysStart && !hasPendingRegistrations();
    }

    public boolean shouldCancelTournament(DateTime now, int registeredPlayersCount, int minPlayers) {
        return shouldCancel || tournamentLifeCycle.shouldCancelTournament(now, registeredPlayersCount, minPlayers);
    }

    public boolean shouldOpenRegistration(DateTime now) {
        return tournamentLifeCycle.shouldOpenRegistration(now);
    }

    public boolean shouldScheduleRegistrationOpening(DateTime now) {
        return tournamentLifeCycle.shouldScheduleRegistrationOpening(getStatus(), now);
    }

    public BigDecimal getWinningsFor(MttPlayer player) {
        if (winMap.containsKey(player.getPlayerId())) {
            return winMap.get(player.getPlayerId());
        }
        return BigDecimal.ZERO;
    }

    public BlindsStructure getBlindsStructure() {
        return blindsStructure;
    }

    public MttPlayer getTournamentPlayer(int playerId, MTTStateSupport support) {
        if (playerMap == null) {
            createPlayerMap(support);
        }
        return playerMap.get(playerId);
    }

    public void invalidatePlayerMap() {
        playerMap = null;
        playerList = null;
    }

    private void createPlayerMap(MTTStateSupport support) {
        playerMap = newHashMap();
        for (MttPlayer player : support.getPlayerRegistry().getPlayers()) {
            playerMap.put(player.getPlayerId(), player);
        }
    }

    public BigDecimal getPrizePool() {
        return prizePool.max(guaranteedPrizePool);
    }

    public void setPlayerList(TournamentPlayerList playerList) {
        this.playerList = playerList;
    }

    public TournamentPlayerList getPlayerList() {
        return playerList;
    }

    public void invalidatePlayerList() {
        playerList = null;
    }

    public int getTableFor(int playerId, MTTStateSupport state) {
        if (playerToTableMap == null) {
            createPlayerToTableMap(state);
        }
        Integer tableId = playerToTableMap.get(playerId);
        return tableId == null ? -1 : tableId;
    }

    private void createPlayerToTableMap(MTTStateSupport state) {
        playerToTableMap = newHashMap();
        for (Integer tableId : state.getTables()) {
            for (Integer playerId : state.getPlayersAtTable(tableId)) {
                playerToTableMap.put(playerId, tableId);
            }
        }
    }

    public void invalidatePlayerToTableMap() {
        playerToTableMap = null;
    }

    public TournamentStatistics getTournamentStatistics() {
        return tournamentStatistics;
    }

    public void invalidateTournamentStatistics() {
        tournamentStatistics = null;
    }

    public void setTournamentStatistics(TournamentStatistics tournamentStatistics) {
        this.tournamentStatistics = tournamentStatistics;
    }

    public com.cubeia.games.poker.io.protocol.BlindsStructure getBlindsStructurePacket() {
        return blindsStructurePacket;
    }

    public void setBlindsStructurePacket(com.cubeia.games.poker.io.protocol.BlindsStructure blindsStructurePacket) {
        this.blindsStructurePacket = blindsStructurePacket;
    }

    public int getCurrentBlindsLevelNr() {
        return currentBlindsLevelNr;
    }

    public int getTimeToNextLevel(DateTime now) {
        return max(0, secondsBetween(now, nextLevelStartTime).getSeconds());
    }

    public void setNextLevelStartTime(DateTime time) {
        nextLevelStartTime = time;
    }

    public void setStartTime(DateTime now) {
        this.startTime = now;
    }

    public int getMinutesVisibleAfterFinished() {
        return minutesVisibleAfterFinished;
    }

    public void setMinutesVisibleAfterFinished(int minutesVisibleAfterFinished) {
        this.minutesVisibleAfterFinished = minutesVisibleAfterFinished;
    }

    public DateTime getNextLevelStartTime() {
        return nextLevelStartTime;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public void setResurrectingPlayers(Set<HistoricPlayer> resurrectingPlayers) {
        this.resurrectingPlayers = resurrectingPlayers;
    }

    public Set<HistoricPlayer> getResurrectingPlayers() {
        return resurrectingPlayers;
    }

    public void setStartDateString(String dateString) {
        this.startDateString = dateString;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setRegistrationStartDate(DateTime date) {
        this.registrationStartDate = date;
    }

    public DateTime getRegistrationStartDate() {
        return registrationStartDate;
    }

    public RebuySupport getRebuySupport() {
        return rebuySupport;
    }

    public void setGuaranteedPrizePool(BigDecimal guaranteedPrizePool) {
        this.guaranteedPrizePool = guaranteedPrizePool;
    }

    public Money getGuaranteedPrizePoolUsedAsMoney() {
        // The guaranteed prize pool used is GUARANTEED - PRIZE POOL, or zero if PRIZE POOL > GUARANTEED.
        return convertToMoney(BigDecimal.ZERO.max(guaranteedPrizePool.subtract(prizePool)));
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }
}
