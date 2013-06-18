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
package com.cubeia.firebase.server.event.processing;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

public abstract class ReceiverEventDaemonBase implements Startable, Haltable {

	// private static final long MAX_SILENCE_WAIT = 5000;

	public static final String MDC_TABLE_ID = "tableid";
	public static final String MDC_PLAYER_ID = "playerid";
	public static final String MDC_TOURNAMENT_ID = "tournid";
	
	protected JmxScheduler exec;

	protected final String nodeId;
	protected final EventFetcherStats stats;
	
	private final Logger log = Logger.getLogger(getClass());

	protected Receiver<RouterEvent> receiver;
	protected EventListener<RouterEvent> listener;

	private MBeanServer mbs;

	protected ReceiverEventDaemonBase(String nodeId) {
		Arguments.notNull(nodeId, "nodeId");
		stats = new EventFetcherStats();
		this.nodeId = nodeId;
	}
	
	public void init(int schedulerSize, MBeanServer mbs, Receiver<RouterEvent> receiver) {
		Arguments.positive(schedulerSize, "schedulerSize");
		Arguments.notNull(mbs, "mbean server");
		Arguments.notNull(receiver, "receiver");
		this.mbs = mbs;
		this.receiver = receiver;
		initExecutor(schedulerSize);
		initReceiver();
		initJmx();
	}
	
	public void destroy() {
		destroyExecutor();
		destroyJmx();
	}
	
	
	
	// --- HALTABLE --- //
	
	public void halt() {
		if(exec != null) {
			exec.halt();
			checkSilence();
		}
	}

	public boolean isHalted() {
		if(exec != null) {
			return exec.isHalted();
		} else {
			return false;
		}
	}
	
	public void resume() {
		if(exec != null) {
			exec.resume();
		}
	}
	
	public void start() {
		startListening();
	}
	
	public void stop() {
		stopListening();
	}
	
	
	// ---- ABSTRACT METHODS --- //
	
	protected abstract String getSchedulerName();
	
	protected abstract void dispatch(RouterEvent event);
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkSilence() {
		/*long threshold = System.currentTimeMillis() + MAX_SILENCE_WAIT;
		while(exec.getActiveCount() > 0 && System.currentTimeMillis() < threshold) {
			try {
				Thread.sleep(50);
			} catch(InterruptedException e) { }
		}*/
		long count = exec.getActiveThreadCount();
		if(count > 0) {
			// log.warn("Halt interrupted after " + MAX_SILENCE_WAIT + "; There may still be executing events! Pool thread count: " + count + "; Manual exec count: " + exec.getActiveCount());	
			log.warn("Executor not silent after halt! Pool thread count: " + exec.getActiveCount() + "; Manual exec count: " + count);	
		}
	}
	
	private void initReceiver() {
		listener = new EventListener<RouterEvent>() {
			
			public void eventReceived(RouterEvent event) {
				exec.submit(new Handler(event));
			}
		};
	}
	
	private void startListening() {
		receiver.addEventListener(listener);
	}
	
	private void stopListening() {
		receiver.removeEventListener(listener);
	}
	
	private void initJmx() {
		try {
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.daemon:type=" + getMBeanDiscriminator());
	        mbs.registerMBean(stats, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
    }
	
	private String getMBeanDiscriminator() {
		String s = getClass().getName();
		int i = s.lastIndexOf('.');
		return (i == -1 ? s : s.substring(i + 1));
	}
	
	private void destroyJmx() {
		try {
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.daemon:type=" + getMBeanDiscriminator());
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
	}
	
	private void destroyExecutor() {
		exec.shutdownNow();
	}
	
	private void initExecutor(int size) {
		exec = new JmxScheduler(size, getSchedulerName());
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Handler extends SafeRunnable {

		private final RouterEvent event;
		
		Handler(RouterEvent event) {
			this.event = event;
		}
		
		public void innerRun() {
			if(event.isValid()) {
				stats.enterExecution();
				try {
					dispatch(event);
				} finally {
					stats.exitExecution();
				}
			}
		}
	}
}
