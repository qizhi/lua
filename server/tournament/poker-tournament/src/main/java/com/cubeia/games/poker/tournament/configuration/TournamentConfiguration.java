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

package com.cubeia.games.poker.tournament.configuration;

import static com.cubeia.poker.betting.BetStrategyType.NO_LIMIT;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructureFactory;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingProfile;

/**
 * This class represents the configuration of a tournament.
 *
 */
@Entity
public class TournamentConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(TournamentConfiguration.class);

    @Id @GeneratedValue
    private int id;

    private String name;

    private int seatsPerTable = 10;

    @ManyToOne
    private TimingProfile timingType;

    private int minPlayers = 0;

    private int maxPlayers = 0;

    @ManyToOne
    private BlindsStructure blindsStructure;

    private BigDecimal buyIn;

    private BigDecimal fee;

    private BetStrategyType betStrategy = NO_LIMIT;

    @ManyToOne(fetch = FetchType.LAZY)
    private PayoutStructure payoutStructure;

    private String currency;

    private BigDecimal startingChips;

    private boolean archived;
    
    @ElementCollection(fetch=EAGER)
    private Set<Long> operatorIds = new HashSet<Long>(); // > 0 for private tournaments

    @ManyToOne(cascade = ALL)
    private RebuyConfiguration rebuyConfiguration = new RebuyConfiguration();

    /**
     * By specifying a guaranteed prize pool, the poker network will add money up to that level,
     * if the prize pool from registration entries does not reach that level.
     */
    private BigDecimal guaranteedPrizePool = BigDecimal.ZERO;

    public TournamentConfiguration() {
    }

    public Set<Long> getOperatorIds() {
        return operatorIds;
    }

    public BigDecimal getGuaranteedPrizePool() {
        return guaranteedPrizePool;
    }

    public void setGuaranteedPrizePool(BigDecimal guaranteedPrizePool) {
        this.guaranteedPrizePool = guaranteedPrizePool;
    }

    public void setOperatorIds(Set<Long> operatorIds) {
        this.operatorIds = operatorIds;
    }

    public RebuyConfiguration getRebuyConfiguration() {
        return rebuyConfiguration;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setRebuyConfiguration(RebuyConfiguration rebuyConfiguration) {
        this.rebuyConfiguration = rebuyConfiguration;
    }

    public String toString() {
        return "id[" + id + "] name[" + name + "] seats[" + seatsPerTable + "] timing[" + timingType + "] min[" + minPlayers + "] max[" + maxPlayers + "] ";
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

    public int getSeatsPerTable() {
        return seatsPerTable;
    }

    public void setSeatsPerTable(int seatsPerTable) {
        this.seatsPerTable = seatsPerTable;
    }

    public TimingProfile getTimingType() {
        return timingType;
    }

    public void setTimingType(TimingProfile timingType) {
        this.timingType = timingType;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayer) {
        this.maxPlayers = maxPlayer;
    }

    public BlindsStructure getBlindsStructure() {
        if (blindsStructure == null) {
            log.warn("No blinds structure defined, using default structure.");
            blindsStructure = BlindsStructureFactory.createDefaultBlindsStructure();
        }
        return blindsStructure;
    }

    public void setBlindsStructure(BlindsStructure blindsStructure) {
        this.blindsStructure = blindsStructure;
    }

    public void setBuyIn(BigDecimal buyIn) {
        this.buyIn = buyIn;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getBuyIn() {
        return buyIn;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public PayoutStructure getPayoutStructure() {
        return payoutStructure;
    }

    public void setPayoutStructure(PayoutStructure payoutStructure) {
        this.payoutStructure = payoutStructure;
    }

    public BetStrategyType getBetStrategy() {
        return betStrategy;
    }

    public void setBetStrategy(BetStrategyType betStrategy) {
        this.betStrategy = betStrategy;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getStartingChips() {
        return startingChips;
    }

    public void setStartingChips(BigDecimal startingChips) {
        this.startingChips = startingChips;
    }
}
