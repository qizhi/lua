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
package com.cubeia.space.handler.table;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.TimeCounter;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.GameObjectSizeRecorder;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.processor.GameObjectProcessor;
import com.cubeia.firebase.server.processor.TableActionProcessor;
import com.cubeia.firebase.server.statistics.Level;
import com.cubeia.firebase.server.statistics.StatisticsLevel;
import com.cubeia.firebase.service.space.ExtendedGameObjectSpace;
import com.cubeia.firebase.service.space.SpaceObjectNotFoundException;
import com.cubeia.space.ExtendedSpace;
import com.cubeia.space.Space;
import com.cubeia.space.handler.AbstractTXHandler;
import com.cubeia.space.service.CommitTimeRecorder;

/**
 * A generic transactional space/action handler for tables.
 * This handler relies on the transactional capabilities of the
 * underlying space implementation.
 *  
 * 
 * Created on 2006-okt-04
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public class TableHandler extends AbstractTXHandler<FirebaseTable, GameAction> implements TableHandlerMBean, ExtendedGameObjectSpace<FirebaseTable, GameAction> {

	private final Logger log = Logger.getLogger(TableHandler.class);
    
    private final TableFactory<FirebaseTable> tableFactory;
    
    private Space<TableData> space;

	private String jmxName = "com.cubeia.firebase.cache:type=TableHandler";

	private final TimeCounter commitTimer = new TimeCounter(1000);
	private final TimeCounter sizeCounter = new TimeCounter(1000);

    @SuppressWarnings("rawtypes")
	public TableHandler(Space<TableData> space, ServiceRegistry reg, TableFactory<FirebaseTable> tableFactory) {
    	super(reg);
    	this.tableFactory = tableFactory;
    	this.space = space;
    	bindToJMX();
    	/*
    	 * Ticket #644: Somewhat of a hack here, we happen to know the space
    	 * is an extended space...
    	 */
    	if(space instanceof ExtendedSpace) {
    		((ExtendedSpace)space).setCommitTimeRecorder(new CommitTimeRecorder() {
				
				@Override
				public void recordCommitTime(long millis) {
					commitTimer.register(millis);
				}
			});
    	}
	}

	public void unregisterClassloader(int gameId) {
    	tableFactory.setGameClassLoader(gameId, null);
    }
    
    public void registerClassloader(int gameId, ClassLoader loader) {
    	tableFactory.setGameClassLoader(gameId, loader);
    }
    
    public void halt() {
    	space.halt();
    }
    
    public boolean isHalted() {
    	return space.isHalted();
    }
    
    public void resume() {
    	space.resume();
    }
    
    public void start() {
		space.start();
		/*if (!space.isFailOverEnabled()) {
			log.info("The underlying Space is not configured for replication fail over.");
		}*/
		super.state = State.STARTED;
	}

	public void stop() {
		space.stop();
		super.state = State.STOPPED;
		unbindFromJMX();
	}
    
    
    /**
     * Handle GameActions in a transaction context.
     * If the handler fails, the transaction will be rolled back 
     * and we will return false (not handled).
     * 
     * @param action
     * @return true if successful
     */
    public boolean handle(Event<GameAction> event, GameObjectProcessor<GameAction> gop) throws SpaceObjectNotFoundException {
    	/*
    	 * This is the transaction order:
    	 * 
    	 *  1) Lock space
    	 *  2) Take table data
    	 *  3) Create FB table
    	 *  4) Start JBC transaction
    	 *  5) Create notifier
    	 *  
    	 * [local actions]
    	 *  * User transaction
    	 *  * Process action
    	 *  * Commit user transaction
    	 * [/local actions]
    	 *  
    	 * <tx commit />
    	 * 
    	 *  5) Flush notifier
    	 *  4) Commit JBC transaction
    	 *  3) Flush FB table
    	 *  2) Put table data
    	 *  1) Unlock space
    	 */
        if(log.isTraceEnabled()) {
        	log.trace("Handle event: " + event);
        }
    	TableActionProcessor<GameAction> proc = checkGameProcessor(gop);
    	int tableId = event.getFirstTargetId();
    	TableData tableData = space.take(tableId); // 1 & 2 (see above)
        if (tableData == null) {
        	if(event.isTransient()) {
        		/* 
        		 * Trac issue [ #209 ]
        		 * 
        		 * This is a transient action. It is most probably a
        		 * scheduled event, and between the scheduling and and running
        		 * the table has been removed, which we now ignores.
        		 */
        		if(log.isDebugEnabled()) {
        			log.debug("Dropping transient event: " + event);
        		}
        	} else {
        		throw new SpaceObjectNotFoundException(tableId);
        	}
        } else {
        	FirebaseTable table = createTable(tableData); // 3 (see above)
        	GameAction action = unwrapEvent(event, tableData);
        	wrapAndDispatch(proc, table, action); // 4 & 5 (see above)
        }
        return true;
    }
    
    public boolean remove(int id) {
    	return space.remove(id);
    }
   	
    public void add(FirebaseTable[] objects) {
    	if(space instanceof ExtendedSpace<?>) {
    		TableData[] data = toDataArray(objects);
    		((ExtendedSpace<TableData>)space).add(data);
    	} else {
    		for (FirebaseTable t : objects) {
    			add(t);
    		}
    	}
    }

	public void remove(int[] objects) {
    	if(space instanceof ExtendedSpace<?>) {
    		((ExtendedSpace<?>)space).remove(objects);
    	} else {
    		for (int id : objects) {
    			remove(id);
    		}
    	}
    }
	
	public FirebaseTable add(FirebaseTable table) {
		Arguments.notNull(table, "table");
		TableData data = tableFactory.extractData(table);
		space.add(data);
		return table;
	}

	public boolean exists(int objectid) {
		return space.exists(objectid);
	}

	public FirebaseTable peek(int id) {
		TableData data = space.peek(id);
		if (data != null) {
			return createTable(data);
		} else {
			return null;
		}
	}

	protected boolean isJtaEnabled() {
		return space.isJtaEnabled();
	}	
	
	
	
	/*------------------------------------------------
	 
 		JMX
  
 	 ------------------------------------------------*/
	
	public double getAverageCommitTimeMicros() {
		if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
			return commitTimer.calculate();
		} else {
			return -1;
		}
	}
	
	public double getAverageGameStateObjectSize() {
		if(StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
			return sizeCounter.calculate();
		} else {
			return -1;
		}
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private void wrapAndDispatch(TableActionProcessor<GameAction> proc, FirebaseTable table, GameAction action) {
		if(log.isTraceEnabled()) {
        	log.trace("Wrap/dispatching action [" + action + "] to table " + table.getId());
        }
		attachJbcTransaction();
		UserTransaction trans = getUserTransaction();
		if(trans != null) {
			wrapJtaAndDispatch(proc, table, action, trans);
		} else {
			proc.handleAction(table, action);
		}
	}

	private void wrapJtaAndDispatch(TableActionProcessor<GameAction> proc, FirebaseTable table, GameAction action, UserTransaction trans) {
		if(log.isTraceEnabled()) {
        	log.trace("Entering JTA wrap/dispatch for action [" + action + "] to table " + table.getId());
        }
		boolean done = false;
		try {
			trans.begin();
			proc.handleAction(table, action);
			trans.commit();
			done = true;
		} catch (NotSupportedException e) {
			failTransaction(e);
		} catch (SystemException e) {
			failTransaction(e);
		} catch (SecurityException e) {
			failTransaction(e);
		} catch (IllegalStateException e) {
			failTransaction(e);
		} catch (RollbackException e) {
			failTransaction(e);
		} catch (HeuristicMixedException e) {
			failTransaction(e);
		} catch (HeuristicRollbackException e) {
			failTransaction(e);
		} finally {
			if(log.isTraceEnabled()) {
	        	log.trace("Exiting JTA wrpa/dispatch for action [" + action + "] to table " + table.getId() + "; done: " + done);
	        }
			if(!done) {
				try {
					trans.rollback();
				} catch (Exception e) {
					log.fatal("Failed to rollback transaction!", e);
				}
			}
		}
	}

	private void failTransaction(Exception e) {
		throw new IllegalStateException("Failed user transaction", e);
	}
	
    private void bindToJMX() {
        try{
        	if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
	            MBeanServer mbs = getMBeanServer();
	            ObjectName monitorName = new ObjectName(jmxName);
	            mbs.registerMBean(this, monitorName);
        	}
        }catch(Exception ex) {
            log.error("Could not bind Table Handler to the JMX Server", ex);
        }
    }
    
    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
    
	private FirebaseTable createTable(TableData data) {
		FirebaseTable t = tableFactory.createTable(data);
		t.setGameObjectSizeRecorder(new GameObjectSizeRecorder() {
			
			@Override
			public void recordGameObjectSize(long bytes) {
				sizeCounter.register(bytes);
			}
		});
		return t;
	}

    private TableData[] toDataArray(FirebaseTable[] objects) {
    	int i = 0;
    	TableData[] arr = new TableData[objects.length];
    	for (FirebaseTable t : objects) {
    		arr[i++] = tableFactory.extractData(t);
    	}
		return arr;
	}
    
	private GameAction unwrapEvent(Event<GameAction> event, TableData tableData) {
		int gameId = getGameId(tableData);
		ClassLoader loader = tableFactory.getGameClassLoader(gameId);
		try {
			event.unwrapForTarget(loader);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to deserialize event for target", e);
		}
		return event.getAction();
	}

	private int getGameId(TableData tableData) {
		return tableData.getMetaData().getGameId();
	}

	private TableActionProcessor<GameAction> checkGameProcessor(GameObjectProcessor<GameAction> gop) {
		TableActionProcessor<GameAction> proc;
		if (gop instanceof TableActionProcessor<?>) {
			proc = (TableActionProcessor<GameAction>) gop;
		} else {
			throw new RuntimeException("Wrong processor type found (expected TableActionProcessor): "+gop.getClass());
		}
		return proc;
	}
    
	private void unbindFromJMX() {
        try{
            MBeanServer mbs = getMBeanServer();
            ObjectName monitorName = new ObjectName(jmxName);
            if(mbs.isRegistered(monitorName)) {
            	mbs.unregisterMBean(monitorName);
            }
        } catch(Exception ex) {
            log.error("Could not unbind Table Handler to the JMX Server", ex);
        }
    }
}

