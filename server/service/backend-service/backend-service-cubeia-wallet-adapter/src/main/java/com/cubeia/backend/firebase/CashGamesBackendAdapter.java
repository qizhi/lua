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
import com.cubeia.backend.cashgame.TransactionId;
import com.cubeia.backend.cashgame.dto.AllowJoinResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableRequest;
import com.cubeia.backend.cashgame.dto.AnnounceTableResponse;
import com.cubeia.backend.cashgame.dto.BalanceUpdate;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.backend.cashgame.dto.BatchHandResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.HandResult;
import com.cubeia.backend.cashgame.dto.OpenSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse.ErrorCode;
import com.cubeia.backend.cashgame.dto.ReserveRequest;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.cashgame.dto.TransactionUpdate;
import com.cubeia.backend.cashgame.dto.TransferMoneyRequest;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.cashgame.exceptions.OpenSessionFailedException;
import com.cubeia.backend.cashgame.exceptions.ReserveFailedException;
import com.cubeia.backoffice.accounting.api.UnbalancedTransactionException;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.games.poker.common.money.*;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import com.cubeia.network.wallet.firebase.domain.TransactionBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse.ErrorCode.UNSPECIFIED_ERROR;

/**
 * Adapter from the Backend Service Contract to the Cubeia Wallet Service.
 *
 * @author w
 */
public class CashGamesBackendAdapter implements CashGamesBackend {

    /**
     * Hardcoded licensee id, should be part of the open session request
     */
    public static final int LICENSEE_ID = 0;

    /**
     * Hardcoded game id, should be configurable or part of requests
     */
    public static final int GAME_ID = 1;

    static final Long RAKE_ACCOUNT_USER_ID = -1000L;

    static final Long PROMOTIONS_ACCOUNT_USER_ID = -2000L;

    private Logger log = LoggerFactory.getLogger(CashGamesBackendAdapter.class);

    private final AtomicLong idSequence = new AtomicLong(0);

    @VisibleForTesting
    protected WalletServiceContract walletService;

    protected AccountLookupUtil accountLookupUtil;

    /**
     * Maps currency to rake account.
     */
    @VisibleForTesting
    protected Map<String, Long> rakeAccounts = Maps.newHashMap();

    /**
     * Maps currency to promotions account.
     */
    protected Map<String, Long> promotionsAccounts = Maps.newHashMap();

    public CashGamesBackendAdapter(WalletServiceContract walletService, AccountLookupUtil accountLookupUtil) throws SystemException {
        this.walletService = walletService;
        this.accountLookupUtil = accountLookupUtil;
    }

    @Override
    public boolean isSystemShuttingDown() {
        return false;
    }

    private long nextId() {
        return idSequence.getAndIncrement();
    }

    @Override
    public String generateHandId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public AllowJoinResponse allowJoinTable(int playerId) {
        log.warn("allow join not implemented, will always return ok");
        return new AllowJoinResponse(true, -1);
    }

    @Override
    public AnnounceTableResponse announceTable(AnnounceTableRequest request) {
        String uniqueId = UUID.randomUUID().toString();
        final AnnounceTableResponse response = new AnnounceTableResponse(new TableId(request.tableId, uniqueId));
        response.setProperty(CashGamesBackendService.MARKET_TABLE_REFERENCE_KEY, "CUBEIA-TABLE-ID::" + uniqueId);
        return response;
    }

    @Override
    public OpenSessionResponse openSession(final OpenSessionRequest request) throws OpenSessionFailedException {
        OpenSessionResponse response = null;
        try {
            Long walletSessionId = walletService.startSession(request.getOpeningBalance().getCurrencyCode(), LICENSEE_ID, request.getPlayerId(),
                    request.getObjectId(), GAME_ID, "unknown-" + request.getPlayerId(), request.getAccountName());

            PlayerSessionId sessionId = new PlayerSessionId(request.playerId, String.valueOf(walletSessionId));
            response = new OpenSessionResponse(sessionId, Collections.<String, String>emptyMap());
            log.debug("new session opened, oId = {}, pId = {}, sId = {}", new Object[]{request.getObjectId(), request.getPlayerId(), response.getSessionId()});

            if (request.openingBalance.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                log.debug("Opening session requests a reservation with amount: " + request.openingBalance.getAmount());
                reserve(new ReserveRequest(response.getSessionId(), request.getOpeningBalance()));
            }

            return response;
        } catch (Exception e) {
            if (response != null) {
                log.error("Failed opening session", e);
                closeSession(new CloseSessionRequest(response.getSessionId()));
            }
            String msg = "error opening session for player " + request.getPlayerId() + ": " + e.getMessage();
            throw new OpenSessionFailedException(msg, e, UNSPECIFIED_ERROR);
        }
    }

