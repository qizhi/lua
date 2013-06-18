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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.firebase.api.action.AbstractGameAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.adapter.FirebaseServerAdapter;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.handler.BackendPlayerSessionHandler;
import com.cubeia.games.poker.handler.TableCloseHandler;
import com.cubeia.games.poker.io.protocol.Enums;
import com.cubeia.games.poker.io.protocol.ErrorPacket;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.adapter.SystemShutdownException;
import com.cubeia.poker.player.PokerPlayer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TableCloseHandlerImpl implements TableCloseHandler {

    private static Logger log = LoggerFactory.getLogger(TableCloseHandlerImpl.class);
    private static StyxSerializer serializer = new StyxSerializer(new ProtocolObjectFactory());

    private final PokerState state;
    private final ActionCache actionCache;
    private final BackendPlayerSessionHandler backendPlayerSessionHandler;
    private final FirebaseServerAdapter serverAdapter;

    @Inject
    public TableCloseHandlerImpl(PokerState state, ActionCache actionCache, BackendPlayerSessionHandler backendPlayerSessionHandler,
                                 FirebaseServerAdapter serverAdapter) {
        this.state = state;
        this.actionCache = actionCache;
        this.backendPlayerSessionHandler = backendPlayerSessionHandler;
        this.serverAdapter = serverAdapter;
    }

    /* (non-Javadoc)
      * @see com.cubeia.games.poker.TableCloseHandlerI#closeTable(com.cubeia.firebase.api.game.table.Table, boolean)
      */
    @Override
    public void closeTable(Table table, boolean force) {
        log.debug("Close table command received; table id = {}, force = {}", table.getId(), force);
        if (countSeated(table) == 0 || force) {
            log.info("Closing table {} with {} seated players", table.getId(), countSeated(table));
            doCloseTable(table, false, getHandId());
        } else {
            log.debug("Close table aborted, have " + countSeated(table) + " seated players, and should not force the close");
        }
    }

    /* (non-Javadoc)
      * @see com.cubeia.games.poker.TableCloseHandlerI#tableCrashed(com.cubeia.firebase.api.game.table.Table)
      */
    @Override
    public void tableCrashed(Table table) {
        log.info("Closing table {} with {} seated players", table.getId(), countSeated(table));
        doCloseTable(table, false, null);
    }

    private int countSeated(Table table) {
        return table.getPlayerSet().getPlayerCount();
    }

    /* (non-Javadoc)
      * @see com.cubeia.games.poker.TableCloseHandlerI#handleUnexpectedExceptionOnTable(com.cubeia.firebase.api.action.AbstractGameAction, com.cubeia.firebase.api.game.table.Table, java.lang.Throwable)
      */
    @Override
    public void handleUnexpectedExceptionOnTable(AbstractGameAction action, Table table, Throwable throwable) {
        if (throwable instanceof SystemShutdownException) {
            closeTable(table, true);
        } else {
            String handId = getHandId();
            log.info("Handling crashed table id = {}, hand id = {}", table.getId(), handId);
            printToErrorLog(action, table, throwable);
            doCloseTable(table, true, handId);
        }
    }

    private void doCloseTable(Table table, boolean isError, String handId) {
        log.info("Closing table {} ", table.getId());
        Collection<GenericPlayer> players = Lists.newArrayList(table.getPlayerSet().getPlayers());

        // 1. stop table from accepting actions
        //    Gotcha: client actions should not be accepted but callbacks from backend should.
        state.shutdown();

        // 2. set table to invisible in lobby
        makeTableInvisibleInLobby(table);

        // 3. send message to clients, must include hand id on errors
        if (isError) {
            sendErrorMessageToClient(table, handId);
        } else {
            sendCloseMessageToClient(table, handId);
        }

        // 4. remove players from table
        Collection<PokerPlayer> removedPokerPlayers = removePlayersFromTable(table, players);

        // 5. close player sessions
        closePlayerSessions(table, removedPokerPlayers);

        // 6. mark table as closed and let the activator take care of destroying it
        markTableReadyForClose(table);
    }

    private void markTableReadyForClose(Table table) {
        table.getAttributeAccessor().setAttribute(PokerLobbyAttributes.TABLE_READY_FOR_CLOSE.name(), new AttributeValue(1));
    }

    private void sendCloseMessageToClient(Table table, String handId) {
        sendMessageToClient(table, Enums.ErrorCode.TABLE_CLOSING, handId);
    }

    private void sendErrorMessageToClient(Table table, String handId) {
        sendMessageToClient(table, Enums.ErrorCode.TABLE_CLOSING_FORCED, handId);
    }

    protected void sendMessageToClient(Table table, Enums.ErrorCode errorCode, String handId) {

        // find all watchers and all players and add them to one list so we can send to all the connected players
        UnmodifiableSet<GenericPlayer> players = table.getPlayerSet().getPlayers();
        UnmodifiableSet<Integer> watcherIds = table.getWatcherSet().getWatchers();

        // extract player ids
        ArrayList<Integer> playerIds = new ArrayList<Integer>();
        for (GenericPlayer player : players) {
            playerIds.add(player.getPlayerId());
        }

        // concat players and watchers
        Iterable<Integer> allPlayerIds = Iterables.concat(playerIds, watcherIds);

        for (Integer playerId : allPlayerIds) {
            ErrorPacket errorPacket = new ErrorPacket(errorCode, handId);
            log.debug("Sending {} message to player: {}", errorCode, playerId);
            GameDataAction errorAction = new GameDataAction(playerId, table.getId());
            ByteBuffer packetBuffer;
            packetBuffer = serializer.pack(errorPacket);
            errorAction.setData(packetBuffer);
            table.getNotifier().notifyPlayer(playerId, errorAction);
        }
    }

    private String getHandId() {
        return serverAdapter.getIntegrationHandId();
    }

    @VisibleForTesting
    protected void closePlayerSessions(Table table, Collection<PokerPlayer> pokerPlayers) {
        for (PokerPlayer pokerPlayer : pokerPlayers) {
            try {
                backendPlayerSessionHandler.endPlayerSessionInBackend(table, pokerPlayer, getCurrentRoundNumber(), state);
            } catch (Exception e) {
                log.error("error closing player session for player = " + pokerPlayer.getId(), e);
            }
        }
    }

    private int getCurrentRoundNumber() {
        return ((FirebaseState) state.getAdapterState()).getHandCount();
    }

    private Collection<PokerPlayer> removePlayersFromTable(Table table, Collection<GenericPlayer> players) {
        Collection<PokerPlayer> removedPlayers = new ArrayList<PokerPlayer>();

        for (GenericPlayer genericPlayer : players) {
            table.getPlayerSet().removePlayer(genericPlayer.getPlayerId());
            PokerPlayer pokerPlayer = state.getPokerPlayer(genericPlayer.getPlayerId());
            if (pokerPlayer != null) {
                log.debug("poker player not found from generic player {} while closing table", genericPlayer.getPlayerId());
                removedPlayers.add(pokerPlayer);
            }
        }

        return removedPlayers;
    }


    private void makeTableInvisibleInLobby(Table table) {
        log.debug("Setting table {} as invisible in lobby", table.getId());
        table.getAttributeAccessor().setIntAttribute(PokerLobbyAttributes.VISIBLE_IN_LOBBY.name(), 0);
    }


    private void printToErrorLog(AbstractGameAction action, Table table, Throwable throwable) {
        if (action instanceof GameDataAction) {
            GameDataAction gda = (GameDataAction) action;
            ProtocolObject packet = serializer.unpack(gda.getData());
            printActionsToErrorLog(throwable, "error handling game action: " + action + " Table: " + table.getId() + " Packet: " + packet, table);
        } else if (action instanceof GameObjectAction) {
            printActionsToErrorLog(throwable, "error handling command action: " + action + " on table: " + table, table);
        } else {
            printActionsToErrorLog(throwable, "error handling action (" + action.getClass().getSimpleName() + "): " + action + " on table: " + table, table);
        }
    }


    /**
     * Dump all cached actions to the error log in case of an error.
     *
     * @param throwable   optional throwable that caused the error
     * @param description description of the error
     * @param table       the current table
     */
    public void printActionsToErrorLog(Throwable throwable, String description, Table table) {
        List<GameAction> actions = actionCache.getPublicActions(table.getId());
        StringBuilder error = new StringBuilder(description);
        error.append("\nState: " + state);
        for (GameAction history : actions) {
            ProtocolObject packet = null;
            if (history instanceof GameDataAction) {
                GameDataAction dataAction = (GameDataAction) history;
                packet = serializer.unpack(dataAction.getData());
            }
            error.append("\n\t" + packet);
        }
        error.append("\nStackTrace: ");
        log.error(error.toString(), throwable);
    }

}
