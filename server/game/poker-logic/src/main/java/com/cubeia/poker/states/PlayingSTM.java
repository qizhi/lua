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

package com.cubeia.poker.states;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.tournament.RoundReport;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.variant.HandFinishedListener;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayingSTM extends AbstractPokerGameSTM implements HandFinishedListener {

    private static final long serialVersionUID = 7076228045164551068L;

    private static final Logger log = LoggerFactory.getLogger(PlayingSTM.class);

    public String toString() {
        return "PlayingState";
    }

    @Override
    public void enterState() {
        gameType.addHandFinishedListener(this);
    }

    @Override
    public void exitState() {
        gameType.removeHandFinishedListener(this);
    }

    @Override
    public boolean act(PokerAction action) {
        return gameType.act(action);
    }

    @Override
    public void timeout() {
        gameType.timeout();
    }

    @Override
    public void handFinished(HandResult result, HandEndStatus status) {
        log.debug("Hand finished.");
        context.setHandFinished(true);
        awardWinners(result.getResults());
        getServerAdapter().notifyHandEnd(result, status, context.isTournamentTable());
        for (PokerPlayer player : context.getPlayerMap().values()) {
            getServerAdapter().notifyPlayerBalance(player);
        }
        if (context.isTournamentTable()) {
            // Report round to tournament coordinator and wait for notification
            sendTournamentRoundReport();
        } else {
            getServerAdapter().performPendingBuyIns(context.getPlayerMap().values());

            // clean up players here and make leaving players leave and so on also update the lobby
            getServerAdapter().cleanupPlayers(new SitoutCalculator());
            sendBuyinInfoToPlayersWithoutMoney();
        }

        changeState(new WaitingToStartSTM());
    }

    @Override
    public boolean isPlayerInHand(int playerId) {
        return context.isPlayerInHand(playerId);
    }

    @Override
    public void performPendingBuyIns(Set<PokerPlayer> players) {
        Set<PokerPlayer> nonPlayingPlayers = new HashSet<PokerPlayer>();
        for (PokerPlayer player : players) {
            if (!context.isPlayerInHand(player.getId())) {
                nonPlayingPlayers.add(player);
            }
        }
        if (!nonPlayingPlayers.isEmpty()) {
            doPerformPendingBuyIns(nonPlayingPlayers);
        }
    }

    /**
     * Send buy-in question to all players in the current hand that do not have enough money to pay ante.
     */
    @VisibleForTesting
    protected void sendBuyinInfoToPlayersWithoutMoney() {
        for (PokerPlayer player : context.getPlayerMap().values()) {

            boolean canPlayerAffordEntryBet = gameType.canPlayerAffordEntryBet(player, context.getSettings(), true);
            if (!canPlayerAffordEntryBet) {
                if (!player.isBuyInRequestActive()) {
                    getServerAdapter().notifyBuyInInfo(player.getId(), true);
                }
            }
        }
    }

    void sendTournamentRoundReport() {
        RoundReport report = new RoundReport(context.getSmallBlindAmount(), context.getBigBlindAmount(), context.getAnteAmount());
        for (PokerPlayer player : context.getPlayerMap().values()) {
            report.setSetBalance(player.getId(), player.getBalance());
        }
        log.debug("Sending tournament round report: " + report);
        getServerAdapter().reportTournamentRound(report);
    }

    private void awardWinners(Map<PokerPlayer, Result> results) {
        for (Map.Entry<PokerPlayer, Result> entry : results.entrySet()) {
            PokerPlayer player = entry.getKey();
            player.addChips(entry.getValue().getWinningsIncludingOwnBets());
            player.saveStartingBalance();
        }
    }


}