    @Override
    public void closeSession(CloseSessionRequest request) {
        PlayerSessionId sid = request.getPlayerSessionId();
        long walletSessionId = getWalletSessionIdByPlayerSessionId(sid);
        log.debug("Closing session " + walletSessionId);
        com.cubeia.backoffice.accounting.api.Money amountDeposited = walletService.endSessionAndDepositAll(LICENSEE_ID, walletSessionId,
                "session ended by game " + GAME_ID + ", player id = " + sid.playerId);

        log.debug("wallet session {} closed for player {}, amount deposited: {}", new Object[]{walletSessionId, sid.playerId, amountDeposited});
    }

    private long getWalletSessionIdByPlayerSessionId(PlayerSessionId sid) {
        return Long.valueOf(sid.integrationSessionId);
    }

    @Override
    public ReserveResponse reserve(final ReserveRequest request) throws ReserveFailedException {
        Money amount = request.getAmount();
        PlayerSessionId sid = request.getPlayerSessionId();
        Long walletSessionId = getWalletSessionIdByPlayerSessionId(sid);
        com.cubeia.backoffice.accounting.api.Money walletAmount = convertToWalletMoney(amount);
        try {
            log.debug("Sending withdrawal request. " + request);
            walletService.withdraw(walletAmount, LICENSEE_ID, walletSessionId, "reserve " + amount + " by player " + sid.playerId);

            AccountBalanceResult sessionBalance = walletService.getBalance(walletSessionId);
            Money newBalance = convertFromWalletMoney(sessionBalance.getBalance());

            BalanceUpdate balanceUpdate = new BalanceUpdate(request.getPlayerSessionId(), newBalance, nextId());
            ReserveResponse response = new ReserveResponse(balanceUpdate, amount);
            log.debug("reserve successful: sId = {}, amount = {}, new balance = {}", new Object[]{sid, amount, newBalance});
            response.setProperty(CashGamesBackendService.MARKET_TABLE_SESSION_REFERENCE_KEY, "CUBEIA-MARKET-SID-" + sid.hashCode());
            return response;
        } catch (Exception e) {
            log.error("Failed reserving money", e);
            String msg = "error reserving " + amount + " to session " + walletSessionId + " for player " + sid.playerId + ": " + e.getMessage();

            throw new ReserveFailedException(msg, e, ErrorCode.UNSPECIFIED_FAILURE, true);
        }
    }

    /**
     * Convert from wallet money type to backend money type.
     *
     * @param amount wallet money amount
     * @return converted amount
     */
    private Money convertFromWalletMoney(com.cubeia.backoffice.accounting.api.Money amount) {
        return new Money(amount.getAmount(), new Currency(amount.getCurrencyCode(), amount.getFractionalDigits()));
    }

    /**
     * Convert from backend money type to wallet money type.
     *
     * @param amount amount to convert
     * @return converted amount
     */
    private com.cubeia.backoffice.accounting.api.Money convertToWalletMoney(Money amount) {
        return new com.cubeia.backoffice.accounting.api.Money(amount.getCurrencyCode(), amount.getFractionalDigits(),
                amount.getAmount());
    }

    @Override
    public BatchHandResponse batchHand(BatchHandRequest request) throws BatchHandFailedException {
        try {
            TransactionBuilder txBuilder = createTransactionBuilder(request);

            // Add entries.
            HashMap<Long, PlayerSessionId> sessionToPlayerSessionMap = new HashMap<Long, PlayerSessionId>();
            createHandResultEntries(request, txBuilder, sessionToPlayerSessionMap);
            createRakeEntry(request, txBuilder);

            // Perform transaction.
            TransactionRequest txRequest = txBuilder.toTransactionRequest();
            log.debug("sending tx request to wallet: {}", txRequest);
            TransactionResult txResult = walletService.doTransaction(txRequest);

            // Return result.
            String currencyCode = request.getTotalRake().getCurrencyCode();
            return createBatchHandResponse(sessionToPlayerSessionMap, txResult, currencyCode);
        } catch (UnbalancedTransactionException ute) {
            throw new BatchHandFailedException("error reporting hand result", ute);
        } catch (Exception e) {
            throw new BatchHandFailedException("error reporting hand result", e);
        }
    }

