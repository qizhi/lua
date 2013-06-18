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

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.IdentifiableAction;
import com.cubeia.firebase.api.action.ScheduledGameAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.processing.EventFetcherStats;
import com.cubeia.firebase.server.game.GameEventLoopback;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

/**
 * Class for scheduling table actions to be executed in the future.
 */
public class TableActionScheduler {

    private final Logger log = Logger.getLogger(getClass());

    /**
     * Provides a loopback to the tables. 
     */
    private GameEventLoopback eventLoopBack;

    /** Used for scheduling loopback events. */
    private final JmxScheduler scheduler;

    /**
     * Maps UUIDs to {@link ScheduledFuture}s, so that scheduled tasks can be
     * canceled. TODO: Synchronize?
     */
    private Map<UUID, FutureContainer> futureMap = new ConcurrentHashMap<UUID, FutureContainer>();

	private final EventFetcherStats stats;

    /**
     * @param scheduler Scheduler to use, must not be null
     * @param stats Statistics to use, may be null
     */
    public TableActionScheduler(JmxScheduler scheduler, EventFetcherStats stats) {
		Arguments.notNull(scheduler, "scheduler");
    	this.scheduler = scheduler;
    	this.stats = stats;
    }
    
    
    /**
     * Schedules an action to be executed after a delay of the specified number
     * of milliseconds.
     * 
     * @param action
     * @param delay delay in milliseconds
     * @param table the table this action should be executed on
     * @param id Optional id if already known, may be null for new actions
     * @return a {@link UUID} representing a handle for the scheduled task. This
     *         can be used for canceling the task.
     */
    public UUID scheduleLoopback(GameAction action, long delay, Table table, UUID id) {
        if(id != null && futureMap.containsKey(id)) {
        	log.warn("Attempted double commit of task with it '" + id + "'; Dropping!");
        	return id;
        }
    	
    	// Create an identifier for this scheduled action.
        UUID uuid = (id == null ? UUID.randomUUID() : id);
        
        GameEvent event = new GameEvent();
        
        // Create a wrapper action.
        ScheduledGameAction scheduledAction = new ScheduledGameAction(action.getTableId(), action, uuid);
        event.setTableId(action.getTableId());
        event.setAction(scheduledAction);

        ScheduledTask task = new ScheduledTask(event, uuid);

        // Schedule the action.
        ScheduledFuture<?> future = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
        
        // Add the action to the map, so we can cancel it.
        futureMap.put(uuid, new FutureContainer(action.getTableId(), future));
        
        // Set the identifier on the action, if it is identifiable.
        if (action instanceof IdentifiableAction) {
            ((IdentifiableAction) action).setIdentifier(uuid);
        }

        // Add the action to the table, for failover support.
        // ((DefaultTable)table).addScheduledGameAction(delay, action, uuid);
        return uuid;
    }
    
    /**
     * Cancels a loopback that has been scheduled. The UUID is used to identify which task to cancel. 
     * The table parameter may be null, in which case the action may still be held by the table.
     *
     * @param uuid the identifier for the task to cancel
     */
    public void cancelSceduledLoopback(UUID uuid/*, Table table*/) {
        FutureContainer fc = futureMap.remove(uuid);
        if (fc != null  &&  fc.getFuture() != null  &&  !fc.getFuture().cancel(false)) {
            log.warn("Could not cancel task with id " + uuid +". It has probably already been executed.");
        }
    }

    /** Cancels all loopbacks scheduled on the given table.
     * @param tableId the table
     */
    public void cancelAllScheduledLoopbacks(int tableId) {
        for (Map.Entry<UUID, FutureContainer> e : futureMap.entrySet()) {
            if (e.getValue() != null  &&  e.getValue().getTableId() == tableId) {
                cancelSceduledLoopback(e.getKey());
            }
        }
    }
    
    /**
     * Sets the event loopback. This needs to be set before any events can be scheduled.
     * 
     * @param eventLoopBack
     */
    public void setEventLoopBack(GameEventLoopback eventLoopBack) {
        this.eventLoopBack = eventLoopBack;
    }

    /*public void start() {
        scheduler = new JmxScheduler(8, "TableProc-LoopBack");        
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }*/
    

    private class ScheduledTask extends SafeRunnable {
        
        private final GameEvent event;
        private final UUID identifier;

        public ScheduledTask(GameEvent event, UUID uuid) {
            this.event = event;
            this.identifier = uuid;
        }

        public void innerRun() {
        	HitCounter.getInstance().inc("ScheduledEvent");
            if(stats != null) {
        		stats.enterExecution();
        	}
        	try {
            	eventLoopBack.dispatch(event);    
        	} finally {
        		futureMap.remove(identifier);
            	if(stats != null) {
            		stats.exitExecution();
            	}
        	}
        }
    }

    public static class FutureContainer implements Serializable {
    	
        private static final long serialVersionUID = -3142269228749190381L;
		
        private int tableId;
        private ScheduledFuture<?> future;
        
        public FutureContainer(int tableId, ScheduledFuture<?> future) {
            super();
            this.tableId = tableId;
            this.future = future;
        }
        
        public int getTableId() {
            return tableId;
        }
        
        public ScheduledFuture<?> getFuture() {
            return future;
        }
    }
}
