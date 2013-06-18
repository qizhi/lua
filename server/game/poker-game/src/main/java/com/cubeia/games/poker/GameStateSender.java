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

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.cache.ActionContainer;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.io.protocol.*;
import com.cubeia.games.poker.util.ProtocolFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class responsible for sending the cached game state to a connecting or re-connecting client.
 *
 * @author w
 */
public class GameStateSender {
    private static Logger log = LoggerFactory.getLogger(PokerTableListener.class);
    private final ActionCache actionCache;
    private SystemTime dateFetcher;

    @Inject
    public GameStateSender(ActionCache actionCache, SystemTime dateFetcher) {
        this.dateFetcher = dateFetcher;
        this.actionCache = actionCache;
    }

    public void sendGameState(Table table, int playerId) {
        try {
            ProtocolFactory protocolFactory = new ProtocolFactory();
            int tableId = table.getId();

            log.debug("sending stored game actions to client, player id = {}", playerId);
            List<GameAction> actions = new LinkedList<GameAction>();
            actions.add(protocolFactory.createGameAction(new StartHandHistory(), playerId, tableId));

            Collection<ActionContainer> containers = actionCache.getPrivateAndPublicActions(tableId, playerId);
            Collection<GameAction> actionsFromCache = filterRequestActions(containers, playerId);
            actions.addAll(actionsFromCache);

            actions.add(protocolFactory.createGameAction(new StopHandHistory(), playerId, tableId));

            table.getNotifier().notifyPlayer(playerId, actions);
        } catch (Exception e) {
            log.error("Failed to create and send game state to player " + playerId, e);
        }
    }

    /**
     * 1. Filter the game actions list by removing all GameDataActions containing RequestAction packets
     * that have been answered by a PerformAction.
     * <p/>
     * 2. Remove all actions that are marked as excluded for this player id to avoid duplicates.
     * <p/>
     * 3. Remove all disconnect packets and add to last request if applicable
     * <p/>
     * If there is one last action request and a disconnect after that we will adjust the
     * time out for that action request.
     *
     * @param actions   actions to filter
     * @param playerId, player id to check for exclusion.
     * @return new filtered list
     * @throws IOException
     */
    @VisibleForTesting
    protected List<GameAction> filterRequestActions(Collection<ActionContainer> actions, int playerId) throws IOException {
        LinkedList<GameAction> filteredActions = new LinkedList<GameAction>();
        StyxSerializer styxalizer = new StyxSerializer(new ProtocolObjectFactory());

        ActionContainer lastContainer = null;
        Long lastRequestTimeStamp = null;
        RequestAction lastRequest = null;

        for (ActionContainer container : actions) {
            if (container.getExcludedPlayerId() != null && container.getExcludedPlayerId() == playerId) {
                continue; // Exclude this action from the list
            }

            GameAction ga = container.getGameAction();
            if (ga instanceof GameDataAction) {
                GameDataAction gda = (GameDataAction) ga;
                ProtocolObject packet;
                try {
                    packet = styxalizer.unpack(gda.getData());

                    if (packet instanceof RequestAction) {

                        // special case for ante since that can be sent out of order
                        // we need to always send all ante
                        RequestAction requestAction = (RequestAction) packet;

                        boolean anteAllowed = false;

                        for (PlayerAction playerAction : requestAction.allowedActions) {
                            if (playerAction.type == Enums.ActionType.ANTE) {
                                anteAllowed = true;
                            }
                        }

                        if (anteAllowed) {
                            ga = adjustTimeToAct(styxalizer, container, requestAction, container.getTimestamp());
                            filteredActions.add(ga);
                        } else {
                            lastRequest = requestAction;
                            lastContainer = container;
                            lastRequestTimeStamp = container.getTimestamp();
                        }

                    } else if (packet instanceof PlayerDisconnectedPacket) {
                        PlayerDisconnectedPacket disconnect = (PlayerDisconnectedPacket) packet;
                        // Store and send packet, but also adjust the time out for this disconnect
                        // and the last found action request to make it easier for the client.
                        if (lastRequest != null) {
                            lastRequest.timeToAct = disconnect.timebank;
                            lastRequestTimeStamp = container.getTimestamp();
                        }
                        // ordering is important here, the adjust time method will change the time allows on
                        // the disconnect which is also used above. So, we change it after we have modified last request action
                        ga = adjustTimeToAct(styxalizer, container, disconnect);
                        filteredActions.add(ga);

                    } else if (packet instanceof PerformAction) {
                        lastRequest = null;
                        lastContainer = null;
                        lastRequestTimeStamp = null;
                        filteredActions.add(ga);

                    } else {
                        filteredActions.add(ga);
                    }
                } catch (IOException e) {
                    log.error("error unpacking cached packet", e);
                }
            } else {
                filteredActions.add(ga);
            }
        }

        // If we have an unanswered request then adjust the time left to act and add it last
        if (lastRequest != null) {
            GameDataAction requestAction = adjustTimeToAct(styxalizer, lastContainer, lastRequest, lastRequestTimeStamp);
            filteredActions.add(requestAction);
        }

        return filteredActions;
    }

    private GameDataAction adjustTimeToAct(StyxSerializer styxalizer, ActionContainer lastContainer, RequestAction lastRequest, Long timerTimeStamp) throws IOException {
        long elapsed = dateFetcher.date().getMillis() - timerTimeStamp;
        int timeToAct = lastRequest.timeToAct - (int) elapsed;
        if (timeToAct < 0) {
            timeToAct = 0;
        }
        lastRequest.timeToAct = timeToAct;
        GameDataAction requestAction = (GameDataAction) lastContainer.getGameAction();
        requestAction.setData(styxalizer.pack(lastRequest));
        return requestAction;
    }

    private GameDataAction adjustTimeToAct(StyxSerializer styxalizer, ActionContainer container, PlayerDisconnectedPacket disconnect) throws IOException {
        long elapsed = dateFetcher.date().getMillis() - container.getTimestamp();
        int timeToAct = disconnect.timebank - (int) elapsed;
        if (timeToAct < 0) {
            timeToAct = 0;
        }
        disconnect.timebank = timeToAct;
        GameDataAction disconnectAction = (GameDataAction) container.getGameAction();
        disconnectAction.setData(styxalizer.pack(disconnect));
        return disconnectAction;
    }

}
