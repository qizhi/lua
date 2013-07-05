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

import static com.cubeia.games.poker.handler.BackendCallHandler.EXT_PROP_KEY_TABLE_ID;

import com.cubeia.games.poker.common.money.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.AllowJoinResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenTableSessionRequest;
import com.cubeia.backend.cashgame.exceptions.CloseSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.adapter.domainevents.DomainEventAdapter;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.player.PokerPlayer;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import java.math.BigDecimal;

public class BackendPlayerSessionHandler {
    private static Logger log = LoggerFactory.getLogger(BackendPlayerSessionHandler.class);

    @Service
    @VisibleForTesting
    protected CashGamesBackendService cashGameBackend;
    
    @Service
    @VisibleForTesting
    protected PokerConfigurationService configService;

    @Inject
    private TableCloseHandler closeHandler;
    
    @Inject DomainEventAdapter achievementAdapter;

    public AllowJoinResponse allowJoinTable(int playerId) {
        return cashGameBackend.allowJoinTable(playerId); 
    }

    public void endPlayerSessionInBackend(Table table, PokerPlayer pokerPlayer, int roundNumber, PokerState state) {
        if (!(pokerPlayer instanceof PokerPlayerImpl)) {
            throw new IllegalStateException("must be a PokerPlayerImpl");
        }

        PokerPlayerImpl pokerPlayerImpl = (PokerPlayerImpl) pokerPlayer;

        PlayerSessionId sessionId = pokerPlayerImpl.getPlayerSessionId();

        int playerId = pokerPlayer.getId();
		log.debug("Handle session end for player[" + playerId + "], sessionid[" + sessionId + "]");
        if (sessionId != null) {
            CloseSessionRequest closeSessionRequest = new CloseSessionRequest(sessionId);
            try {
                cashGameBackend.closeSession(closeSessionRequest);
            } catch (CloseSessionFailedException e) {
                log.error("error ending wallet session: " + sessionId, e);
            } finally {
                pokerPlayer.clearBalance();
                pokerPlayerImpl.setPlayerSessionId(null);
            }
        }
        
		try {
			Money accountBalance = cashGameBackend.getAccountBalance(playerId, state.getSettings().getCurrency().getCode());
	        achievementAdapter.notifyEndPlayerSession(playerId, pokerPlayer.getScreenname(), pokerPlayer.getOperatorId(), accountBalance);
		} catch (GetBalanceFailedException e) {
			log.error("Failed to get player account balance", e);
		}
        
    }

    public void startWalletSession(PokerState state, Table table, int playerId) {
        log.debug("starting wallet session: tId = {}, pId = {}", table.getId(), playerId);
        TableId tableId = (TableId) state.getExternalTableProperties().get(EXT_PROP_KEY_TABLE_ID);
        if (tableId == null) {
            log.error("No table ID found in external properties; Table must be announced first; tId = {}", table.getId());
            log.debug("Crashing table " + table.getId());
            closeHandler.tableCrashed(table);
        } else {
            String currency = state.getSettings().getCurrency().getCode();
            Money openingBalance = new Money(BigDecimal.ZERO, new Currency(currency, 2));
            OpenTableSessionRequest openSessionRequest = new OpenTableSessionRequest(playerId, tableId, openingBalance);
            cashGameBackend.openTableSession(openSessionRequest);
        }

    }
}
