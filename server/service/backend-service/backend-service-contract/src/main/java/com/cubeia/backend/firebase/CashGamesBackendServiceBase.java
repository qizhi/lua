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

import com.cubeia.backend.cashgame.CashGamesBackend;
import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.TournamentId;
import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.cashgame.callback.WalletCallback;
import com.cubeia.backend.cashgame.dto.AllowJoinResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableFailedResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableRequest;
import com.cubeia.backend.cashgame.dto.AnnounceTableResponse;
import com.cubeia.backend.cashgame.dto.BalanceUpdate;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.backend.cashgame.dto.BatchHandResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.OpenTableSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenTournamentSessionRequest;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse;
import com.cubeia.backend.cashgame.dto.ReserveRequest;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.cashgame.dto.TransferMoneyRequest;
import com.cubeia.backend.cashgame.exceptions.AnnounceTableFailedException;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.CloseSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.cashgame.exceptions.OpenSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.ReserveFailedException;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Base class for a service implementation. The implementing class
 * only needs to supply a {@link CashGamesBackend} implementation, the
 * asynchronous calls are taken care of by this class.
 */
public abstract class CashGamesBackendServiceBase implements CashGamesBackendService {

    private static Logger log = LoggerFactory.getLogger(CashGamesBackendServiceBase.class);

    private final long scheduleGraceDelay;

    private final ScheduledExecutorService executor;

    protected CashGamesBackendServiceBase(int numThread, long scheduleGraceDelay) {
        executor = Executors.newScheduledThreadPool(numThread);
        this.scheduleGraceDelay = scheduleGraceDelay;
    }

    protected abstract CashGamesBackend getCashGamesBackend();

    protected abstract ServiceRouter getServiceRouter();

    @Override
    public boolean isSystemShuttingDown() {
        return getCashGamesBackend().isSystemShuttingDown();
    }

    @Override
    public String generateHandId() {
        return getCashGamesBackend().generateHandId();
    }

    @Override
    public AllowJoinResponse allowJoinTable(int playerId) {
        return getCashGamesBackend().allowJoinTable(playerId);
    }

    @Override
    public void announceTable(final AnnounceTableRequest request) {
        scheduleCallback(new SafeRunnable() {

            @Override
            protected void execute() {
                WalletCallback callback = new TableCallback(request.tableId, getServiceRouter());
                try {
                    AnnounceTableResponse resp = getCashGamesBackend().announceTable(request);
                    callback.requestSucceeded(resp);
                } catch (AnnounceTableFailedException e) {
                    AnnounceTableFailedResponse resp = new AnnounceTableFailedResponse(e.errorCode, e.getMessage());
                    callback.requestFailed(resp);
                }
            }
        });
    }

    @Override
    public void openTableSession(final OpenTableSessionRequest request) {
        openSession(request, new TableCallback(request.getTableId(), getServiceRouter()));
    }

    @Override
    public void openTournamentSession(final OpenTournamentSessionRequest request) {
        openSession(request, new TournamentCallback(request.getTournamentId(), getServiceRouter()));
    }

    @Override
    public void openTournamentPlayerSession(final OpenTournamentSessionRequest request, TournamentSessionId tournamentSessionId) {
        openTournamentSession(request, new TournamentCallback(request.getTournamentId(), getServiceRouter()), tournamentSessionId);
    }

    private void openTournamentSession(final OpenSessionRequest request, final WalletCallback callback, final TournamentSessionId tournamentSessionId) {
        scheduleCallback(new SafeRunnable() {

            @Override
            protected void execute() {
                try {
                    OpenSessionResponse resp = getCashGamesBackend().openSession(request);

                    callback.requestSucceeded(resp);
                } catch (OpenSessionFailedException e) {
                    OpenSessionFailedResponse err = new OpenSessionFailedResponse(e.errorCode, e.getMessage(), request.playerId);
                    callback.requestFailed(err);
                }
            }
        });
    }

    @Override
    public void transfer(TransferMoneyRequest request) {
        // TODO: Make async (version)?
        getCashGamesBackend().transfer(request);
    }

    private void openSession(final OpenSessionRequest request, final WalletCallback callback) {
        scheduleCallback(new SafeRunnable() {

            @Override
            protected void execute() {
                try {
                    OpenSessionResponse resp = getCashGamesBackend().openSession(request);
                    callback.requestSucceeded(resp);
                } catch (OpenSessionFailedException e) {
                    OpenSessionFailedResponse err = new OpenSessionFailedResponse(e.errorCode, e.getMessage(), request.playerId);
                    callback.requestFailed(err);
                }
            }
        });
    }

    @Override
    public void reserveMoneyForTable(final ReserveRequest request, final TableId tableId) {
        reserve(request, new TableCallback(tableId, getServiceRouter()));
    }

