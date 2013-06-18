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

package com.cubeia.games.poker.activator;

import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_EXTERNAL_ID;
import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_TEMPLATE;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.cubeia.games.poker.common.money.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.AnnounceTableRequest;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.DefaultCreationParticipant;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.PokerVariant;
import com.cubeia.poker.variant.factory.GameTypeFactory;
import com.cubeia.poker.variant.telesina.TelesinaDeckUtil;


/**
 * Table Creator.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerParticipant extends DefaultCreationParticipant {

    private static final TelesinaDeckUtil TELESINA_DECK_UTIL = new TelesinaDeckUtil();

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(PokerParticipant.class);

    public static final int GAME_ID = 1;

    private final String domain;
    private final PokerStateCreator stateCreator;
    private final CashGamesBackendService cashGameBackendService;
    private final TableConfigTemplate template;

    private final TableNameManager tableNamer;

    public PokerParticipant(TableConfigTemplate template, String domain, PokerStateCreator stateCreator,
            CashGamesBackendService cashGameBackendService, TableNameManager tableNamer) {
        this.domain = domain;
        this.template = template;
        this.stateCreator = stateCreator;
        this.cashGameBackendService = cashGameBackendService;
        this.tableNamer = tableNamer;
    }

    @Override
    public LobbyPath getLobbyPathForTable(Table table) {
        return new LobbyPath(GAME_ID, domain + "/" + template.getVariant().name());
    }

    @Override
    public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
        super.tableCreated(table, acc);
        PokerVariant variant = template.getVariant();

        // Create state.
        PokerState pokerState = stateCreator.newPokerState();
        GameType gameType = GameTypeFactory.createGameType(variant);
        String externalTableId = "TABLE::" + UUID.randomUUID();
        PokerSettings settings = createSettings(table, externalTableId);
        pokerState.init(gameType, settings);
        pokerState.setAdapterState(new FirebaseState());
        pokerState.setTableId(table.getId());
        table.getGameState().setState(pokerState);

        // Set lobby attributes
        acc.setIntAttribute(TABLE_TEMPLATE.name(), template.getId());
        acc.setIntAttribute(PokerLobbyAttributes.VISIBLE_IN_LOBBY.name(), 0);
        acc.setStringAttribute(PokerLobbyAttributes.SPEED.name(), template.getTiming().getName());
        acc.setStringAttribute(PokerLobbyAttributes.ANTE.name(), template.getAnte().toPlainString());
        acc.setStringAttribute(PokerLobbyAttributes.SMALL_BLIND.name(), settings.getSmallBlindAmount().toPlainString());
        acc.setStringAttribute(PokerLobbyAttributes.BIG_BLIND.name(), settings.getBigBlindAmount().toPlainString());
        acc.setStringAttribute(PokerLobbyAttributes.BETTING_GAME_BETTING_MODEL.name(), settings.getBetStrategyType().name());
        acc.setStringAttribute(PokerLobbyAttributes.CURRENCY_CODE.name(),settings.getCurrency().getCode());
        acc.setStringAttribute(PokerLobbyAttributes.MONETARY_TYPE.name(), "REAL_MONEY");
        acc.setStringAttribute(PokerLobbyAttributes.VARIANT.name(), variant.name());
        acc.setStringAttribute(PokerLobbyAttributes.MIN_BUY_IN.name(), pokerState.getMinBuyIn().toPlainString());
        acc.setStringAttribute(PokerLobbyAttributes.MAX_BUY_IN.name(), pokerState.getMaxBuyIn().toPlainString());
        int deckSize = TELESINA_DECK_UTIL.createDeckCards(pokerState.getTableSize()).size();
        acc.setIntAttribute(PokerLobbyAttributes.DECK_SIZE.name(), deckSize);
        acc.setStringAttribute(PokerLobbyAttributes.TABLE_EXTERNAL_ID.name(), externalTableId);

        // Announce table
        // FirebaseCallbackFactory callbackFactory = cashGameBackendService.getCallbackFactory();
        AnnounceTableRequest announceRequest = new AnnounceTableRequest(new TableId(table.getMetaData().getGameId(), table.getId()));   // TODO: this should be the id from the table record
        cashGameBackendService.announceTable(announceRequest);
    }

    private PokerSettings createSettings(Table table, String externalTableId) {
        BigDecimal minBuyIn = template.getMinBuyIn();
        BigDecimal maxBuyIn = template.getMaxBuyIn();

        /*if (template.getBetStrategy() == BetStrategyType.FIXED_LIMIT) {
            maxBuyIn = Integer.MAX_VALUE;
        }*/

        int seats = table.getPlayerSet().getSeatingMap().getNumberOfSeats();
        RakeSettings rake = template.getRakeSettings();
        // Map<Serializable,Serializable> attributes = Collections.emptyMap();
        Map<Serializable, Serializable> attributes = Collections.<Serializable, Serializable>singletonMap(TABLE_EXTERNAL_ID.name(), externalTableId);
        BigDecimal smallBlindAmount = template.getSmallBlind();
        BigDecimal bigBlindAmount = template.getBigBlind();
        BlindsLevel level = new BlindsLevel(smallBlindAmount, bigBlindAmount, template.getAnte());
        Currency currency = cashGameBackendService.getCurrency(template.getCurrency());
        return new PokerSettings(level, template.getBetStrategy(), minBuyIn, maxBuyIn, template.getTiming(), seats, rake, currency, attributes);
    }

    @Override
    public String getTableName(GameDefinition def, Table t) {
        return tableNamer.tableCreated(t);
    }

    public int getSeats() {
        return template.getSeats();
    }

    public CashGamesBackendService getCashGameBackendService() {
        return cashGameBackendService;
    }

    public TableConfigTemplate getTemplate() {
        return template;
    }

    @Override
    public String toString() {
        return "PokerParticipant [domain=" + domain + ", template=" + template + "]";
    }
}
