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
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.variant.GameType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Predicates.alwaysTrue;

public abstract class AbstractPokerGameSTM implements PokerGameSTM {

    private static final long serialVersionUID = 1L;

    @VisibleForTesting
    StateChanger stateChanger;

    protected GameType gameType;

    protected PokerContext context;

    protected ServerAdapterHolder serverAdapterHolder;

    private static final Logger log = LoggerFactory.getLogger(AbstractPokerGameSTM.class);

    public AbstractPokerGameSTM(GameType gameType, PokerContext context, ServerAdapterHolder serverAdapter, StateChanger stateChanger) {
        this.gameType = gameType;
        this.stateChanger = stateChanger;
        this.context = context;
        this.serverAdapterHolder = serverAdapter;
    }

    protected AbstractPokerGameSTM() {

    }

    @Override
    public void enterState() {
    }

    @Override
    public void exitState() {
    }

    @Override
    public void timeout() {
        log.warn("Ignoring timeout in state " + this + " context: " + context);
    }

    @Override
    public boolean act(PokerAction action) {
        log.warn("Ignoring action. PokerState: " + context + " Action: " + action);
        return false;
    }

    @Override
    public String getStateDescription() {
        return getClass().getName();
    }

    @Override
    public void playerJoined(PokerPlayer player) {
    }

    @Override
    public boolean isPlayerInHand(int playerId) {
        return false;
    }

    /**
     * NOTE: A player can NOT sit out during a hand, it defies the concept of sitting out.
     * Sitting out means that you are not dealt any cards for that hand. If a player already has
     * cards, he is in the hand and cannot suddenly sit out.
     *
     * In tournaments when selecting sit out next hand the player will be marked
     * as away after the hand finishes or when he folds
     *
     * next hand
     *
     * @param playerId the id of the player who wants to sit out next hand
     *
     */
    @Override
    public void setPlayerSitOutNextHand(int playerId) {
        PokerPlayer player = context.getPlayer(playerId);
        log.info("Player with id " + playerId + " wants to sit out next hand.");
        if (player.hasFolded()) {
            markPlayerAsSittingOutOrAway(player);
        } else {
            player.setSittingOutNextHand(true);
        }
    }

    protected void markPlayerAsSittingOutOrAway(PokerPlayer player) {
        PokerPlayerStatus status = PokerPlayerStatus.SITIN;
        if (!context.isTournamentTable()) {
            player.setSitOutStatus(SitOutStatus.SITTING_OUT);
            status = PokerPlayerStatus.SITOUT;
        } else {
            player.setAway(true);
        }
        getServerAdapter().notifyPlayerStatusChanged(player.getId(), status,
                false, player.isAway(),player.isSittingOutNextHand());

    }

    @Override
    public void playerSitsIn(int playerId) {
        log.debug("player {} is sitting in", playerId);

        PokerPlayer player = context.getPokerPlayer(playerId);
        if (player == null) {
            log.warn("player {} not at table but tried to sit in. Ignoring.", playerId);
            return;
        }

        if (!player.isSittingOut()) {
            //in tournaments you're not allowed to sit out but be away
            player.setSittingOutNextHand(false);
            player.setAway(false);
            return;
        }

        if (gameType.canPlayerAffordEntryBet(player, context.getSettings(), true)) {
            log.debug("Player {} can afford ante. Sit in", player);

            player.sitIn();
            player.setSittingOutNextHand(false);
            player.setAway(false);
            player.setSitInAfterSuccessfulBuyIn(false);
            notifyPlayerSittingIn(playerId);

            // This might start the game.
            playerJoined(player);
        } else {
            log.debug("player {} is out of cash, must bring more before joining", player);

            if (!player.isBuyInRequestActive() && player.getRequestedBuyInAmount().compareTo(BigDecimal.ZERO) == 0) {
                log.debug("player {} does not have buy in request active so notify buy in info", player);
                notifyBuyinInfo(playerId, true);
            }
        }
    }

    @Override
    public void performPendingBuyIns(Set<PokerPlayer> singleton) {
        log.debug("Not performing pending buy-ins as the current state does not think that's appropriate: " + this);
    }

