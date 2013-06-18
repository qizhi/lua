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

package com.cubeia.network.wallet.firebase.api;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.network.wallet.firebase.domain.ResultEntry;
import com.cubeia.network.wallet.firebase.domain.RoundResultResponse;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collection;
import java.util.Set;

/**
 * The service contract for the Cubeia generic wallet service.
 * @author w
 *
 */
public interface WalletServiceContract extends Contract {

	/**
	 * Returns an account by id.
	 * @param accountId account id
	 * @return account or null if not found
	 */
	public Account getAccountById(long accountId);

	/**
	 * Lists all accounts matching the given request.
	 * @param lar list account request
	 * @return result
	 */
	public AccountQueryResult listAccounts(ListAccountsRequest lar);

	/**
	 * Creates a new session account. This must be done before all other operations on the session.
	 *
	 * @param currencyCode currency code of the session
	 * @param licenseeId
	 * @param userId
	 * @param objectId an identifier of the object this session is related to, for example a table id or a tournament id
	 * @param gameId
	 * @param userName
	 * @param accountName
	 * @return the session id
	 */
	public Long startSession(String currencyCode, int licenseeId, int userId, String objectId, int gameId, String userName, String accountName);

	/**
	 * Close the session account. After this operation no transactions can be done on the session account.
	 * @param sessionId session id
	 * @throws AccountNotFoundException 
	 */
	public void endSession(long sessionId);

	/**
	 * Deposit money from the session account to the remote wallet. 
	 * @param amount The amount in session currency to deposit to the remote wallet, must not be more
	 *   than the session balance.
	 * @param licenseeId
	 * @param sessionId
	 * @param comment anything or null
	 */
	public void deposit(Money amount, int licenseeId, long sessionId, String comment);

	/**
	 * Deposit all funds from the session account to the remote wallet and close the session.
	 * @param licenseeId operator id
	 * @param sessionId session id
	 * @param comment transaction comment
	 * @return the amount that was deposited
	 */
	public Money endSessionAndDepositAll(int licenseeId, long sessionId, String comment);

	/**
	 * Withdraw money from the remote wallet to the session account.
	 * @param amount the amount to withdraw from the remote wallet
	 * @param licenseeId
	 * @param sessionId
	 * @param comment, anything or null
	 */
	public void withdraw(Money amount, int licenseeId, long sessionId, String comment);

	/**
	 * Returns the current balance of the given session account.
	 * @param sessionId
	 * @return the session balance
	 */
	public AccountBalanceResult getBalance(long sessionId);

	/**
	 * Handles the result of a game round by applying the given results for each player. 
	 * I.e. making a transaction for the given session accounts and the given results.
	 * @param type the type of the hand (e.g. tournament, normal, ...)
	 * @param contextId the context (e.g. game)
	 * @param subContextId the sub context (e.g. table)
	 * @param results a collection of results containing session id and the amount lost/won, the summary of
	 *   the amounts must be zero
	 * @param comment an optional textual description of the round
	 * @return the result
	 * @deprecated use {@link #doTransaction(com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest)} instead
	 */
	@Deprecated
	public RoundResultResponse roundResult(long type, long contextId, long subContextId, Collection<ResultEntry> results, String comment);

	/**
	 * Do a generic transaction consisting of the given entries.
	 * @param txRequest transaction request
	 * @return the result
	 */
	public TransactionResult doTransaction(com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest txRequest);

    /**
	 * Performs a cleanup of the system by closing all open sessions.
	 *
	 * Note that this should only be called when the system is starting up or shutting down.
	 *
	 * @param excludedAccountNames a set of account names to exclude from closing
	 */
	void closeOpenSessionAccounts(Set<String> excludedAccountNames);
	
	
	Currency getCurrency(String currencyCode);
	
}
