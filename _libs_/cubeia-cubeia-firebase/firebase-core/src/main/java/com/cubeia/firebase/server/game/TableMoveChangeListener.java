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
package com.cubeia.firebase.server.game;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.commands.ObjectEmerged;
import com.cubeia.firebase.server.processor.TableActionScheduler;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.messagebus.util.ChannelChangeListener;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This class have been refactored out from the game event daemon in order to be
 * re-used. It listens to the mbus in order to correct re-schedule events from moved 
 * tables.
 * 
 * @author Larsan
 */
class TableMoveChangeListener implements ChannelChangeListener, CommandListener {
	
	private final JmxExecutor tableMover;
	private final TableActionScheduler actionScheduler;
	private final GameObjectSpace<FirebaseTable, GameAction> tableSpace;
	private final MBusContract mbus;
	private final Partition p;
	
	private final Logger log = Logger.getLogger(getClass());
	
	TableMoveChangeListener(JmxExecutor tableMover, TableActionScheduler actionScheduler, GameObjectSpace<FirebaseTable, GameAction> tableSpace, Partition p, MBusContract mbus) {
		this.tableMover = tableMover;
		this.actionScheduler = actionScheduler;
		this.tableSpace = tableSpace;
		this.mbus = mbus;
		this.p = p;
	}
	
	public Object commandReceived(CommandMessage c) {
		if(c.command instanceof ObjectEmerged) {
			PartitionMap map = mbus.getCurrentPartitionMap();
			for (int id : ((ObjectEmerged)c.command).getAttachment()) {
				Partition p = map.getPartitionForChannel(EventType.GAME, id);
				if(this.p.equals(p)) {
					final int tableId = id;
					log.debug("Scheduling check for table data on emerged object: " + tableId);
					tableMover.submit(new SafeRunnable() {
					
						@Override
						protected void innerRun() {
							FirebaseTable table = tableSpace.peek(tableId);
							if(table == null) {
								// table = retryPeek(ch.getId());
								Logger.getLogger(getClass()).warn("Failed to inspect object " + tableId);
							} else {
								TableScheduler sh = table.getScheduler();
								for (UUID id : sh.getAllScheduledGameActions()) {
									long delay = sh.getScheduledGameActionDelay(id);
									GameAction action = (GameAction)sh.getScheduledGameAction(id);
									if (log.isDebugEnabled()) {
										log.debug("Table["+tableId+"] Emerged table rescheduling action: "+action);
									}
									actionScheduler.scheduleLoopback(action, delay, table, id);
								}
							}
						}
					});
				}
			}
		}
		return null;
	}

	public void addition(final Channel[] channels, boolean knownMove) {
		/*
		 * Return here for ticket [ #312 ], if this is an addition we 
		 * shouldn't need to check for event processing at all
		 */
		if(!knownMove) return; // SANITY CHECK
		/*
		 * For additions we need to loop through the tables, check all
		 * UUIDs for the table and optionally re-schedule them. /LJN
		 */
		
		tableMover.submit(new SafeRunnable() {
			@Override
			protected void innerRun() {
				
				for (Channel ch : channels) {
					FirebaseTable table = tableSpace.peek(ch.getId());
					if (log.isDebugEnabled()) {
						log.debug("Table["+ch.getId()+"] Addition reschedule. Table object: "+table);
					}
					if(table == null) {
						// table = retryPeek(ch.getId());
						Logger.getLogger(getClass()).warn("Failed to inspect object " + ch.getId());
						continue;
					}
					TableScheduler sh = table.getScheduler();
					for (UUID id : sh.getAllScheduledGameActions()) {
						long delay = sh.getScheduledGameActionDelay(id);
						GameAction action = (GameAction)sh.getScheduledGameAction(id);
						if (log.isDebugEnabled()) {
							log.debug("Table["+ch.getId()+"]         Addition reschedule action:"+action);
						}
						actionScheduler.scheduleLoopback(action, delay, table, id);
					}
				}
			}
		}); 
				
	}
	
	public void removal(final Channel[] channels, boolean knownMove) {
		// if(state == State.STOPPED) return; // SANITY CHECK
		/*
		 * For removal, all we have to do is to remove the action from
		 * the scheduler. We do not have to remove it from the table, as this may
		 * be a move, it will be picked up somewhere else, and if it is a deletion
		 * it will be GC'ed. /LJN
		 */
		tableMover.submit(new SafeRunnable() {
            public void innerRun() {
                for (Channel ch : channels) {
                    actionScheduler.cancelAllScheduledLoopbacks(ch.getId());
                }
            }
		});
	}
	
	// --- INNER CLASSES --- //
	
    /*
     * This runnable calls the object space for each channel, passing
     * in the table processor as an argument.
     */
    /*private class ChannelProcessor implements Runnable {
    	
    	private final Channel[] channels;
    	private final Processor<FirebaseTable> processor;
    	
    	private ChannelProcessor(Channel[] chans, Processor<FirebaseTable> proc) {
    		this.channels = chans;
    		this.processor = proc;
    	}
    	
    	public void run() {
    		for (Channel ch : channels) {
    			// if(state == State.STOPPED) break; // SANITY CHECK
    			try {
    				tableSpace.handle(ch.getId(), processor);
    			} catch(Throwable e) {
    				// Only log if we're still running...
    				// if(state != State.STOPPED) {
    				log.error("Failed to process table '" + ch.getId() + "'", e);
    				// }
     			}
    		}
    	}
    }*/
}