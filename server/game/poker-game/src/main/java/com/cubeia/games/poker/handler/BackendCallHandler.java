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

package com.cubeia.games.poker.handler;

import static com.cubeia.backend.firebase.CashGamesBackendService.MARKET_TABLE_REFERENCE_KEY;
import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static com.cubeia.games.poker.model.PokerPlayerImpl.ATTR_PLAYER_EXTERNAL_SESSION_ID;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.dto.AnnounceTableResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.io.protocol.BuyInResponse;
import com.cubeia.games.poker.io.protocol.Enums;
import com.cubeia.games.poker.io.protocol.Enums.BuyInResultCode;
import com.cubeia.games.poker.io.protocol.Enums.ErrorCode;
import com.cubeia.games.poker.io.protocol.ErrorPacket;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.player.PokerPlayer;
import com.google.inject.Inject;

public class BackendCallHandler {
    public static final String EXT_PROP_KEY_TABLE_ID = "tableId";

    private static Logger log = LoggerFactory.getLogger(BackendCallHandler.class);

    private final PokerState state;

    private final Table table;

    private final BackendPlayerSessionHandler backendPlayerSessionHandler;

    private StyxSerializer styx = new StyxSerializer(null);

    @Inject
    public BackendCallHandler(PokerState state, Table table, BackendPlayerSessionHandler backendPlayerSessionHandler) {
        this.state = state;
        this.table = table;
        this.backendPlayerSessionHandler = backendPlayerSessionHandler;
    }

    public void handleReserveSuccessfulResponse(ReserveResponse reserveResponse) {
        // TODO: A lot of ask don't tell going on here.
        int playerId = reserveResponse.getPlayerSessionId().playerId;
        PokerPlayerImpl pokerPlayer = (PokerPlayerImpl) state.getPokerPlayer(playerId);
        Money amountReserved = reserveResponse.getAmountReserved();
        log.debug("handle reserve response: session = {}, amount = {}, pId = {}, properties = {}",
                new Object[]{reserveResponse.getPlayerSessionId(), amountReserved, pokerPlayer.getId(), reserveResponse.getReserveProperties()});

        pokerPlayer.addNotInHandAmount(amountReserved.getAmount());

        String externalPlayerSessionReference = reserveResponse.getReserveProperties().get(CashGamesBackendService.MARKET_TABLE_SESSION_REFERENCE_KEY);
        pokerPlayer.getAttributes().put(ATTR_PLAYER_EXTERNAL_SESSION_ID, externalPlayerSessionReference);

        pokerPlayer.clearRequestedBuyInAmountAndRequest();

        Serializable marketTableRef = state.getExternalTableProperties().get(MARKET_TABLE_REFERENCE_KEY);
        state.getServerAdapter().notifyExternalSessionReferenceInfo(
                playerId,
                marketTableRef == null ? null : marketTableRef.toString(),
                externalPlayerSessionReference);

        // TODO: response should move to PokerHandler.handleReserveResponse
        BuyInResponse resp = new BuyInResponse();
        resp.balance = format(pokerPlayer.getBalance());
        resp.pendingBalance = format(pokerPlayer.getPendingBalanceSum());
        resp.amountBroughtIn = format(amountReserved.getAmount());
        resp.resultCode = Enums.BuyInResultCode.OK;

        if (!state.isPlayerInHand(playerId)) {
            pokerPlayer.commitBalanceNotInHand(state.getMaxBuyIn());
        }

        sendGameData(playerId, resp);

        if (pokerPlayer.isSitInAfterSuccessfulBuyIn()) {
            state.playerIsSittingIn(playerId);
        }

        state.getServerAdapter().notifyPlayerBalance(pokerPlayer);

    }

    public void handleReserveFailedResponse(ReserveFailedResponse response) {
        int playerId = response.getSessionId().playerId;


        BuyInResultCode errorCode;

        switch (response.getErrorCode()) {
            case AMOUNT_TOO_HIGH:
                errorCode = Enums.BuyInResultCode.AMOUNT_TOO_HIGH;
                break;

            case MAX_LIMIT_REACHED:
                errorCode = Enums.BuyInResultCode.MAX_LIMIT_REACHED;
                break;

            case SESSION_NOT_OPEN:
                errorCode = Enums.BuyInResultCode.SESSION_NOT_OPEN;
                break;

            default:
                errorCode = Enums.BuyInResultCode.UNSPECIFIED_ERROR;
                break;
        }

        PokerPlayer player = state.getPokerPlayer(playerId);

        if (player.isBuyInRequestActive()) {
            log.error("reserve failed but player had no active request, player id = {}", playerId);
        }

        player.clearRequestedBuyInAmountAndRequest();

        if (response.isPlayerSessionNeedsToBeClosed()) {
            sendGeneralErrorMessageToClient(player, Enums.ErrorCode.CLOSED_SESSION_DUE_TO_FATAL_ERROR, getHandId());

            try {
                backendPlayerSessionHandler.endPlayerSessionInBackend(table, player, getCurrentRoundNumber(), state);
            } catch (Exception e) {
                log.error("error closing player session for player = " + player.getId(), e);
            }

            state.unseatPlayer(playerId, false);

        } else {
            sendBuyInResponseToPlayer(playerId, errorCode);
        }
    }

