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

package com.cubeia.games.poker.tournament.lobby;

import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.io.protocol.*;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.OUT;
import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;

/**
 * This class is responsible for serving tournament lobby data to clients, such as:
 * <p/>
 * - A list of players in the tournament (with chips, position and other data)
 * - The payout structure
 * - The blinds structure
 */
public class TournamentLobby {

    private static final Logger log = Logger.getLogger(TournamentLobby.class);

    @Assisted
    private MTTStateSupport state;

    @Assisted
    private PokerTournamentState pokerState;

    @Inject
    private SystemTime dateFetcher;

    @Inject
    private PacketSender sender;

    @Service
    private transient CashGamesBackendService backend;

    @SuppressWarnings("UnusedDeclaration")
    public TournamentLobby() {
    }

    public TournamentLobby(PacketSender sender, SystemTime dateFetcher, CashGamesBackendService backend, MTTStateSupport state, PokerTournamentState pokerState) {
        this.sender = sender;
        this.dateFetcher = dateFetcher;
        this.state = state;
        this.pokerState = pokerState;
        this.backend = backend;
    }

    public void sendPlayerListTo(int playerId) {
        sendPacketToPlayer(pokerState.getPlayerList(), playerId);
    }

    public void sendBlindsStructureTo(int playerId) {
        BlindsStructure packet = getBlindsStructurePacket();
        sendPacketToPlayer(packet, playerId);
    }

    public void sendPayoutInfoTo(int playerId) {
        PayoutInfo payoutInfo = createPayoutInfoPacket();
        sendPacketToPlayer(payoutInfo, playerId);
    }

    PayoutInfo createPayoutInfoPacket() {
        PayoutInfo payoutInfo = new PayoutInfo();
        List<Payout> payouts = newArrayList();
        Payouts payoutStructure = pokerState.getPayouts();
        for (int i = 1; i <= payoutStructure.getNumberOfPlacesInTheMoney(); i++) {
            payouts.add(new Payout(i, format(payoutStructure.getPayoutsForPosition(i))));
        }
        payoutInfo.payouts = payouts;
        payoutInfo.prizePool = pokerState.getPrizePool().toString();
        return payoutInfo;
    }

    BlindsStructure getBlindsStructurePacket() {
        if (pokerState.getBlindsStructurePacket() == null) {
            pokerState.setBlindsStructurePacket(createBlindsStructurePacket());
        }
        return pokerState.getBlindsStructurePacket();
    }

    BlindsStructure createBlindsStructurePacket() {
        BlindsStructure packet = new BlindsStructure();
        List<BlindsLevel> list = newArrayList();
        for (Level level : pokerState.getBlindsStructure().getBlindsLevels()) {
            list.add(new BlindsLevel(
                    "" + level.getSmallBlindAmount(),
                    "" + level.getBigBlindAmount(),
                    "" + level.getAnteAmount(),
                    level.isBreak(),
                    level.getDurationInMinutes()));
        }
        packet.blindsLevels = list;
        return packet;
    }

    private void sendPacketToPlayer(ProtocolObject packet, int playerId) {
        sender.sendPacketToPlayer(packet, playerId);
    }

    @VisibleForTesting
    TournamentPlayerList getPlayerList() {
        if (pokerState.getPlayerList() != null) {
            return pokerState.getPlayerList();
        }
        TournamentPlayerList list = new TournamentPlayerList();
        List<TournamentPlayer> players = newArrayList();
        int runningPosition = 0;
        int sharedPlaces = 1;
        BigDecimal lastChipStack = new BigDecimal(-1);

        List<MttPlayer> sortedPlayers = sortPlayers(state.getPlayerRegistry().getPlayers());
        for (MttPlayer player : sortedPlayers) {
            int playerId = player.getPlayerId();
            BigDecimal stackSize = pokerState.getPlayerBalance(playerId);

            // Deal with shared places. If two or more players have the same stack size, they share a place, the next guy will be on place
            // "shared + number of shared places" => I.e. 4. Adam (1500), 4. Ben (1500), 6. Caesar (1550)
            if (stackSize.compareTo(lastChipStack) != 0) {
                runningPosition += sharedPlaces;
                sharedPlaces = 1;
            } else {
                sharedPlaces++;
            }

            int position = player.getStatus() == OUT ? player.getPosition() : runningPosition;

            players.add(new TournamentPlayer(player.getScreenname(), format(stackSize), position, format(getWinningsFor(playerId)), getTableFor(playerId)));
            lastChipStack = stackSize;
        }

        list.players = players;
        pokerState.setPlayerList(list);
        return list;
    }

    private int getTableFor(int playerId) {
        return pokerState.getTableFor(playerId, state);
    }

    private List<MttPlayer> sortPlayers(Collection<MttPlayer> players) {
        List<MttPlayer> list = newArrayList(players);
        sort(list, reverseOrder(new TournamentPlayerListComparator(pokerState)));
        return list;
    }

    private BigDecimal getWinningsFor(int playerId) {
        return pokerState.getWinningsFor(pokerState.getTournamentPlayer(playerId, state));
    }

