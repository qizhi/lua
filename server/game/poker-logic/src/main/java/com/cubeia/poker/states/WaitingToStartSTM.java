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
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.util.ThreadLocalProfiler;
import com.cubeia.poker.variant.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class WaitingToStartSTM extends AbstractPokerGameSTM {

    private static final long serialVersionUID = -4837159720440582936L;

    private static final Logger log = LoggerFactory.getLogger(WaitingToStartSTM.class);

    public WaitingToStartSTM(GameType gameType, PokerContext pokerContext, ServerAdapterHolder serverAdapter, StateChanger stateChanger) {
        super(gameType, pokerContext, serverAdapter, stateChanger);
    }

    protected WaitingToStartSTM() {

    }

    @Override
    public void enterState() {
        log.info("Entered waiting to start state.");
        if (!context.isTournamentTable() && !getServerAdapter().isSystemShutDown()) {
            long timeout = context.getSettings().getTiming().getTime(Periods.START_NEW_HAND);
            log.info("Scheduling timeout in " + timeout + " millis.");
            getServerAdapter().scheduleTimeout(timeout);
        }
    }

    @Override
    public void timeout() {
        if (context.isTournamentTable()) {
            commitPendingTournamentBalances();
        } else {
            getServerAdapter().performPendingBuyIns(context.getSeatedPlayers());
            commitPendingBalances(context.getMaxBuyIn());
            setPlayersWithoutMoneyAsSittingOut();
            getServerAdapter().cleanupPlayers(new SitoutCalculator());
        }

        sitOutPlayersMarkedForSitOutNextHand();

        if (getPlayersReadyToStartHand().size() < 2) {
            context.setHandFinished(true);
            log.info("WILL NOT START NEW HAND, TOO FEW PLAYERS SEATED: " + getPlayersReadyToStartHand().size() + " sitting in of " + context.getSeatedPlayers().size());
            changeState(new NotStartedSTM());
        } else if (systemIsShutDown()) {
            log.info("Won't start new hand since system is down.");
            unseatPlayersAndShutDownTable();
        } else {
            startHand();
        }
    }

    private void commitPendingBalances(BigDecimal maxBuyIn) {
        List<PokerPlayer> pokerPlayers = context.commitPendingBalances(maxBuyIn);
        for (PokerPlayer pokerPlayer : pokerPlayers) {
            serverAdapterHolder.get().notifyPlayerBalance(pokerPlayer);
        }
    }

    private void commitPendingTournamentBalances() {
        commitPendingBalances(new BigDecimal(Long.MAX_VALUE));
    }

    public void sitOutPlayersMarkedForSitOutNextHand() {
        for (PokerPlayer player : context.getSeatedPlayers()) {
            if (player.isSittingOutNextHand()) {
                log.debug("Player " + player.getId() + " wants to sit out.");
                markPlayerAsSittingOutOrAway(player);
            }
        }
    }

    private void unseatPlayersAndShutDownTable() {
        unseatPlayers();
        shutDownTable();
    }

    private void shutDownTable() {
        // TODO.
    }

    private void unseatPlayers() {
        // TODO.
    }

    private boolean systemIsShutDown() {
        return getServerAdapter().isSystemShutDown();
    }

    @Override
    public void performPendingBuyIns(Set<PokerPlayer> players) {
        doPerformPendingBuyIns(players);
    }

    @Override
    public boolean act(PokerAction action) {
        log.info("Discarding out of order action: " + action);
        return false;
    }

    /**
     * If a player has no money left he should be set as sitting out to
     * prevent him from being included in new hands.
     */
    public void setPlayersWithoutMoneyAsSittingOut() {
        ThreadLocalProfiler.add("setPlayersWithoutMoneyAsSittingOut");
        for (PokerPlayer player : context.getPlayerMap().values()) {
            log.info("Checking if player " + player.getId() + " can post entry bet.");
            boolean canPlayerAffordEntryBet = gameType.canPlayerAffordEntryBet(player, context.getSettings(), true);
            if (!canPlayerAffordEntryBet) {
                log.info("Player with id " + player.getId() + " can not post entry bet.");
                markPlayerAsSittingOutOrAway(player);
            }
        }
    }

    public String toString() {
        return "WaitingToStartState";
    }

}