    private TransactionBuilder createTransactionBuilder(BatchHandRequest request) {
        String currencyCode = request.getTotalRake().getCurrencyCode();
        int fractionalDigits = request.getTotalRake().getFractionalDigits();
        return new TransactionBuilder(currencyCode, fractionalDigits);
    }

    private void createRakeEntry(BatchHandRequest request, TransactionBuilder txBuilder) throws BatchHandFailedException {
        //txBuilder.entry(rakeAccountId, convertToWalletMoney(request.getTotalRake()).getAmount());
        TreeMap<Integer, Money> operatorRake = new TreeMap<Integer, Money>();
        for (HandResult hr : request.getHandResults()) {
            int operatorId = hr.getOperator();
            if (operatorRake.containsKey(operatorId)) {
                operatorRake.put(operatorId, operatorRake.get(operatorId).add(hr.getRake()));
            } else {
                operatorRake.put(operatorId, hr.getRake());
            }
        }
        for (Map.Entry<Integer, Money> rakeEntry: operatorRake.entrySet()) {
            log.debug("transferring rake: operatorId = " + rakeEntry.getKey() + ", amount = " + rakeEntry.getValue());
            try {
                txBuilder.entry(accountLookupUtil.lookupOperatorAccountId(walletService, rakeEntry.getKey()), convertToWalletMoney(rakeEntry.getValue()).getAmount());
            } catch (SystemException e) {
                txBuilder.entry(getRakeAccount(rakeEntry.getValue().getCurrencyCode()), convertToWalletMoney(rakeEntry.getValue()).getAmount());
            }
        }
        txBuilder.comment("poker hand result");
        txBuilder.attribute("pokerTableId", String.valueOf((request.getTableId()).integrationId)).attribute("pokerGameId", String.valueOf(GAME_ID)).attribute(
                "pokerHandId", request.getHandId());
    }

    private long getRakeAccount(String currencyCode) {
        if (!rakeAccounts.containsKey(currencyCode)) {
            long value;
            try {
                value = accountLookupUtil.lookupRakeAccountId(walletService, currencyCode);
            } catch (SystemException e) {
                throw new RuntimeException("No rake account found for currency " + currencyCode);
            }
            rakeAccounts.put(currencyCode, value);
        }
        return rakeAccounts.get(currencyCode);
    }

    private long getPromotionsAccount(String currencyCode) {
        if (!promotionsAccounts.containsKey(currencyCode)) {
            long value;
            try {
                value = accountLookupUtil.lookupPromotionsAccountId(walletService, currencyCode);
            } catch (SystemException e) {
                throw new RuntimeException("No promotions account found for currency " + currencyCode);
            }
            promotionsAccounts.put(currencyCode, value);
        }
        return promotionsAccounts.get(currencyCode);
    }

    private void createHandResultEntries(BatchHandRequest request, TransactionBuilder txBuilder, HashMap<Long, PlayerSessionId> sessionToPlayerSessionMap) {
        // Add one entry for each hand result.
        for (HandResult hr : request.getHandResults()) {
            log.debug("recording hand result: handId = {}, sessionId = {}, bets = {}, wins = {}, rake = {}",
                    new Object[]{request.getHandId(), hr.getPlayerSession(), hr.getAggregatedBet(), hr.getWin(), hr.getRake()});

            Money resultingAmount = hr.getWin().subtract(hr.getAggregatedBet());

            Long walletSessionId = getWalletSessionIdByPlayerSessionId(hr.getPlayerSession());
            sessionToPlayerSessionMap.put(walletSessionId, hr.getPlayerSession());
            txBuilder.entry(walletSessionId, convertToWalletMoney(resultingAmount).getAmount());
        }
    }

    private BatchHandResponse createBatchHandResponse(HashMap<Long, PlayerSessionId> sessionToPlayerSessionMap, TransactionResult txResult, String currency) {
        List<TransactionUpdate> resultingBalances = new ArrayList<TransactionUpdate>();
        for (AccountBalanceResult sb : txResult.getBalances()) {
            if (sb.getAccountId() != getRakeAccount(currency)) {
                PlayerSessionId playerSessionId = sessionToPlayerSessionMap.get(sb.getAccountId());
                Money balance = convertFromWalletMoney(sb.getBalance());
                BalanceUpdate balanceUpdate = new BalanceUpdate(playerSessionId, balance, nextId());
                resultingBalances.add(new TransactionUpdate(new TransactionId(txResult.getTransactionId()), balanceUpdate));
            }
        }
        return new BatchHandResponse(resultingBalances);
    }