    public TournamentStatistics getTournamentStatistics() {
        if (pokerState.getTournamentStatistics() == null) {
            ChipStatistics chipStatistics = getChipStatistics();
            LevelInfo levelInfo = getLevelInfo();
            PlayersLeft playersLeft = getPlayerLeft();
            TournamentStatistics statistics = new TournamentStatistics(chipStatistics, levelInfo, playersLeft);
            pokerState.setTournamentStatistics(statistics);
        }
        return pokerState.getTournamentStatistics();
    }

    ChipStatistics getChipStatistics() {
        BigDecimal smallestStack = new BigDecimal(Integer.MAX_VALUE);
        BigDecimal biggestStack = BigDecimal.ZERO;
        BigDecimal totalChips = BigDecimal.ZERO;
        BigDecimal averageStack = BigDecimal.ZERO;
        for (MttPlayer player : state.getPlayerRegistry().getPlayers()) {
            BigDecimal chipStack = pokerState.getPlayerBalance(player.getPlayerId());
            if (chipStack.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (chipStack.compareTo(smallestStack) <0 ) {
                smallestStack = chipStack;
            }
            if (chipStack.compareTo(biggestStack) > 0) {
                biggestStack = chipStack;
            }
            totalChips = totalChips.add(chipStack);
        }
        if (smallestStack.compareTo(new BigDecimal(Integer.MAX_VALUE)) == 0) {
            smallestStack = BigDecimal.ZERO;
        }
        if (state.getRemainingPlayerCount() > 0) {
            averageStack = totalChips.divide(new BigDecimal(state.getRemainingPlayerCount()),0, RoundingMode.DOWN);
        }
        log.debug("Total chips: " + totalChips + " Players still in: " + state.getRemainingPlayerCount() + " Average: " + averageStack);
        return new ChipStatistics(format(smallestStack), format(biggestStack), format(averageStack));
    }

    private PlayersLeft getPlayerLeft() {
        return new PlayersLeft(state.getRemainingPlayerCount(), state.getPlayerRegistry().size());
    }

    private LevelInfo getLevelInfo() {
        return new LevelInfo(pokerState.getCurrentBlindsLevelNr() + 1, pokerState.getTimeToNextLevel(dateFetcher.date()));
    }

    public void sendTournamentLobbyDataTo(int playerId) {
        TournamentLobbyData lobbyData = new TournamentLobbyData();
        lobbyData.blindsStructure = getBlindsStructurePacket();
        lobbyData.payoutInfo = createPayoutInfoPacket();
        lobbyData.players = getPlayerList();
        lobbyData.tournamentStatistics = getTournamentStatistics();
        lobbyData.tournamentInfo = getTournamentInfo();
        sendPacketToPlayer(lobbyData, playerId);
    }

    private TournamentInfo getTournamentInfo() {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.buyIn = format(pokerState.getBuyInAsMoney().getAmount());
        tournamentInfo.fee = format(pokerState.getFeeAsMoney().getAmount());
        tournamentInfo.gameType = "No Limit Hold'em"; // TODO: Change when we actually support anything other than NL Hold'em..
        tournamentInfo.maxPlayers = state.getCapacity();
        tournamentInfo.minPlayers = state.getMinPlayers();
        tournamentInfo.startTime = "" + pokerState.getStartTime().getMillis();
        tournamentInfo.registrationStartTime = "" + pokerState.getRegistrationStartDate().getMillis();
        tournamentInfo.tournamentName = state.getName();
        tournamentInfo.tournamentStatus = convertTournamentStatus(pokerState.getStatus());
        tournamentInfo.buyInCurrencyCode = pokerState.getCurrency().getCode();
        return tournamentInfo;
    }

    public void sendTournamentTableTo(int playerId) {
        TournamentTable tournamentTable = new TournamentTable(pokerState.getTableFor(playerId, state));
        sendPacketToPlayer(tournamentTable, playerId);
    }

    @VisibleForTesting
    Enums.TournamentStatus convertTournamentStatus(PokerTournamentStatus status) {
        return Enums.TournamentStatus.valueOf(status.name());
    }

    public void sendRegistrationInfoTo(int playerId) {
        // TODO: Handle currencies.
        Money balance = getBalanceFor(playerId);
        Money buyIn = pokerState.getBuyInAsMoney();
        Money fee = pokerState.getFeeAsMoney();
        boolean sufficient = balance.getAmount().compareTo(buyIn.getAmount().add(fee.getAmount())) > 0;
        TournamentRegistrationInfo registrationInfo = new TournamentRegistrationInfo(format(buyIn), format(fee), pokerState.getCurrency().getCode(), format(balance), sufficient);
        sender.sendPacketToPlayer(registrationInfo, playerId);
    }

    private Money getBalanceFor(int playerId) {
        Money balance = pokerState.createZeroMoney();
        try {
            balance = backend.getAccountBalance(playerId, pokerState.getCurrency().getCode());
        } catch (Exception e) {
            log.warn("Failed fetching balance for playerId: " + playerId);
        }
        // TODO: Return some kind of error instead of 0.
        return balance;
    }

}
