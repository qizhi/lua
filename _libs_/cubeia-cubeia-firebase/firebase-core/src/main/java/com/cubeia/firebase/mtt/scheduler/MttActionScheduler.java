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
package com.cubeia.firebase.mtt.scheduler;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.IdentifiableAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.ScheduledMttAction;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.mtt.event.MttEventLoopback;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

/**
 * Class for scheduling table actions to be executed in the future.
 * 
 * 
 */
public class MttActionScheduler implements Startable {

    /** Logger. Note, not private because it is being used by inner class. */
    private final Logger log = Logger.getLogger(MttActionScheduler.class);

    /**
     * Provides a loopback to the tables. 
     */
    private MttEventLoopback eventLoopBack;

    /** Used for scheduling loopback events. */
    private JmxScheduler scheduler;

    /**
     * Maps UUIDs to {@link ScheduledFuture}s, so that scheduled tasks can be
     * canceled. TODO: Synchronize?
     */
    private Map<UUID, FutureContainer> futureMap = new ConcurrentHashMap<UUID, FutureContainer>();
    
    /**
     * Sets the event loopback. 
     * This needs to be set before any events can be scheduled.
     * 
     * @param eventLoopBack
     */
    public void setEventLoopBack(MttEventLoopback eventLoopBack) {
        this.eventLoopBack = eventLoopBack;
    }

    public void start() {
        scheduler = new JmxScheduler(8, "MttProc-LoopBack");        
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Schedules an action to be executed after a delay of the specified number
     * of milliseconds.
     * 
     * @param action
     * @param delay delay in milliseconds
     * @param id Optional id if already known, may be null for new actions
     * @return a {@link UUID} representing a handle for the scheduled task. This
     *         can be used for cancelling the task.
     */
    public UUID scheduleLoopback(MttAction action, long delay, UUID id) {
        if(id != null && futureMap.containsKey(id)) {
        	log.warn("Attempted double commit of task with it '" + id + "'; Dropping!");
        	return id;
        }
        
        // Create an identifier for this scheduled action.
        UUID uuid = (id == null ? UUID.randomUUID() : id);
        
        MttEvent event = new MttEvent();
        
        // Create a wrapper action.
        ScheduledMttAction scheduledAction = new ScheduledMttAction(action.getMttId(), action, uuid);
        event.setMttId(action.getMttId());
        event.setAction(scheduledAction);

        ScheduledTask task = new ScheduledTask(event, uuid);

        // Schedule the action.
        ScheduledFuture<?> future = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
        
        // Add the action to the map, so we can cancel it.
        futureMap.put(uuid, new FutureContainer(action.getMttId(), future));
        
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
     * @param uuid the identifier for the task to cancel, must not be null
     */
    public void cancelSceduledLoopback(UUID uuid/*, Table table*/) {
        FutureContainer container = futureMap.remove(uuid);
        ScheduledFuture<?> future = (container == null ? null : container.future);
        if (future != null && !future.cancel(false)) {
            log.warn("Could not cancel task with id " + uuid +". It has probably already been executed.");
        }
    }

    public void cancelAllScheduledLoopbacks(int mttId) {
        for (Map.Entry<UUID, FutureContainer> e : futureMap.entrySet()) {
            if (e.getValue() != null  &&  e.getValue().getMttId() == mttId) {
                cancelSceduledLoopback(e.getKey());
            }
        }
    }
    

    /**
     * Holds the scheduled action.
     * 
     */
    private class ScheduledTask extends SafeRunnable {
        
        private final MttEvent event;
        
        private final UUID identifier;

        public ScheduledTask(MttEvent event, UUID uuid) {
            this.event = event;
            this.identifier = uuid;
        }

        public void innerRun() {
        	HitCounter.getInstance().inc("SchedEvent");
            futureMap.remove(identifier);
            eventLoopBack.dispatch(event);
        }
    }
    
    public static class FutureContainer implements Serializable {
    	
        private static final long serialVersionUID = -3142269228749190381L;
		
        private int mttId;
        private ScheduledFuture<?> future;
        
        public FutureContainer(int mttId, ScheduledFuture<?> future) {
            super();
            this.mttId = mttId;
            this.future = future;
        }
        
        public int getMttId() {
            return mttId;
        }
        
        public ScheduledFuture<?> getFuture() {
            return future;
        }
    }
}
