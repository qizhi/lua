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

package com.cubeia.games.poker;

import static com.cubeia.games.poker.common.money.MoneyFormatter.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backend.cashgame.dto.AnnounceTableFailedResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableResponse;
import com.cubeia.backend.cashgame.dto.CloseTableRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.handler.BackendCallHandler;
import com.cubeia.games.poker.handler.PokerHandler;
import com.cubeia.games.poker.handler.Trigger;
import com.cubeia.games.poker.io.protocol.AchievementNotificationPacket;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.jmx.PokerStats;
import com.cubeia.games.poker.logic.TimeoutCache;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.games.poker.tournament.messages.AddOnPeriodClosed;
import com.cubeia.games.poker.tournament.messages.AddOnsAvailableDuringBreak;
import com.cubeia.games.poker.tournament.messages.BlindsWithDeadline;
import com.cubeia.games.poker.tournament.messages.OfferRebuy;
import com.cubeia.games.poker.tournament.messages.PlayerAddedChips;
import com.cubeia.games.poker.tournament.messages.TournamentDestroyed;
import com.cubeia.games.poker.tournament.messages.WaitingForPlayers;
import com.cubeia.games.poker.tournament.messages.WaitingForTablesToFinishBeforeBreak;
import com.cubeia.games.poker.util.ProtocolFactory;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.adapter.SystemShutdownException;
import com.cubeia.poker.domainevents.api.BonusEventWrapper;
import com.cubeia.poker.model.BlindsLevel;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;


/**
 * Handle incoming actions.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class Processor implements GameProcessor, TournamentProcessor {

    /**
     * Serializer for poker packets
     */
    private static StyxSerializer serializer = new StyxSerializer(new ProtocolObjectFactory());

    private static Logger log = LoggerFactory.getLogger(Processor.class);

    @Inject
    @VisibleForTesting
    ActionCache actionCache;

    @Inject
    @VisibleForTesting
    StateInjector stateInjector;

    @Inject
    @VisibleForTesting
    PokerState state;

    @Inject
    @VisibleForTesting
    PokerHandler pokerHandler;

    @Inject
    @VisibleForTesting
    BackendCallHandler backendHandler;

    @Inject
    @VisibleForTesting
    TimeoutCache timeoutCache;

    @Inject
    @VisibleForTesting
    TableCloseHandlerImpl tableCloseHandler;

