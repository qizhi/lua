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
package com.cubeia.firebase.server.processor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.AbstractPlayerAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.ScheduledGameAction;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TableInterceptorProvider;
import com.cubeia.firebase.api.game.TableListenerProvider;
import com.cubeia.firebase.api.game.TournamentGame;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.handler.AbstractTableActionHandler;
import com.cubeia.firebase.api.game.handler.ActionHandler;
import com.cubeia.firebase.api.game.handler.DefaultGameActionHandler;
import com.cubeia.firebase.api.game.handler.MttActionHandler;
import com.cubeia.firebase.api.game.handler.MttTableActionHandler;
import com.cubeia.firebase.api.game.handler.StandardTableActionHandler;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributeMapper;
import com.cubeia.firebase.api.game.rule.DefaultSeatingRules;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.trans.StandardPlayerSet;
import com.cubeia.firebase.game.table.trans.StandardWatcherSet;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.game.GameConfig;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyAttributeAccessor;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyTableAccessor;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.util.InternalComponentInvocationHandler;
import com.cubeia.firebase.server.util.InvocationHandlerAdapter;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;


/**
 * Dispatches and handles logic from actions to Tables and Games.
 * 
 * Created on 2006-sep-28
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class TableActionProcessor<T extends GameAction> implements GameObjectProcessor<T> {
    
    private final Logger log = Logger.getLogger(TableActionProcessor.class);
    
    private final Sender<ClientEvent<?>> clientSender;
    private final Sender<MttEvent> mttSender;
	private final ActionGameRegistry creator;
    private final SystemStateServiceContract state;
    
    /** Used for scheduling actions. */
    private final TableActionScheduler tableActionScheduler;

	private final ServiceRegistry serviceRegistry;
	private final GameConfig config;

	private final int playerReconnectTimeout;
	private final int playerReservationTimeout;
	private final boolean useNotifierCommit;
	
	private JndiProvider jndiProvider;

    public TableActionProcessor(Sender<ClientEvent<?>> wrappingSender, Sender<MttEvent> wrappingSender2, ServiceRegistry serviceRegistry, ActionGameRegistry creator, TableActionScheduler scheduler, GameConfig config) {
    	this.clientSender = wrappingSender;
        this.mttSender = wrappingSender2;
		this.serviceRegistry = serviceRegistry;
		this.creator = creator;
        this.tableActionScheduler = scheduler;
		this.config = config;
		jndiProvider = serviceRegistry.getServiceInstance(JndiProvider.class);
        state = serviceRegistry.getServiceInstance(SystemStateServiceContract.class);
        // caching these values to avoid lookup every action
		this.playerReconnectTimeout = config.getPlayerReconnectTimeout();
		this.playerReservationTimeout = config.getPlayerReservationTimeout();
		this.useNotifierCommit = config.getUseNotifierCommit();
    }
    
    public void start() {
        // getTableActionScheduler().start();
    }
    
    public void stop() {
        // getTableActionScheduler().stop();
    }

	/**
     * Execute the given action on the given table
     */
    public Notifier handleAction(final FirebaseTable table, final T action) {
    	if(log.isTraceEnabled()) {
        	log.trace("Start handle action [" + action + "] on table " + table.getId());
        }
    	
    	/*
    	 * CHECK FOR OUT OF ORDER SCHEDULING
    	 */
    	if (alreadyExecuted(action, table)) return null; // EARLY RETURN
    	
    	/*
    	 * WRAP IN JNDI
    	 */
    	return (Notifier) jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
    		@Override
    		public Object invoke() throws RuntimeException {
    			return handleInJndicontext(table, action);
    		}
		});
    }

    /*
     * JNDI should be mounted in the thread context first
     */
	private Notifier handleInJndicontext(final FirebaseTable table, final T action) {
		// Create player notifier
    	Notifier notifier = new Notifier(table, clientSender, useNotifierCommit);
    	checkTransaction(notifier);
    	
    	// Set scheduler
    	table.setInternalScheduler(getTableActionScheduler());
    	
    	/*
         * GET GAME INSTANCE
         */
        final Game game = lookupGame(table);
        
        /*
    	 * SET NOTIFIER(s)
    	 */
    	table.setNotifier(notifier);
    	// FIXME: the tournament notifier doesn't belong to a general table, move it to some context dependent object.
    	// if (table.getMetaData().getType() == MULTI_TABLE_TOURNAMENT) {
    	    TournamentNotifier mttNotifier = new TournamentNotifierImpl(mttSender);
    	    table.setTournamentNotifier(mttNotifier);
    	// }
    	
    	/*
    	 * CHECK GAME INTERFACES
    	 */
    	checkGameImplementations(table, notifier, game);
    	
    	
    	/*
    	 * PROXY TABLE TO MAKE SURE PLATFORM CALLBACKS ARE
    	 * MADE WITH THE RIGHT CLASS LOADER
    	 */
    	InvocationHandler root = new InvocationHandlerAdapter(table);
    	InvocationHandler switcher = new InternalComponentInvocationHandler(getClass().getClassLoader(), root);
    	Table proxy = (Table) Proxy.newProxyInstance(Table.class.getClassLoader(), new Class[] { Table.class }, switcher);
    	
    	/*
    	 * GET ACTION HANDLERS (FOR PROXY)
    	 */
    	final List<ActionHandler> handlers = new LinkedList<ActionHandler>();
    	handlers.add(lookupTableActionHandler(proxy, proxy.getNotifier()));
    	handlers.add(lookupGameActionHandler(game, proxy, proxy.getNotifier()));
    	handlers.add(lookupMttActionHandler(game, proxy));
    	
    	/*
    	 * HANDLE ACTION WITH CLASS CONTEXT LOADER
    	 */
    	Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
    		
    		@Override
    		public Object invoke() {
    			handleAction(action, handlers, game);
    			return null;
    		}
		}, game.getClass().getClassLoader());
    	
    	
        /*
         * Store action sequence no.
         */
        storeSequence(action, table);
        
        /* 
         * UPDATE LOBBY IF NEEDED
         */
        checkLobbyUpdate(table);

        /*
    	 * UPDATE LAST MODIFIED
    	 */
    	updateLastModified(table);
    	
    	/*
    	 * CLEAN UP
    	 */
    	cleanup(game, table);
    	
    	if(log.isTraceEnabled()) {
        	log.trace("End handle action [" + action + "] on table " + table.getId());
        }
      
    	/*
    	 * RETURN NOTIFIER FOR LATE COMMIT
    	 */
    	return notifier;
	}

    private void checkTransaction(Notifier notifier) {
		CoreTransactionManager manager = serviceRegistry.getServiceInstance(CoreTransactionManager.class);
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new NotifierResource(notifier));
		}
    }

	private void checkLobbyUpdate(FirebaseTable table) {
    	/*
    	 * This method is called to update the seated/watching players
    	 * around a table. These two attributes (together with last modified, 
    	 * which is done elsewhere) are the only dynamic attributes we handle 
    	 * currently, so we're checking if they have been changed before actually
    	 * calling the update method.
    	 * 
    	 * This is a slight bit of a hack of course, but I'll leave it in for now. /LJN
    	 */
		if(isPlayerSetDirty(table) || isWatcherSetDirty(table)) {
			updateLobby(table);
		}
	}

    private boolean isWatcherSetDirty(FirebaseTable table) {
    	if (table.getWatcherSet() instanceof StandardWatcherSet) {
			StandardWatcherSet  playerSet = (StandardWatcherSet) table.getWatcherSet();
			return playerSet.getIsDirty();
        } else {
        	return true;
        }
	}
    
    private boolean isPlayerSetDirty(FirebaseTable table) {
    	if (table.getPlayerSet() instanceof StandardPlayerSet) {
			StandardPlayerSet  playerSet = (StandardPlayerSet) table.getPlayerSet();
			return playerSet.getIsDirty();
        } else {
        	return true;
        }
	}

	private void updateLobby(FirebaseTable table) {
		DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
		DefaultLobbyMutator mut = new DefaultLobbyMutator();
		mut.updateTable(acc, table);
    }
    
    
    
    /*
     * If action is of the ScheduledGameAction type, check the table to 
     * make sure the action is still scheduled, otherwise we have a fail over
     * failure or really bad luck and should ignore this execution. 
     * 
     * This method must be left, as tables are scheduling on commit there's
     * a real possibility one event cancels a scheduled action when the action
     * is already queued up for executing on the table lock, here however we 
     * should be safe to check. /LJN
     */
    private boolean alreadyExecuted(T action, FirebaseTable table) {
		if(action instanceof ScheduledGameAction) {
			UUID id = ((ScheduledGameAction)action).getIdentifier();
			return !table.getScheduler().hasScheduledGameAction(id);
		} else if(action instanceof AbstractPlayerAction) {
		    AbstractPlayerAction gameAction = (AbstractPlayerAction)action;
            return checkActionSequence(gameAction, table);
        } else {
			return false;
		}
	}

    /**
     * Verifies that the action has not yet been executed.
     * This is possible in certain fail over scenarios and we need to check this
     * to avoid double execution of events.
     * 
     * The sequence is not generated by the client and sequences between actions
     * do not need to be sequential (i.e. incremented by one) as long as they
     * are unique within a reasonable window of actions.
     * 
     * @param action, the action to check
     * @return true if this action should be discarded (already executed)
     */
	private boolean checkActionSequence(AbstractPlayerAction action, FirebaseTable table) {
        if (table.getMetaData().isDoubleExecution(action)) {
            log.warn("Discarding action due to double event execution: " + action + " Id: " + action.getActionId());
            return true;
        } else {
            return false;
        }
    }
	
	/**
	 * Stores the sequence of the action to the table's last executed action property.
	 * This is needed for fail over reasons (see checkActionSequence method).
	 * 
	 * @param action
	 * @param table
	 */
	private void storeSequence(T action, FirebaseTable table) {
		table.getMetaData().setLastExecuted(action);
		/*if (action.getSeq() != -1) {
            table.getMetaData().setLastExecuted(action.getSeq());
        }*/
    }

    /**
     * Handles the action, by letting the table handler and game handler visit it.
     * 
     * Note that before the handlers are called, we set the class loader to the game's
     * class loader. The system class loader will always be reset before returning.
     * 
     * @param action
     * @param tableActionHandler
     * @param gameActionHandler
	 * @param mttActionHandler 
     * @param game
     */
    private void handleAction(T action, List<ActionHandler> handlers, Game game) {
		for (ActionHandler handler : handlers) {
			if (handler != null) {
				if(log.isTraceEnabled()) {
		        	log.trace("Forwarding action [" + action + "] to action handler [" + handler + "]");
		        }
				action.visit(handler);
			}
		}
    }
    
    

	/**
     * Cleanup whatever needs to be cleaned up.
     * 
     * @param game
     * @param table
     */
    private void cleanup(Game game, FirebaseTable table) {
    	table.setNotifier(null);
    	table.setListener(null);
    	table.setInterceptor(null);
    	table.setInternalScheduler(null);
    	table.setTableAccessor(null);
    		
    	// Clean up Game Support specific
    	/*if (game instanceof GameSupport) {
			GameSupport support = (GameSupport) game;
			setGameContext(null, game, support);
			// Clean Lobby Locator
	    	if (support instanceof LobbyTableParticipant) {
				LobbyTableParticipant part = (LobbyTableParticipant) support;
				part.setLobbyLocator(null);
			}
		}*/
	}

	/**
     * Check what interfaces the game implements and set information
     * accordingly.
     * 
     * @param table
     * @param notifier
     * @param game
     */
	private void checkGameImplementations(final FirebaseTable table, final Notifier notifier, final Game game) {
		// WE NEED THE RIGHT CONTEXT CLASS LOADER HERE...
		Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
			@Override
			public Object invoke() throws RuntimeException {
				// Check for table listener before executing anything
		    	checkTableListener(table, game);
		    	// Check table lobby accessor
		    	checkTableLobbyAttributes(game, table);
				// Check for table interceptor
		    	checkTableInterceptor(table, game);
		    	// Check if the game provides extended details
		    	checkExtendedDetailsProvider(table, game);    
				return null;
			}
		}, game.getClass().getClassLoader());
	}


	/**
	 * Only update last modified if the table has no players.
	 * This will minimize the number of system state updates needed. 
	 * @param table
	 */
	protected void updateLastModified(FirebaseTable table) {
		if (table.getPlayerSet().getPlayerCount() == 0) {
			DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(state, table.getLobbyPath());
			DefaultTableAttributeMapper.updateLastModified(acc);
		}
	}
	
    
    
    /**
     * Set the lobby accessor on the table.
     */
	private void checkTableLobbyAttributes(Game game, FirebaseTable table) {
		DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(state, table.getMetaData().getLobbyPath());
		table.setTableAccessor(acc);
	}



	/**
     * Get the ActionHandler used for this table.
     * Currently we only provide default handler, 
     * but this could easily be extended by adding an interface
     * available to the game.
     * 
     * @param game
     * @return
     */
    protected ActionHandler lookupTableActionHandler(Table table, GameNotifier notifier) {
    	DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
		AbstractTableActionHandler handler = null;
		
		if (table.getMetaData().getType() == TableType.NORMAL) {
			handler = new StandardTableActionHandler(table, acc, new DefaultLobbyMutator());
			
		} else if (table.getMetaData().getType() == TableType.MULTI_TABLE_TOURNAMENT) {
			handler = new MttTableActionHandler(table, acc, new DefaultLobbyMutator());
			
		} else {
			log.error("Unknown Table Type encountered. Action will be ignored!. Table: "+table.getId()+" Type: "+table.getMetaData().getType());
		}
		
		if (handler != null) {
			handler.setSeatingRules(new DefaultSeatingRules());
			handler.setNotifier(notifier);
			handler.setServiceRegistry(serviceRegistry);
			checkSetTimeouts(handler);
		}
		return handler;
	}

    private void checkSetTimeouts(AbstractTableActionHandler handler) {
		if(config != null) {
			handler.setPlayerReconnectTimeout(playerReconnectTimeout);
			handler.setPlayerReservationTimeout(playerReservationTimeout);
		}
	}

	/**
     * Get the ActionHandler used for this game.
     * Currently we only provide default handler, 
     * but this could easily be extended by adding an interface
     * available to the game.
     * 
     * @param game
     * @param table
     * @param notifier
     * @return
     */
    protected ActionHandler lookupGameActionHandler(Game game, Table table, GameNotifier notifier) {
    	DefaultGameActionHandler handler = new DefaultGameActionHandler(table, game);
    	handler.setNotifier(notifier);
    	return handler;
    }
    
    /**
     * Get tournament handler if applicable otherwise null.
     * 
     * @param game
     * @return
     */
    private ActionHandler lookupMttActionHandler(Game game, Table table) {
    	if (game instanceof TournamentGame) {
			TournamentGame mttGame = (TournamentGame) game;
			return new MttActionHandler(table, mttGame);
		} else {
			return null;
		}
	}
    
	/**
     * Check if the game implements table listener provider,
     * and if so set the listener to the table.
     * 
     * @param table
     * @param game
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
	
	/**
     * Check if the game implements table interceptor provider,
     * and if so set the interceptor to the table.
     * 
     * @param table
     * @param game
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
    
	/**
	 * Sets a detail provider on the table if the game implements ExtendedDetailsProvider.
	 *  
	 * @param table
	 * @param game
	 */
	private void checkExtendedDetailsProvider(FirebaseTable table, Game game) {
		if (game instanceof ExtendedDetailsProvider) {
			ExtendedDetailsProvider provider = (ExtendedDetailsProvider)game;
			((FirebaseTable)table).setExtendedDetailsProvider(provider);
    	}		
	}	
	
    /**
     * Get Game implementation for the given table.
     * 
     * @param table
     * @return
     */
	private Game lookupGame(Table table) {
    	/*Game g = checkGameSupport(table);
    	if(g != null) return g; // SHORTCUT, HAVE GAME SUPPORT !!
    	else {*/
	    	int gameId = table.getMetaData().getGameId();
	    	try {
	    		Game g = creator.getGameInstance(gameId);
	    		if(g == null) log.error("failed to find game deployment/revision; gameId: " + gameId);
				return g;
	    	} catch (Exception e) {
	    		/*
	    		 * Ugly catch all here... /LJN
	    		 */
	    		throw new IllegalStateException("failed to instantiate game class", e);
			} 
    	//}
	}
    
    /**
     * Gets the table action scheduler for this action processor.
     * 
     * @return the {@link TableActionScheduler}
     */
    public TableActionScheduler getTableActionScheduler() {
        return tableActionScheduler;
    }
}