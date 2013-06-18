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
/**
 * 
 */
package com.cubeia.firebase.server.mtt.event;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.commands.ObjectEmerged;
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

class MttChangeHandler implements ChannelChangeListener, CommandListener {
	
	private final JmxExecutor mttMover; 
	private final MttActionScheduler actionScheduler;
	private final GameObjectSpace<TransactionalMttState, MttAction> space;
	private final MBusContract mbus;
	private final Partition p;
	
	private final Logger log = Logger.getLogger(getClass());
	
	MttChangeHandler(GameObjectSpace<TransactionalMttState, MttAction> space, JmxExecutor mttMover, MttActionScheduler actionScheduler, Partition p, MBusContract mbus) {
		this.space = space;
		this.mttMover = mttMover;
		this.actionScheduler = actionScheduler;
		this.mbus = mbus;
		this.p = p;
	}
	
	public Object commandReceived(CommandMessage c) {
		if(c.command instanceof ObjectEmerged) {
			PartitionMap map = mbus.getCurrentPartitionMap();
			for (int id : ((ObjectEmerged)c.command).getAttachment()) {
				Partition p = map.getPartitionForChannel(EventType.MTT, id);
				if(this.p.equals(p)) {
					final int mttId = id;
					log.debug("Scheduling check for mtt data on object emerged: " + mttId);
					mttMover.submit(new SafeRunnable() {
					
						@Override
						protected void innerRun() {
							TransactionalMttState mtt = space.peek(mttId);
							if(mtt == null) {
								// table = retryPeek(ch.getId());
								Logger.getLogger(getClass()).warn("Failed to inspect object " + mttId);
							} else {
								Scheduler<MttAction> sh = mtt.getScheduler();
								for (UUID id : sh.getAllScheduledGameActions()) {
									long delay = sh.getScheduledGameActionDelay(id);
									MttAction action = (MttAction)sh.getScheduledGameAction(id);
									actionScheduler.scheduleLoopback(action, delay, id);
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
		// if(state == State.STOPPED || !knownMove) return; // SANITY CHECK
		if(!knownMove) return; // SANITY CHECK
		
		/*
		 * For additions we need to loop through the MTT, check all
		 * UUIDs for the MTT and optionally re-schedule them. /LJN
		 */
		mttMover.submit(new SafeRunnable() {
		
			@Override
			protected void innerRun() {
				
				for (Channel ch : channels) {
					TransactionalMttState mtt = space.peek(ch.getId());
					if(mtt == null) {
						// table = retryPeek(ch.getId());
						Logger.getLogger(getClass()).warn("Failed to inspect object " + ch.getId());
						continue;
					}
					Scheduler<MttAction> sh = mtt.getScheduler();
					for (UUID id : sh.getAllScheduledGameActions()) {
						long delay = sh.getScheduledGameActionDelay(id);
						MttAction action = (MttAction)sh.getScheduledGameAction(id);
						actionScheduler.scheduleLoopback(action, delay, id);
					}
				}
			}
		});
	}
	
	public void removal(final Channel[] channels, boolean knownMove) {
		// if(state == State.STOPPED) return; // SANITY CHECK
		/*
		 * For removal, all we have to do is to remove the action from
		 * the scheduler. We do not have to remove it from the MTT, as this may
		 * be a move, it will be picked up somewhere else, and if it is a deletion
		 * it will be GC'ed. /LJN
		 */
		mttMover.submit(new SafeRunnable() {
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
    	private final Processor<TransactionalMttState> processor;
    	
    	private ChannelProcessor(Channel[] chans, Processor<TransactionalMttState> proc) {
    		this.channels = chans;
    		this.processor = proc;
    	}
    	
    	public void run() {
    		for (Channel ch : channels) {
    			if(state == State.STOPPED) break; // SANITY CHECK
    			try {
    				space.handle(ch.getId(), processor);
    			} catch(Exception e) {
    				// Only log if we're still running...
    				if(state != State.STOPPED) {
    					log.error("Failed to process table '" + ch.getId() + "'", e);
    				}
     			}
    		}
    	}
    }*/
}