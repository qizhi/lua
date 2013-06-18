/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.server.processor.filter;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.TableInterceptorProvider;
import com.cubeia.firebase.api.game.TableListenerProvider;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.game.GameConfig;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyAttributeAccessor;
import com.cubeia.firebase.server.processor.Notifier;
import com.cubeia.firebase.server.processor.NotifierResource;
import com.cubeia.firebase.server.processor.TableActionScheduler;
import com.cubeia.firebase.server.processor.TournamentNotifierImpl;
import com.cubeia.firebase.server.routing.GameNodeRouter;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * This filter sets up the Firebase table listeners, notifiers and scheduler. It
 * acceps tables only as data.  
 * 
 * @author Lars J. Nilsson
 */
// TODO This is a heavy-lifting filter, should possibly be broken up
public class FirebaseTableSetupFilter<T extends FirebaseTable, A extends Action> extends AbstractGameAccessFilter<T, A> {

	private final SystemStateServiceContract state;
	private final GameNodeRouter nodeRouter;
	private final GameConfig config;
	private final CoreTransactionManager transactions;
	private final TableActionScheduler scheduler;
	
	public FirebaseTableSetupFilter(FirebaseTableSetupContext context) {
		super(context.getGameRegistry());
		this.transactions = context.getServiceRegistry().getServiceInstance(CoreTransactionManager.class);
		this.state = context.getServiceRegistry().getServiceInstance(SystemStateServiceContract.class);
		this.nodeRouter = context.getNodeRouter();
		this.config = context.getGameConfig();
		this.scheduler = context.getTableScheduler();
	}
	
	@Override
	public void process(A action, T table, ProcessorChain<T, A> filters) {
		Game game = getGameForTable(table);
		if(game != null) {
			createAndAttachNotifier(table);
			checkGameImplementations(table, game);
			attachTableScheduler(table);
			createAndAttachMttNotifier(table);
			try {
				filters.next(action, table);
			} finally {
				cleanup(table);
			}
		} else {
			log.warn("Dropping action '" + action + "' as game was not found.");
		}
	}

	
	

	// --- PRIVATE METHODS --- //
	
    private void cleanup(T table) {
    	table.setNotifier(null);
    	table.setListener(null);
    	table.setInterceptor(null);
    	table.setInternalScheduler(null);
    	table.setTableAccessor(null);
	}
	
	private void createAndAttachMttNotifier(T table) {
		// FIXME: the tournament notifier doesn't belong to a general table, move it to some context dependent object.
		TournamentNotifier mttNotifier = new TournamentNotifierImpl(nodeRouter.getMttSender());
	    table.setTournamentNotifier(mttNotifier);
	}

	private void attachTableScheduler(T table) {
		table.setInternalScheduler(scheduler);
	}
	
	private void createAndAttachNotifier(T table) {
		Notifier notifier = new Notifier(table, nodeRouter.getClientEventSender(), config.getUseNotifierCommit());
    	checkTransaction(notifier);
    	table.setNotifier(notifier);
	}
	
    private void checkTransaction(Notifier notifier) {
		CoreTransaction trans = transactions.currentTransaction();
		if(trans != null) {
			trans.attach(new NotifierResource(notifier));
		}
    }
	
	private void checkGameImplementations(FirebaseTable table, Game game) {
		// Check for table listener before executing anything
    	checkTableListener(table, game);
    	// Check table lobby accessor
    	checkTableLobbyAttributes(game, table);
		// Check for table interceptor
    	checkTableInterceptor(table, game);
    	// Check if the game provides extended details
    	checkExtendedDetailsProvider(table, game);    	
	}
	
    /*
     * Set the lobby accessor on the table.
     */
	private void checkTableLobbyAttributes(Game game, FirebaseTable table) {
		DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(state, table.getMetaData().getLobbyPath());
		table.setTableAccessor(acc);
	}
	
	/*
     * Check if the game implements table listener provider,
     * and if so set the listener to the table.
     */
	private void checkTableListener(Table table, Game game) {
		if (game instanceof TableListenerProvider) {
    		TableListenerProvider provider = (TableListenerProvider)game;
    		TableListener tableListener = provider.getTableListener(table);
    		((FirebaseTable)table).setListener(tableListener);
    	} else if(game instanceof TableListener) {
    		((FirebaseTable)table).setListener((TableListener)game);
    	}
	}
	
	/*
     * Check if the game implements table interceptor provider,
     * and if so set the interceptor to the table.
     */
	private void checkTableInterceptor(Table table, Game game) {
		if (game instanceof TableInterceptorProvider) {
			TableInterceptorProvider provider = (TableInterceptorProvider)game;
			TableInterceptor interceptor = provider.getTableInterceptor(table);
			((FirebaseTable)table).setInterceptor(interceptor);
    	} else if(game instanceof TableInterceptor) {
    		((FirebaseTable)table).setInterceptor((TableInterceptor)game);
    	}
	}
    
	/*
	 * Sets a detail provider on the table if the game implements ExtendedDetailsProvider.
	 */ 
	private void checkExtendedDetailsProvider(FirebaseTable table, Game game) {
		if (game instanceof ExtendedDetailsProvider) {
			ExtendedDetailsProvider provider = (ExtendedDetailsProvider)game;
			((FirebaseTable)table).setExtendedDetailsProvider(provider);
    	}		
	}	
}