    /**
     * This will reserve the given amount of money from the given player's account and place
     * it in the given session.
     * @param request
     * @param tournamentId
     */
    @Override
    public void reserveMoneyForTournament(final ReserveRequest request, TournamentId tournamentId) {
        reserve(request, new TournamentCallback(tournamentId, getServiceRouter()));
    }

    private void reserve(final ReserveRequest request, final WalletCallback callback) {
        scheduleCallback(new SafeRunnable() {

            @Override
            protected void execute() {
                try {
                    ReserveResponse resp = getCashGamesBackend().reserve(request);
                    callback.requestSucceeded(resp);
                } catch (ReserveFailedException e) {
                    ReserveFailedResponse resp = new ReserveFailedResponse(request.playerSessionId, e.errorCode, e.getMessage(),
                            e.playerSessionNeedsToBeClosed);
                    callback.requestFailed(resp);
                }
            }
        });
    }

    @Override
    public void closeSession(CloseSessionRequest request) throws CloseSessionFailedException {
        try {
            getCashGamesBackend().closeSession(request);
        } catch (Exception e) {
            throw new CloseSessionFailedException("Failed closing session for request: " + request, e);
        }
    }

    @Override
    public void closeTournamentSession(final CloseSessionRequest request, final TournamentId tournamentId) {
        final TournamentCallback callback = new TournamentCallback(tournamentId, getServiceRouter());
        scheduleCallback(new SafeRunnable() {
            @Override
            protected void execute() {
                try {
                    getCashGamesBackend().closeSession(request);
                } catch (CloseSessionFailedException e) {
                    CloseSessionFailedResponse resp = new CloseSessionFailedResponse(request.getPlayerSessionId(), e.getMessage());
                    callback.requestFailed(resp);
                }
            }
        });
    }

    @Override
    public BatchHandResponse batchHand(BatchHandRequest request) throws BatchHandFailedException {
        return getCashGamesBackend().batchHand(request);
    }

    @Override
    public Money getAccountBalance(int playerId, String currency) throws GetBalanceFailedException {
        return getCashGamesBackend().getAccountBalance(playerId, currency);
    }

    @Override
    public Currency getCurrency(String currencyCode) {
        return getCashGamesBackend().getCurrency(currencyCode);
    }

    @Override
    public BalanceUpdate getSessionBalance(PlayerSessionId sessionId) throws GetBalanceFailedException {
        return getCashGamesBackend().getSessionBalance(sessionId);
    }

    @Override
    public void transferMoneyToRakeAccount(PlayerSessionId sessionAccountToTransferFrom, Money moneyToTransferToRakeAccount, String comment) {
        getCashGamesBackend().transferMoneyToRakeAccount(sessionAccountToTransferFrom, moneyToTransferToRakeAccount, comment);
    }

    @Override
    public void transferMoneyFromRakeAccount(PlayerSessionId sessionAccountToTransferFrom, Money moneyToTransferToRakeAccount, String comment) {
        getCashGamesBackend().transferMoneyFromRakeAccount(sessionAccountToTransferFrom, moneyToTransferToRakeAccount, comment);
    }

    @Override
    public void transferMoneyFromPromotionsAccount(PlayerSessionId toAccount, Money amount, String comment) {
        getCashGamesBackend().transferMoneyFromPromotionsAccount(toAccount, amount, comment);
    }

    // --- PRIVATE METHODS --- //

    private void scheduleCallback(Runnable runnable) {
        executor.schedule(runnable, this.scheduleGraceDelay, MILLISECONDS);
    }

    private static class TableCallback implements WalletCallback {

        private final int gameId;
        private final int tableId;
        private final ServiceRouter router;

        private TableCallback(TableId table, ServiceRouter router) {
            gameId = table.gameId;
            tableId = table.tableId;
            this.router = router;
        }

        @Override
        public void requestSucceeded(Object response) {
            log.debug("Request succeeded: " + response);
            sendGameObjectActionToTable(gameId, tableId, response);
        }

        @Override
        public void requestFailed(Object response) {
            log.debug("Request failed: " + response);
            sendGameObjectActionToTable(gameId, tableId, response);
        }

        private void sendGameObjectActionToTable(int gameId, int tableId, Object object) {
            GameObjectAction action = new GameObjectAction(tableId);
            action.setAttachment(object);
            router.dispatchToGame(gameId, action);
        }
    }

    private static class TournamentCallback implements WalletCallback {

        private final int instanceId;
        private final ServiceRouter router;

        private TournamentCallback(TournamentId tournament, ServiceRouter router) {
            instanceId = tournament.instanceId;
            this.router = router;
        }

        @Override
        public void requestSucceeded(Object response) {
            log.debug("Request succeeded: " + response);
            sendObjectToTournament(instanceId, response);
        }

        @Override
        public void requestFailed(Object response) {
            log.debug("Request failed: " + response);
            sendObjectToTournament(instanceId, response);
        }

        private void sendObjectToTournament(int instanceId, Object object) {
            MttObjectAction action = new MttObjectAction(instanceId, object);
            router.dispatchToTournament(instanceId, action);
        }
    }
}
