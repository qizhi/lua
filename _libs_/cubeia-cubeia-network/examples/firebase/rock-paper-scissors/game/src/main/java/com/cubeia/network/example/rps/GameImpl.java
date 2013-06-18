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

package com.cubeia.network.example.rps;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;

public class GameImpl implements Game, TableListener {
	public final static int OPERATOR_ID = 0;
	public final static int GAME_ID = 1337;

	/**
	 * The amount to bet
	 */
	public final static BigDecimal BET = new BigDecimal("1.00");

	/**
	 * The fee that will be taken from the winner of the hand and credited the operator
	 */
	public final static BigDecimal FEE = new BigDecimal("0.10");

	/**
	 * Currency code
	 */
	public final static String CURRENCY = "EUR";

	/**
	 * Number of fractional digits in the currency
	 */
	public final static int FRACTIONAL_DIGITS = 2;

	private Logger log = LoggerFactory.getLogger(getClass());

	private WalletServiceContract walletService;

	public void init(GameContext ctx) {
		walletService = ctx.getServices().getServiceInstance(WalletServiceContract.class);
	}

	public GameProcessor getGameProcessor() {
		return new Processor(this);
	}

	public void destroy() {
	}

	@Override
	public void playerJoined(Table table, GenericPlayer player) {
		log.info("player joined, table: {}, player: {}", table.getId(), player.getName());
		withdrawBet(table, player);
	}

	/**
	 * Start session and withdraw bet from wallet.
	 *
	 * @param table  table
	 * @param player player
	 */
	private void withdrawBet(Table table, GenericPlayer player) {
		// open a new session account
		Long sessionId = walletService.startSession(CURRENCY, OPERATOR_ID, player.getPlayerId(), String.valueOf(table.getId()), GAME_ID, player.getName(), "");
		log.info("opened new session {} for user {}", sessionId, player.getPlayerId());

		// withdraw (move) money from the remote wallet to the session account
		BigDecimal amount = BET;
		walletService.withdraw(new Money(CURRENCY, FRACTIONAL_DIGITS, amount), OPERATOR_ID, sessionId, "joined table");
		log.info("credited {} to session {} for user {}", new Object[]{amount, sessionId, player.getPlayerId()});

		// store the session account id in the game state
		getState(table).setSessionForPlayer(player.getPlayerId(), sessionId);
	}

	@Override
	public void playerLeft(Table table, int playerId) {
		log.info("player left, table: {}, player id: {}", table.getId(), playerId);
	}

	@Override
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {

	}

	@Override
	public void watcherJoined(Table table, int playerId) {
	}

	@Override
	public void watcherLeft(Table table, int playerId) {
	}

	@Override
	public void seatReserved(Table table, GenericPlayer player) {
	}

	public static State getState(Table table) {
		return (State) table.getGameState().getState();
	}

	public static GameDataAction createGameDataAction(int playerId, int tableId, String message) {
		GameDataAction gda = new GameDataAction(playerId, tableId);
		gda.setData(ByteBuffer.wrap(message.getBytes()));
		return gda;
	}

	public WalletServiceContract getWalletService() {
		return walletService;
	}

}
