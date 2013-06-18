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
package com.cubeia.firebase.util.executor;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.util.FirebaseLockFactory;
import com.cubeia.util.threads.NamedThreadFactory;
import com.cubeia.util.threads.SafeRunnable;

/**
 * The JMX Executor is an MBean monitoring wrapper for a Thread pool.
 * 
 * The underlying executor will not support scheduling and uses an
 * unbound blocking queue. If you want to support a different queue etc.
 * you need to subclass this class.
 * 
 * @author fredrik.johansson
 *
 */
public class JmxScheduler extends ScheduledThreadPoolExecutor implements JmxSchedulerMBean {
	
	private static final long MAX_HALT_WAIT = 3000;

	/** The logger */
    private Logger log = Logger.getLogger(JmxScheduler.class);
    
    /** Keep a reference to the thread name for logging and JMX purposes */
    private String name = "JmxScheduler";
    
    /*
	 * Trac #562: Using fair locks
	 */
    private final ReadWriteLock haltLock = FirebaseLockFactory.createLock();
    private final AtomicBoolean isHalted = new AtomicBoolean(false);
    
    private final AtomicInteger execThreads = new AtomicInteger();
	private final ThreadPoolExecutor haltThread; 
    
	/**
	 * Constructor
	 * 
	 * @param corePoolSize
	 * @param name
	 */
	public JmxScheduler(int corePoolSize, String name) {
		super(corePoolSize, new NamedThreadFactory(name));
		haltThread = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS , new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name + " - HaltThread"));
		this.name = name;
		initJmx();
	}
	
	public long getActiveThreadCount() {
		return execThreads.get();
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		haltLock.readLock().lock();
		execThreads.incrementAndGet();
		super.beforeExecute(t, r);
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		execThreads.decrementAndGet();
		haltLock.readLock().unlock();
		super.afterExecute(r, t);
	}
	
    public void halt() {
    	if(!isHalted() && !haltThread.isShutdown()) {
    		Future<?> fut = sumbitHalt();
    		try {
				fut.get(MAX_HALT_WAIT, TimeUnit.MILLISECONDS);
			} catch (Exception e) { 
				log.error("Failed to halt", e);
			}
    	}
    }

    public boolean isHalted() {
    	return isHalted.get();
    }
    
    public void resume() {
    	if(isHalted() && !haltThread.isShutdown()) {
	    	Future<?> fut = submitResume();
    		try {
    			fut.get(MAX_HALT_WAIT, TimeUnit.MILLISECONDS);
			} catch (Exception e) { 
				log.error("Failed to resume", e);
			}
    	}
    }


    /**
     * Add MBean info to JMX.
     * Will be called from the constructor.
     *
     */
    private void initJmx() {
        try {
            // log.info("Binding JMX Scheduler '"+name+"' to JMX");
            MBeanServer mbs = getMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.threads:type=scheduler,name="+name);
            mbs.registerMBean(this, monitorName);
        } catch(Exception e) {
            log.error("failed to start JMX for named threads: "+name, e);
        }
    }
    
    private Future<?> sumbitHalt() {
		return haltThread.submit(new SafeRunnable() {
			public void innerRun() {
				log.debug("Attempting to halt scheduler '" + name + "'... (exec count: " + getActiveThreadCount() + ")");
				haltLock.writeLock().lock();
				log.debug("Scheduler '" + name + "' halted (exec count: " + getActiveThreadCount() + ")");
				isHalted.set(true);
			}
		});
	}
	
	private Future<?> submitResume() {
		return haltThread.submit(new SafeRunnable() {
			public void innerRun() {
				log.debug("Attempting to resume scheduler '" + name + "'... (exec count: " + getActiveThreadCount() + ")");
				haltLock.writeLock().unlock();
				log.debug("Scheduler '" + name + "' resumed (exec count: " + getActiveThreadCount() + ")");
				isHalted.set(false);
			}
		});
	}
    
    private void destroyJmx() {
        try {
            // log.info("Unbinding JMX Scheduler '"+name+"' from mbean server");
            MBeanServer mbs = getMBeanServer();
            ObjectName monitorName = new ObjectName("com.cubeia.firebase.threads:type=scheduler,name="+name);
            if(mbs.isRegistered(monitorName)) {
            	mbs.unregisterMBean(monitorName);
            }
        } catch(Exception e) {
            log.error("failed to stop JMX for named threads: "+name, e);
        }
	}
    
    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    
    /**
     * Currently does nothing. 
     * The executor is initialized in the constructor.
     */
	public void start() {}

	
	@Override
	public void shutdown() {
		super.shutdown();
		haltThread.shutdown();
		destroyJmx();
	}
	
	@Override
	public List<Runnable> shutdownNow() {
		List<Runnable> list = super.shutdownNow();
		haltThread.shutdown();
		destroyJmx();
		return list;
	}
	
	/**
     * Shuts down the executor.
     * Currently this is a one way stop, it is not
     * possible to re-start the service.
     * 
     * will not terminate running tasks.
     * 
     */
	public void stop() {
		super.shutdown();
		haltThread.shutdown();
		destroyJmx();
	}
	
	public long getQueueSize() {
		return getQueue().size();
	}

	public long getThreadCount() {
		return getPoolSize();
	}
	
	public String getStateDescription() {
		return "";
	}
	
	public long getHaltThreadQueueSize() {
		return ((ThreadPoolExecutor)haltThread).getQueue().size();
	}
	
	
    public long getHaltThreadActiveThreadCount() {
        return ((ThreadPoolExecutor)haltThread).getActiveCount();
    }
}
