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

package com.cubeia.backend.firebase;

import com.cubeia.backend.cashgame.Asynchronous;
import com.cubeia.backend.cashgame.CashGamesBackend;
import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.TournamentId;
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.cashgame.dto.AllowJoinResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableRequest;
import com.cubeia.backend.cashgame.dto.BalanceUpdate;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.backend.cashgame.dto.BatchHandResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenTableSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenTournamentSessionRequest;
import com.cubeia.backend.cashgame.dto.ReserveRequest;
import com.cubeia.backend.cashgame.dto.TransferMoneyRequest;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.CloseSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;

public interface AsynchronousCashGamesBackend {

    /**
     * See {@link CashGamesBackend} for documentation. 
     */
    String generateHandId();

    /**
     * See {@link CashGamesBackend} for documentation. 
     */
    AllowJoinResponse allowJoinTable(int playerId);

    /**
     * See {@link CashGamesBackend} for documentation. 
     */
    boolean isSystemShuttingDown();

    /**
     * This is an asynchronous call, the response
     * will be sent as object action to the table. 
     * 
     * <p>See {@link CashGamesBackend} for more 
     * documentation.</p> 
     */
    @Asynchronous
    void announceTable(AnnounceTableRequest request);

    /**
     * This is an asynchronous call, the response
     * will be sent as object action to the table. 
     * 
     * <p>See {@link CashGamesBackend#openSession(OpenSessionRequest)} for more
     * documentation.</p> 
     */
    @Asynchronous
    void openTableSession(OpenTableSessionRequest request);

    /**
     * This is an asynchronous call, the response
     * will be sent as object action to the tournament.
     *
     */
    @Asynchronous
    void openTournamentSession(OpenTournamentSessionRequest request);

    /**
     * This is an asynchronous call, the response
     * will be sent as object action to the tournament.
     *
     * The opening balance will be transferred to the tournament account
     * directly after the session has been opened.
     *
     */
    @Asynchronous
    void openTournamentPlayerSession(OpenTournamentSessionRequest request, TournamentSessionId tournamentSessionId);

    /**
     * See {@link CashGamesBackend#closeSession(CloseSessionRequest)} for documentation.
     */
    void closeSession(CloseSessionRequest request) throws CloseSessionFailedException;

    /**
     * See {@link CashGamesBackend#closeSession(CloseSessionRequest)} for documentation.
     */
    @Asynchronous
    void closeTournamentSession(CloseSessionRequest request, TournamentId tournamentId);

    /**
     * This is an asynchronous call, the response
     * will be sent as object action to the table. 
     * 
     * <p>See {@link CashGamesBackend#reserve(ReserveRequest)} for more
     * documentation.</p> 
     */
    @Asynchronous
    void reserveMoneyForTable(ReserveRequest request, TableId tableId);

    @Asynchronous
    void reserveMoneyForTournament(ReserveRequest request, TournamentId tournamentId);

    /**
     * See {@link CashGamesBackend#transfer(TransferMoneyRequest)} for documentation.
     */
    public void transfer(TransferMoneyRequest request);

    /**
     * See {@link CashGamesBackend#batchHand(BatchHandRequest)} for documentation.
     */
    BatchHandResponse batchHand(BatchHandRequest request) throws BatchHandFailedException;

    /**
     * See {@link CashGamesBackend#getAccountBalance(int, String)} for documentation.
     */
    Money getAccountBalance(int playerId, String currency) throws GetBalanceFailedException;

    /**
     * See {@link CashGamesBackend#getSessionBalance(PlayerSessionId)} for documentation.
     */    
    BalanceUpdate getSessionBalance(PlayerSessionId sessionId) throws GetBalanceFailedException;

    void transferMoneyFromPromotionsAccount(PlayerSessionId toAccount, Money amount, String comment);

    /**
     * See {@link CashGamesBackend#transferMoneyToRakeAccount(PlayerSessionId, Money, String)} for documentation.
     */
    void transferMoneyToRakeAccount(PlayerSessionId sessionAccountToTransferFrom, Money moneyToTransferToRakeAccount, String comment);

    /**
     * See {@link CashGamesBackend#transferMoneyFromRakeAccount(PlayerSessionId, Money, String)} for documentation.
     */
    void transferMoneyFromRakeAccount(PlayerSessionId sessionAccountToTransferFrom, Money moneyToTransferToRakeAccount, String comment);
    
    Currency getCurrency(String currencyCode);
}