    @Override
    public Money getAccountBalance(int playerId, String currency) throws GetBalanceFailedException {
        long accountId = this.accountLookupUtil.lookupAccountIdForPlayerAndCurrency(walletService, playerId, currency);
        log.debug("Found account ID {} for player {}", accountId, playerId);
        if (accountId == -1) {
            log.warn("No account found for " + playerId + " and currency " + currency + ". Returning zero money.");
            return new Money(BigDecimal.ZERO, new Currency(currency, 2));
        }
        Money money = convertFromWalletMoney(walletService.getBalance(accountId).getBalance());
        log.debug("Found balance {} for player {}", money, playerId);
        return money;
    }

    @Override
    public BalanceUpdate getSessionBalance(PlayerSessionId sessionId) throws GetBalanceFailedException {
        AccountBalanceResult sessionBalance = walletService.getBalance(getWalletSessionIdByPlayerSessionId(sessionId));
        Money balanceMoney = convertFromWalletMoney(sessionBalance.getBalance());
        return new BalanceUpdate(sessionId, balanceMoney, nextId());
    }

    @Override
    public void transfer(TransferMoneyRequest request) {
        TransactionBuilder txBuilder = new TransactionBuilder(request.amount.getCurrencyCode(), request.amount.getFractionalDigits());
        txBuilder.entry(getWalletSessionIdByPlayerSessionId(request.fromSession), convertToWalletMoney(request.amount.negate()).getAmount());
        txBuilder.entry(getWalletSessionIdByPlayerSessionId(request.toSession), convertToWalletMoney(request.amount).getAmount());
        txBuilder.toTransactionRequest();
        txBuilder.comment(request.comment);
        TransactionRequest txRequest = txBuilder.toTransactionRequest();
        log.debug("sending tx request to wallet: {}", txRequest);
        TransactionResult txResult = walletService.doTransaction(txRequest);
        log.debug("Result: " + txResult);
    }

    @Override
    public Currency getCurrency(String currencyCode) {
        com.cubeia.backoffice.wallet.api.dto.Currency currency = walletService.getCurrency(currencyCode);
        return new Currency(currency.getCode(),currency.getFractionalDigits());
    }

    @Override
    public void transferMoneyFromPromotionsAccount(PlayerSessionId toAccount, Money money, String comment) {
        TransactionBuilder txBuilder = new TransactionBuilder(money.getCurrencyCode(), money.getFractionalDigits());
        txBuilder.entry(getPromotionsAccount(money.getCurrencyCode()), convertToWalletMoney(money.negate()).getAmount());
        txBuilder.entry(getWalletSessionIdByPlayerSessionId(toAccount), convertToWalletMoney(money).getAmount());
        txBuilder.comment(comment);
        TransactionRequest txRequest = txBuilder.toTransactionRequest();
        log.debug("sending tx request to wallet: {}", txRequest);
        TransactionResult txResult = walletService.doTransaction(txRequest);
        log.debug("Result: " + txResult);
    }

    @Override
    public void transferMoneyToRakeAccount(PlayerSessionId fromAccount, Money money, String comment) {
        TransactionBuilder txBuilder = new TransactionBuilder(money.getCurrencyCode(), money.getFractionalDigits());
        txBuilder.entry(getWalletSessionIdByPlayerSessionId(fromAccount), convertToWalletMoney(money.negate()).getAmount());
        txBuilder.entry(getRakeAccount(money.getCurrencyCode()), convertToWalletMoney(money).getAmount());
        txBuilder.comment(comment);
        TransactionRequest txRequest = txBuilder.toTransactionRequest();
        log.debug("sending tx request to wallet: {}", txRequest);
        TransactionResult txResult = walletService.doTransaction(txRequest);
        log.debug("Result: " + txResult);
    }

    @Override
    public void transferMoneyFromRakeAccount(PlayerSessionId fromAccount, Money money, String comment) {
        TransactionBuilder txBuilder = new TransactionBuilder(money.getCurrencyCode(), money.getFractionalDigits());
        txBuilder.entry(getRakeAccount(money.getCurrencyCode()), convertToWalletMoney(money.negate()).getAmount());
        txBuilder.entry(getWalletSessionIdByPlayerSessionId(fromAccount), convertToWalletMoney(money).getAmount());
        txBuilder.comment(comment);
        TransactionRequest txRequest = txBuilder.toTransactionRequest();
        log.debug("sending tx request to wallet: {}", txRequest);
        TransactionResult txResult = walletService.doTransaction(txRequest);
        log.debug("Result: " + txResult);
    }
}
