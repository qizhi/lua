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

import static com.cubeia.network.example.rps.GameImpl.BET;
import static com.cubeia.network.example.rps.GameImpl.CURRENCY;
import static com.cubeia.network.example.rps.GameImpl.FEE;
import static com.cubeia.network.example.rps.GameImpl.FRACTIONAL_DIGITS;
import static com.cubeia.network.example.rps.GameImpl.OPERATOR_ID;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import com.cubeia.network.wallet.firebase.domain.TransactionBuilder;

public class Processor implements GameProcessor {
    private Logger log = LoggerFactory.getLogger(getClass());

	private final GameImpl game;

    private Long feeAccountId;
	
	public Processor(GameImpl game) {
		this.game = game;
	}
	
	public void handle(GameDataAction action, Table table) { 
		String message = new String(action.getData().array());
	    log.info("Table " + table.getId() + " incoming message: " + message);
	    
	    int playerId = action.getPlayerId();
	    State gameState = GameImpl.getState(table);
	    
	    if (message.startsWith("play ")) {
	    	handlePlayCommand(table, message, playerId, gameState);
	    }
	    
	    playIfBothPlayersAreReady(table, gameState);
	    
	}

	private void handlePlayCommand(Table table, String message, int playerId, State gameState) {
		try {
			String tokenStr = message.split(" ")[1];
			PlayToken token = PlayToken.valueOf(tokenStr);
		
			if (token == null) {
			} else {
				gameState.setToken(playerId, token);
				log.info("{} played {}", playerId, token);
			}
		} catch (Exception e) {
			log.error("bad play token for input command: {}", message);
			GameDataAction a = new GameDataAction(playerId, table.getId());
			a.setData(ByteBuffer.wrap(("ERROR: Unknown token/command. Must be one of: " + Arrays.toString(PlayToken.values())).getBytes()));
			table.getNotifier().notifyPlayer(playerId, a);
		}
	}

	private void playIfBothPlayersAreReady(Table table, State gameState) {
		if (gameState.getPlayerTokenMap().size() == 2) {
	    	log.info("both players made their move");
	    	
	    	// get both player's tokens and sort out the winner (if not a draw)
	    	Iterator<Entry<Integer, PlayToken>> entryIter = gameState.getPlayerTokenMap().entrySet().iterator();
	    	Entry<Integer, PlayToken> playerOneEntry = entryIter.next();
	    	Entry<Integer, PlayToken> playerTwoEntry = entryIter.next();

	    	
	    	Integer winnerPlayerId = null;
	    	Integer loserPlayerId = null;
	    	String result = null;
	    	
	    	if (playerOneEntry.getValue().winsOver(playerTwoEntry.getValue()) == 0) {
	    	    // draw
	    		log.info("no winner, both played: {}", playerOneEntry.getValue());
	    		result = "Draw, both played " + playerOneEntry.getValue();
	    	} else if (playerOneEntry.getValue().winsOver(playerTwoEntry.getValue()) > 0) {
                // player one wins
	    		winnerPlayerId = playerOneEntry.getKey();
	    		loserPlayerId = playerTwoEntry.getKey();
	    		result = "Player "  +  table.getPlayerSet().getPlayer(winnerPlayerId).getName() + " won. " + 
	    			playerOneEntry.getValue() + " beats " + playerTwoEntry.getValue();
	    		
	    	} else if (playerOneEntry.getValue().winsOver(playerTwoEntry.getValue()) < 0) {
	    	    // player two wins
	    		winnerPlayerId = playerTwoEntry.getKey();
                loserPlayerId = playerOneEntry.getKey();
	    		result = "Player "  +  table.getPlayerSet().getPlayer(winnerPlayerId).getName() + " won. " + 
	    			playerTwoEntry.getValue() + " beats " + playerOneEntry.getValue();
	    	}
	    	
	    	log.info(result);
	    	table.getNotifier().notifyAllPlayers(GameImpl.createGameDataAction(-1, table.getId(), result));
	    	
	    	
	    	State state = GameImpl.getState(table);
	    	
	    	WalletServiceContract walletService = game.getWalletService();

	    	// update session accounts if not a draw (move money from loser to winner and to the system fee account)
            if (winnerPlayerId != null) {
                TransactionBuilder txBuilder = new TransactionBuilder(CURRENCY, FRACTIONAL_DIGITS);
                txBuilder.comment("game result");
                txBuilder.entry(state.getSessionForPlayer(winnerPlayerId), BET);
                txBuilder.entry(state.getSessionForPlayer(winnerPlayerId), FEE.negate());
                txBuilder.entry(state.getSessionForPlayer(loserPlayerId), BET.negate());
                txBuilder.entry(getFeeAccountId(), FEE);
                walletService.doTransaction(txBuilder.toTransactionRequest());
            } 
            
            // returns funds to wallet and close session accounts
            walletService.endSessionAndDepositAll(OPERATOR_ID, state.getSessionForPlayer(winnerPlayerId), "game ended");
            walletService.endSessionAndDepositAll(OPERATOR_ID, state.getSessionForPlayer(loserPlayerId), "game ended");
	    	
            // remove players from the table
	    	for (GenericPlayer p : table.getPlayerSet().getPlayers()) {
	    		table.getPlayerSet().removePlayer(p.getPlayerId());
	    	}
	    	
	    	// mark the table as finished and let the activator purge it
            table.getAttributeAccessor().setStringAttribute("FINISHED", "");
	    }
	}

	private long getFeeAccountId() {
	    if (feeAccountId == null) {
            feeAccountId = getFeeAccount().getId();
	    }
        return feeAccountId;
    }

    private Account getFeeAccount() {
        ListAccountsRequest lar = new ListAccountsRequest();
	    lar.setUserId((long) OPERATOR_ID);
	    lar.setTypes(Collections.singletonList(AccountType.OPERATOR_ACCOUNT));
	    lar.setLimit(1);
        AccountQueryResult accountsResult = game.getWalletService().listAccounts(lar);
        if (accountsResult.getAccounts().isEmpty()) {
            throw new IllegalStateException("error getting fee account, please create an account of type " + AccountType.OPERATOR_ACCOUNT + 
                " with user id " + OPERATOR_ID);
        } else if (accountsResult.getTotalQueryResultSize() > 1) {
            log.warn("got more than one fee account candidate");
        }
        
        Account account = accountsResult.getAccounts().iterator().next();
        log.debug("using fee account: {}", account);
        return account;
    }

    public void handle(GameObjectAction action, Table table) { 
	}

}
