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

package com.cubeia.poker.variant;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.rounds.RoundHelper;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.util.SitoutCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractGameType implements GameType {

    private static final long serialVersionUID = -6519559952200204899L;

    protected ServerAdapterHolder serverAdapterHolder;

    protected PokerContext context;

    protected RoundHelper roundHelper;

    private Collection<HandFinishedListener> handFinishedListeners = new HashSet<HandFinishedListener>();

    private static final Logger log = LoggerFactory.getLogger(AbstractGameType.class);

    public void requestMultipleActions(Collection<ActionRequest> requests) {
        for (ActionRequest request : requests) {
            request.setTimeToAct(context.getTimingProfile().getTime(Periods.ACTION_TIMEOUT));
            request.setTotalPotSize(context.getTotalPotSize());
        }
        getServerAdapter().requestMultipleActions(requests);
    }

    public void notifyPotAndRakeUpdates(Collection<PotTransition> potTransitions) {
        getServerAdapter().notifyPotUpdates(context.getPotHolder().getPots(), potTransitions);

        // notify all the new balances
        for (PokerPlayer player : context.getPlayersInHand()) {
            getServerAdapter().notifyPlayerBalance(player);
        }
        notifyRakeInfo();
    }

    public void notifyRakeInfo() {
        getServerAdapter().notifyRakeInfo(context.getPotHolder().calculateRakeIncludingBetStacks(context.getPlayersInHand()));
    }

    /**
     * Removes all disconnected players from the table
     */
    public void cleanupPlayers() {
        // Clean up players in states not accessible to the poker logic
        getServerAdapter().cleanupPlayers(new SitoutCalculator());
    }

    public void notifyPlayerBalance(int playerId) {
        getServerAdapter().notifyPlayerBalance(context.getPokerPlayer(playerId));
    }

    /**
     * Notify everyone about hand start status.
     */
    public void notifyAllHandStartPlayerStatus() {
        for (PokerPlayer player : context.getSeatedPlayers()) {
            if (player.isSittingOut()) {
                getServerAdapter().notifyHandStartPlayerStatus(player.getId(), PokerPlayerStatus.SITOUT,player.isAway(),
                        player.isSittingOutNextHand());
            } else {
                getServerAdapter().notifyHandStartPlayerStatus(player.getId(), PokerPlayerStatus.SITIN, player.isAway(),
                        player.isSittingOutNextHand());
            }
        }
    }

    public ServerAdapter getServerAdapter() {
        return serverAdapterHolder.get();
    }

    @Override
    public void setPokerContextAndServerAdapter(PokerContext context, ServerAdapterHolder serverAdapterHolder) {
        this.context = context;
        this.serverAdapterHolder = serverAdapterHolder;
        this.roundHelper = new RoundHelper(context, serverAdapterHolder);
    }

    @Override
    public void addHandFinishedListener(HandFinishedListener handFinishedListener) {
        handFinishedListeners.add(handFinishedListener);
    }

    @Override
    public void removeHandFinishedListener(HandFinishedListener handFinishedListener) {
        handFinishedListeners.remove(handFinishedListener);
    }

    protected void notifyHandFinished(HandResult handResult, HandEndStatus status) {
        log.debug("Hand over. Result: " + handResult.getPlayerHands());
        for (HandFinishedListener listener : handFinishedListeners) {
            listener.handFinished(handResult, status);
        }
    }
}