    @Override
    public void playerOpenedSession(int playerId) {
    	log.info("playerOpenedSession ["+playerId+"], gameType["+gameType+"] context["+context+"]");
        boolean enoughMoney = gameType.canPlayerAffordEntryBet(context.getPlayer(playerId), context.getSettings(), false);
        log.debug("Player {} opened session. Sending buy-in request if he doesn't have enough money for an entry bet: {}", playerId, enoughMoney);
        if (!enoughMoney) {
            getServerAdapter().notifyBuyInInfo(playerId, false);
        }
    }

    private void notifyPlayerSittingIn(int playerId) {
        log.debug("notifyPlayerSittingIn() id: " + playerId + " status:" + PokerPlayerStatus.SITIN.name());
        boolean isInCurrentHand = context.isPlayerInHand(playerId);
        PokerPlayer player = context.getPlayer(playerId);
        getServerAdapter().notifyPlayerStatusChanged(playerId, PokerPlayerStatus.SITIN, isInCurrentHand,
                player.isAway(), player.isSittingOutNextHand());
    }

    private void notifyBuyinInfo(int playerId, boolean mandatoryBuyin) {
        getServerAdapter().notifyBuyInInfo(playerId, mandatoryBuyin);
    }

    protected void doPerformPendingBuyIns(Set<PokerPlayer> players) {
        getServerAdapter().performPendingBuyIns(players);
    }

    protected void changeState(AbstractPokerGameSTM newState) {
        newState.context = context;
        newState.gameType = gameType;
        newState.serverAdapterHolder = serverAdapterHolder;
        newState.stateChanger = stateChanger;
        stateChanger.changeState(newState);
    }

    protected ServerAdapter getServerAdapter() {
        return serverAdapterHolder.get();
    }

    protected void startHand() {
        context.setHandFinished(false);
        Collection<PokerPlayer> playersReadyToStartHand = getPlayersReadyToStartHand();
        if (playersReadyToStartHand.size() > 1) {
            context.prepareHand(getReadyPlayerFilter());

            notifyNewHand();
            notifyAllPlayerBalances();
            notifyAllHandStartPlayerStatus();

            gameType.prepareNewHand();
            gameType.startHand();

            changeState(new PlayingSTM());
        } else {
            log.warn("Not enough players to start hand: " + playersReadyToStartHand.size());
            changeState(new NotStartedSTM());
        }
    }

    protected Collection<PokerPlayer> getPlayersReadyToStartHand() {
        return context.getPlayersReadyToStartHand(getReadyPlayerFilter());
    }

    /**
     * Notify everyone about hand start status.
     */
    public void notifyAllHandStartPlayerStatus() {
        for (PokerPlayer player : context.getSeatedPlayers()) {
            if (player.isSittingOut()) {
                getServerAdapter().notifyHandStartPlayerStatus(player.getId(), PokerPlayerStatus.SITOUT,
                        player.isAway(), player.isSittingOutNextHand());
            } else {
                getServerAdapter().notifyHandStartPlayerStatus(player.getId(), PokerPlayerStatus.SITIN, player.isAway(),
                        player.isSittingOutNextHand());
            }
        }
    }

    public void notifyNewHand() {
        getServerAdapter().notifyNewHand();
    }

    public void notifyAllPlayerBalances() {
        for (PokerPlayer player : context.getSeatedPlayers()) {
            notifyPlayerBalance(player);
        }
    }

    public void notifyPlayerBalance(PokerPlayer player) {
        getServerAdapter().notifyPlayerBalance(player);
    }

    private Predicate<PokerPlayer> getReadyPlayerFilter() {
        if (context.isTournamentTable()) {
            return alwaysTrue();
        } else {
            return cashGamesReadyPlayerFilter();
        }
    }

    private Predicate<PokerPlayer> cashGamesReadyPlayerFilter() {
        return new Predicate<PokerPlayer>() {
            @Override
            public boolean apply(PokerPlayer pokerPlayer) {
                boolean canAffordEntryBet = gameType.canPlayerAffordEntryBet(pokerPlayer, context.getSettings(), false);
                boolean isSittingIn = !pokerPlayer.isSittingOut();
                boolean buyInActive = pokerPlayer.isBuyInRequestActive();
                boolean readyToPlay = canAffordEntryBet && isSittingIn && !buyInActive;
                return readyToPlay;
            }
        };
    }

    public boolean playerReadyToStartHand(PokerPlayer pokerPlayer) {
        return getReadyPlayerFilter().apply(pokerPlayer);
    }
}