    private void sendBuyInResponseToPlayer(int playerId, BuyInResultCode errorCode) {
        BuyInResponse resp = new BuyInResponse();
        resp.resultCode = errorCode;
        resp.balance = "N/A";
        resp.amountBroughtIn = "0";
        resp.pendingBalance = "0";
        sendGameData(playerId, resp);
    }

    private int getCurrentRoundNumber() {
        return ((FirebaseState) state.getAdapterState()).getHandCount();
    }

    private String getHandId() {
        return state.getServerAdapter().getIntegrationHandId();
    }

    public void handleOpenSessionSuccessfulResponse(OpenSessionResponse openSessionResponse) {
        PlayerSessionId playerSessionId = openSessionResponse.getSessionId();
        int playerId = playerSessionId.playerId;

        // TODO: This ain't pretty. Either make PokerPlayer know about sessions or hold the session in some wrapper.
        PokerPlayerImpl pokerPlayer = (PokerPlayerImpl) state.getPokerPlayer(playerId);
        pokerPlayer.setPlayerSessionId(playerSessionId);
        state.playerOpenedSession(playerId);
    }

    public void handleAnnounceTableSuccessfulResponse(AnnounceTableResponse attachment) {
        log.trace("handle announce table success, tId = {}, intTableId = {}, tableProperties = {}", new Object[]{table.getId(), attachment.getTableId(), attachment.getTableProperties()});
        if (attachment.getTableId() == null) {
            log.error("got announce successful callback but the external table id is null! Attachment: {}", attachment);
            LobbyTableAttributeAccessor attributeAccessor = table.getAttributeAccessor();
            attributeAccessor.setIntAttribute(PokerLobbyAttributes.TABLE_READY_FOR_CLOSE.name(), 1);
        } else {
            Map<String, Serializable> extProps = state.getExternalTableProperties();
            extProps.put(EXT_PROP_KEY_TABLE_ID, attachment.getTableId());
            extProps.putAll(attachment.getTableProperties());
            makeTableVisibleInLobby(table);
        }
    }

    private void makeTableVisibleInLobby(Table table) {
        //log.debug("setting table {} as visible in lobby", table.getTableId());
        table.getAttributeAccessor().setIntAttribute(PokerLobbyAttributes.VISIBLE_IN_LOBBY.name(), 1);
    }

    /**
     * This table has not been approved by 3rd party (e.g. Italian government).
     * We need to close it asap.
     *
     */
    public void handleAnnounceTableFailedResponse() {
        log.info("handle Announce Table Failed for table[" + table.getId() + "], will flag for removal");
        LobbyTableAttributeAccessor attributeAccessor = table.getAttributeAccessor();
        attributeAccessor.setIntAttribute(PokerLobbyAttributes.TABLE_READY_FOR_CLOSE.name(), 1);
    }

    public void handleOpenSessionFailedResponse(OpenSessionFailedResponse response) {
        log.info("handle Open Session Failed on table[" + table.getId() + "]: " + response);
        int playerId = response.getPlayerId();

        sendBuyInErrorToClientAndUnseatPlayer(playerId, true, Enums.BuyInResultCode.SESSION_NOT_OPEN);
    }

    private void sendBuyInErrorToClientAndUnseatPlayer(int playerId, boolean setAsWatcher, BuyInResultCode buyInResultCode) {
        log.debug("sending buy in error to client: player = {}, result code = {}", playerId, buyInResultCode);

        sendBuyInResponseToPlayer(playerId, buyInResultCode);

        // Unseat player and optionally set as watcher
        state.unseatPlayer(playerId, setAsWatcher);
    }

    private void sendGeneralErrorMessageToClient(PokerPlayer player, ErrorCode errorCode, String handId) {
        log.debug("sending general error message to client: player = {}, result code = {}, hand id = {}",
                new Object[]{player.getId(), errorCode, handId});

        ErrorPacket errorPacket = new ErrorPacket(errorCode, handId);
        GameDataAction errorAction = new GameDataAction(player.getId(), table.getId());
        ByteBuffer packetBuffer;
        packetBuffer = styx.pack(errorPacket);
        errorAction.setData(packetBuffer);
        table.getNotifier().notifyPlayer(player.getId(), errorAction);
    }


    private void sendGameData(int playerId, ProtocolObject resp) {
        GameDataAction action = new GameDataAction(playerId, table.getId());
        action.setData(styx.pack(resp));

        table.getNotifier().notifyPlayer(playerId, action);
    }

}
