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
package com.cubeia.test.loadtest.bot;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.bot.Bot;
import com.cubeia.firebase.bot.ai.BasicAI;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.test.loadtest.io.protocol.LoadRequest;
import com.cubeia.test.loadtest.io.protocol.LoadResponse;
import com.cubeia.test.loadtest.io.protocol.ProtocolObjectFactory;

public class BotImpl extends BasicAI {
	
	private static final Stats stats = new Stats(1000);
	private static final Random RAND = new Random();
	private static final int PRINT_EVERY = 40;
	
	private static final AtomicBoolean MOUNT_LOCK = new AtomicBoolean(false);
	
	static void tryMountStatsBean() {
		if(MOUNT_LOCK.compareAndSet(false, true)) {
			MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
			try {
				serv.registerMBean(stats, new ObjectName("com.cubeia.test.loadtest.bot:type=Stats"));
			} catch (Exception e) {
				System.err.println("FAILED TO MOUNT STATS MBEAN");
				e.printStackTrace();
			} 
		}
	}
	
	private final ProtocolObjectFactory fact = new ProtocolObjectFactory();
	private final StyxSerializer styx = new StyxSerializer(fact);
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Queue<Integer> sequenceQueue = new ConcurrentLinkedQueue<Integer>();
	private final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicInteger loadSequence = new AtomicInteger();
	
	private boolean fixedRate;
	private int executionDelay;
	private long lastSendTimeStamp;
	private long timeoutPeriod = 30000; 
	private long sendInterval = 1000; 
	
	private ScheduledFuture<?> task;
	private ScheduledFuture<?> timeout;
	
	public BotImpl(Bot bot) {
		super(bot);
	}
	
	public boolean getFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}

	public int getExecutionDelay() {
		return executionDelay;
	}

	public void setExecutionDelay(int executionDelay) {
		this.executionDelay = executionDelay;
	}

	public long getSendInterval() {
		return sendInterval;
	}

	public void setSendInterval(long sendInterval) {
		this.sendInterval = sendInterval;
	}

	@Override
	public void handleGamePacket(GameTransportPacket action) {
		LoadResponse resp = parsePacket(action);
		if(isFromSelf(resp)) {
			handeResponse(resp);
		} else if(!isRightTable(resp)) {
			log.fatal("I received wrong table id! I am seated at: "+getTable().getId()+". I got packet from: "+resp.tableid+" PACKET("+resp+")");
		}
	}
	
	@Override
	public void handleDisconnected() {
		super.handleDisconnected();
		doStop();
	}
	
	@Override
	protected void handleSeated() {
		super.handleSeated();
		tryMountStatsBean();
		running.set(true);
		if(sequenceQueue.size() == 0) {
			scheduleFirst();
		} else {
			/*
			 * This must be a reconnect, so re-send what we're waiting for
			 * and schedule a new timeout.
			 */
			logInfo("Sending reconnect to table [seq=" + loadSequence.get() + "]");
			scheduleReconnect();
		}
	}

	@Override
	public void handleProbePacket(ProbePacket arg0) { }

	@Override
	public void stop() { 
		doStop();
	}

	
	// --- PRIVATE METHODS --- //
	
	private void scheduleFirst() {
		if(fixedRate) {
			int initDelay = RAND.nextInt((int)sendInterval);
			task = scheduleFixed(new Logic(), initDelay);
		} else {
			scheduleNext();
		}
	}
	
	private ScheduledFuture<?> scheduleFixed(Runnable task, int initDelay) {
		return executor.scheduleAtFixedRate(task, initDelay, sendInterval, MILLISECONDS);
	}

	private void doStop() {
		running.set(false);
		checkAndCancel(timeout);
		checkAndCancel(task);
	}
	
	private void checkAndCancel(ScheduledFuture<?> task) {
		if(task != null) {
			task.cancel(false);
		}
	}
	
	private void handeResponse(LoadResponse resp) {
		logDebug("Got response: [seq=" + resp.seq + "; resend=" + resp.resend + "; pid=" + resp.pid + "]");
		checkCorrectReturn(resp.seq, resp.resend);
		checkQueueSize();
		checkAndSchedule();
	}

	private void checkAndSchedule() {
		if (!fixedRate && running.get()) {
			scheduleNext();
		}
	}
	
	private void scheduleReconnect() {
		checkAndCancel(timeout);
	    timeout = schedule(new Timeout(), timeoutPeriod);
		task = schedule(new Reconnect(), 0);
    }

	private void scheduleNext() {
		checkAndCancel(timeout);
	    timeout = schedule(new Timeout(), timeoutPeriod);
		task = schedule(new Logic(), sendInterval);
    }

	private ScheduledFuture<?> schedule(Runnable task, long delay) {
		return executor.schedule(task, delay, MILLISECONDS);
	}

	private void checkQueueSize() {
		if(sequenceQueue.size() > 100) {
			logInfo("Load packet sequence queue getting bigger! [size=" + sequenceQueue.size() + "]");
		}
	}
	
	private void checkCorrectReturn(int seq, boolean isResend) {
		int test = sequenceQueue.size() > 0 ? sequenceQueue.remove() : -1;
		if(isResend) {
			logInfo("Received resend packet; Have we fallen over?");
		}
		if(seq != test) {
			logFatal("Load packet out of order! [expected=" + test + "; actual=" + seq + "; resend=" + isResend + "]");
			stats.incrementFails();
		} else {
			long elapsed = System.currentTimeMillis() - lastSendTimeStamp;
			if (counter.incrementAndGet() % PRINT_EVERY == 0) {
				logInfo("Load packet return time: " + elapsed + " ms");
			}
			stats.register(elapsed);
		}
	}
	
	private boolean isRightTable(LoadResponse resp) {
		return resp.tableid == getTable().getId();
	}
	
	private boolean isFromSelf(LoadResponse resp) {
		return resp.pid == getBot().getId();
	}
	
	private LoadResponse parsePacket(GameTransportPacket action) {
		try {
			return (LoadResponse) styx.unpack(ByteBuffer.wrap(action.gamedata));
		} catch (Exception e) {
			throw new IllegalStateException("protocol parse error", e);
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Logic implements Runnable {
		
		@Override
		public void run() {
			LoadRequest req = new LoadRequest();
			req.pid = getBot().getId();
			req.tableid = getTable().getId();
			req.seq = loadSequence.incrementAndGet();
			req.delay = executionDelay;
			sequenceQueue.add(req.seq);
            bot.sendGameData(req.tableid, req.pid, (ProtocolObject) req);
            lastSendTimeStamp = System.currentTimeMillis();
		}
	}
	
	private class Reconnect implements Runnable {
		
		@Override
		public void run() {
			LoadRequest req = new LoadRequest();
			req.pid = getBot().getId();
			req.tableid = getTable().getId();
			req.seq = loadSequence.get(); // re-send latest
			req.delay = executionDelay;
			req.resend = true; // this is a re-send
			// do not add to queue... sequenceQueue.add(req.seq);
            bot.sendGameData(req.tableid, req.pid, (ProtocolObject) req);
            lastSendTimeStamp = System.currentTimeMillis();
		}
	}
	
	private class Timeout implements Runnable {
        
        public void run() {
        	stats.incrementTimeouts();
            logInfo("Timeout triggered. Table: " + getTable().getId() + " Waiting on event: " + sequenceQueue.peek());
            sequenceQueue.clear(); // ?!
            scheduleNext();
        }   
    }
}