//    @Service
//    @VisibleForTesting
//    HandDebuggerContract handDebugger;

    @Inject
    @VisibleForTesting
    SystemTime dateFetcher;

    /**
     * Handles a wrapped game packet.
     */
    public void handle(GameDataAction action, Table table) {
        stateInjector.injectAdapter(table);

        try {
            ProtocolObject packet = safeUnpack(action);
            if (packet != null) {
                pokerHandler.setPlayerId(action.getPlayerId());
                packet.accept(pokerHandler);
                PokerStats.getInstance().setState(table.getId(), state.getStateDescription());
            }
        } catch (Throwable t) {
            log.error("Unhandled error on table", t);
            tableCloseHandler.handleUnexpectedExceptionOnTable(action, table, t);
        }
    }

    private ProtocolObject safeUnpack(GameDataAction action) {
        try {
            return serializer.unpack(action.getData());
        } catch (Exception e) {
            log.warn("Failed unpacking action from player " + action.getPlayerId() + ". Is he using an old version of the protocol?", e);
        }
        return null;
    }

    /**
     * Handle a wrapped object.
     *
     * <p/>
     */
    public void handle(GameObjectAction action, Table table) {
        stateInjector.injectAdapter(table);
        try {
            Object attachment = action.getAttachment();
            if (attachment instanceof Trigger) {
                Trigger command = (Trigger) attachment;
                handleCommand(table, command);
            } else if (attachment instanceof OpenSessionResponse) {
                log.debug("got open session response: {}", attachment);
                backendHandler.handleOpenSessionSuccessfulResponse((OpenSessionResponse) attachment);
            } else if (attachment instanceof OpenSessionFailedResponse) {
                log.debug("got open session failed response: {}", attachment);
                backendHandler.handleOpenSessionFailedResponse((OpenSessionFailedResponse) attachment);
            } else if (attachment instanceof ReserveResponse) {
                log.debug("got reserve response: {}", attachment);
                backendHandler.handleReserveSuccessfulResponse((ReserveResponse) attachment);
            } else if (attachment instanceof ReserveFailedResponse) {
                log.debug("got reserve failed response: {}", attachment);
                backendHandler.handleReserveFailedResponse((ReserveFailedResponse) attachment);
            } else if (attachment instanceof AnnounceTableResponse) {
                backendHandler.handleAnnounceTableSuccessfulResponse((AnnounceTableResponse) attachment);
            } else if (attachment instanceof AnnounceTableFailedResponse) {
                log.debug("got announce table failed response: {}", attachment);
                backendHandler.handleAnnounceTableFailedResponse();
            } else if (attachment instanceof CloseTableRequest) {
                log.debug("got close table request: {}", attachment);
                CloseTableRequest closeTableRequest = (CloseTableRequest) attachment;
                tableCloseHandler.closeTable(table, closeTableRequest.isForced());
            } else if (attachment instanceof WaitingForTablesToFinishBeforeBreak) {
                handleWaitingForBreak();
            } else if (attachment instanceof WaitingForPlayers) {
                handleWaitingForPlayers();
            } else if (attachment instanceof BlindsWithDeadline) {
                handleBlindsLevel((BlindsWithDeadline) attachment);
            } else if (attachment instanceof TournamentDestroyed) {
                handleTournamentDestroyed();
            } else if (attachment instanceof PlayerAddedChips) {
                handleAddedChips((PlayerAddedChips) attachment);
            } else if (attachment instanceof OfferRebuy) {
                handleOfferRebuy((OfferRebuy) attachment);
            } else if (attachment instanceof AddOnsAvailableDuringBreak) {
                handleAddOnsAvailable((AddOnsAvailableDuringBreak) attachment);
            } else if (attachment instanceof AddOnPeriodClosed) {
                handleAddOnPeriodClosed();
            } else if ("CLOSE_TABLE_HINT".equals(attachment.toString())) {
                log.debug("got CLOSE_TABLE_HINT");
                tableCloseHandler.closeTable(table, false);
            } else if ("CLOSE_TABLE".equals(attachment.toString())) {
                log.debug("got CLOSE_TABLE");
                tableCloseHandler.closeTable(table, true);
            } else if (attachment instanceof BonusEventWrapper) {
            	handleBonusEvent((BonusEventWrapper)attachment, table);
            } else {
                log.warn("Unhandled object: " + attachment.getClass().getName());
            }
        } catch (SystemShutdownException t) {
            log.debug("System is shutting down, closing table " + table.getId());
            tableCloseHandler.closeTable(table, true);
        } catch (Throwable t) {
            log.error("Failed handling game object action.", t);
            tableCloseHandler.handleUnexpectedExceptionOnTable(action, table, t);
        }
    }
    
    private void handleBonusEvent(BonusEventWrapper wrapper, Table table) {
    	log.debug("On Bonus Event wrapper ("+wrapper.hashCode()+"): "+wrapper);
		int playerId = wrapper.playerId;
		int tableId = table.getId();
		
		AchievementNotificationPacket notification = new AchievementNotificationPacket();
		notification.playerId = playerId;
		notification.message = wrapper.event;
		
		ProtocolFactory factory = new ProtocolFactory();
		GameDataAction action = factory.createGameAction(notification, playerId, tableId);
		
		if (wrapper.broadcast) {
			log.info("Notify all players at table["+tableId+"] with event ["+notification.message+"] for player["+playerId+"]");
			table.getNotifier().notifyAllPlayers(action);
		} else {
			log.info("Notify player["+playerId+"] at table["+tableId+"] with event ["+notification.message+"]");
			table.getNotifier().notifyPlayer(playerId, action);
		}
	}

	private void handleAddOnPeriodClosed() {
        state.notifyAddOnPeriodClosed();
    }

    private void handleAddOnsAvailable(AddOnsAvailableDuringBreak addOns) {
        state.notifyAddOnsAvailable(format(addOns.getAddOnCost()), format(addOns.getChipsForAddOn()));
    }

    private void handleOfferRebuy(OfferRebuy offerRebuy) {
        state.offerRebuys(offerRebuy.getPlayers(), offerRebuy.getRebuyCost(), offerRebuy.getRebuyChips());
    }

    private void handleAddedChips(PlayerAddedChips addedChips) {
        state.handleAddedChips(addedChips.getPlayerId(), addedChips.getChipsToAdd());
        if (addedChips.getReason() == PlayerAddedChips.Reason.REBUY) {
            state.notifyPlayerPerformedRebuy(addedChips.getPlayerId());
        } else if (addedChips.getReason() == PlayerAddedChips.Reason.ADD_ON) {
            state.notifyPlayerPerformedAddOn(addedChips.getPlayerId());
        }
    }

    private void handleTournamentDestroyed() {
        state.notifyTournamentDestroyed();
    }

    private void handleWaitingForBreak() {
        state.notifyWaitingToStartBreak();
    }

    private void handleWaitingForPlayers() {
        state.notifyWaitingForPlayers();
    }

    private void handleBlindsLevel(BlindsWithDeadline level) {
        BlindsLevel blindsLevel = new BlindsLevel(level.getSmallBlindAmount(), level.getBigBlindAmount(), level.getAnteAmount(), level.isBreak(),
                                                  level.getDurationInMinutes(), level.getDeadline());
        state.setBlindsLevels(blindsLevel);
    }

    /**
     * Basic switch and response for command types.
     *
     */
    private void handleCommand(Table table, Trigger command) {
        switch (command.getType()) {
            case TIMEOUT:
                boolean verified = pokerHandler.verifySequence(command);
                if (verified) {
                    state.timeout();
                } else {
                    log.warn("Invalid sequence detected");
                    tableCloseHandler.printActionsToErrorLog(null,
                            "Timeout command OOB: " + command + " on table: " + table,
                            table);
                }
                break;
            case PLAYER_TIMEOUT:
                handlePlayerTimeoutCommand(table, command);
                break;
        }

        PokerStats.getInstance().setState(table.getId(), state.getStateDescription());
    }

    /**
     * Verify sequence number before timeout
     *
     */
    private void handlePlayerTimeoutCommand(Table table, Trigger command) {
        if (pokerHandler.verifySequence(command)) {
            timeoutCache.removeTimeout(table.getId(), command.getPid(), table.getScheduler());
            clearRequestSequence();
            state.timeout();
        }
    }

    public void startRound(Table table) {
        stateInjector.injectAdapter(table);
        if (actionCache != null) {
            actionCache.clear(table.getId());
        }
        log.debug("Start Hand on table: " + table + " (" + table.getPlayerSet().getPlayerCount() + ":" + state.getSeatedPlayers().size() + ")");
        state.scheduleTournamentHandStart();
    }

    public void stopRound(Table table) {
        stateInjector.injectAdapter(table);
    }

    private void clearRequestSequence() {
        FirebaseState fbState = (FirebaseState) state.getAdapterState();
        fbState.setCurrentRequestSequence(-1);
    }

}
