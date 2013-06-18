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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Haltable;
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
public class JmxExecutor implements JmxExecutorMBean, Haltable {

    private static final long DEFAULT_TTL = 60000;

	/** The logger */
    private Logger log = Logger.getLogger(JmxExecutor.class);
    
    /** The underlying thread pool for executing work */
    private ThreadPoolExecutor executor;    
    
    /** Keep a reference to the thread name for logging and JMX purposes */
    private String name;
	private String jmxId; // optional "id" in jmx name
	
    /** Halt lock, use read lock on execution and a write lock for halt */
    private final ReadWriteLock haltLock = FirebaseLockFactory.createLock();
    private final AtomicBoolean isHalted = new AtomicBoolean(false);
	
	private final ThreadPoolExecutor haltThread;
    
	public JmxExecutor(int coreSize, String name) {
        this(coreSize, coreSize, Long.MAX_VALUE, name, null);
    }
	
    public JmxExecutor(int minSize, int coreSize, String name) {
        this(minSize, coreSize, DEFAULT_TTL, name, null);
    }
	
    public JmxExecutor(int minSize, int coreSize, long ttl, String name) {
        this(minSize, coreSize, ttl, name, null);
    }
    
    public JmxExecutor(int coreSize, String jmxType, String jmxId) {
    	this(coreSize, coreSize, Long.MAX_VALUE, jmxType, jmxId);
    }
    
    public JmxExecutor(int minSize, int coreSize, String jmxType, String jmxId) {
    	this(minSize, coreSize, Long.MAX_VALUE, jmxType, jmxId);
    }
    
    public JmxExecutor(int minSize, int coreSize, long ttl, String jmxType, String jmxId) {
        this.name = jmxType;
        this.jmxId = jmxId;
        haltThread = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS , new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name + " - HaltThread"));
        executor = new ThreadPoolExecutor(minSize, coreSize, ttl, TimeUnit.MILLISECONDS , new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name)) {
        	
        	@Override
        	protected void beforeExecute(Thread t, Runnable r) {
        		haltLock.readLock().lock();
        		super.beforeExecute(t, r);
        	}
        	
        	@Override
        	protected void afterExecute(Runnable r, Throwable t) {
        		haltLock.readLock().unlock();
        		super.afterExecute(r, t);
        	}
        };
        initJmx();
    }
    
    public void halt() {
    	if(!isHalted() && !haltThread.isShutdown()) {
    		sumbitHalt();
    	}
    }

    public boolean isHalted() {
    	return isHalted.get();
    }
    
    public void resume() {
    	if(isHalted() && !haltThread.isShutdown()) {
	    	submitResume();
    	}
    }
    
    /**
     * Submit a task for execution.
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * Submit a task for execution.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void submit(Callable task) {
        executor.submit(task);
    }
    
    public long getTaskCount() {
    	return executor.getTaskCount();
    }
    
    public long getQueueSize() {
        return executor.getQueue().size();
    }

    public long getActiveThreadCount() {
        return executor.getActiveCount();
    }
    
    public long getThreadCount() {
        return executor.getPoolSize();
    }
    
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }
    
    public long getMaxThreads() {
    	return executor.getMaximumPoolSize();
    }
    
    public String getStateDescription() {
        return executor.isShutdown() ? "Shutdown" : "Running";
    }
    
    public String getName() {
    	return name;
    }
    
    /**
     * Currently does nothing. 
     * The executor is initialized in the constructor.
     */
    public void start() {}
    
    /**
     * Shuts down the executor.
     * Currently this is a one way stop, it is not
     * possible to re-start the service.
     * 
     */
    public void stop() {
        executor.shutdown();
        haltThread.shutdown();
        destroyJmx();
    }
    
    /**
     * Shuts down the executor.
     * Currently this is a one way stop, it is not
     * possible to re-start the service.
     * 
     */
    public void stopNow() {
        executor.shutdownNow();
        haltThread.shutdown();
        destroyJmx();
    }


	/**
     * Add MBean info to JMX.
     * Will be called from the constructor.
     *
     */
    private void initJmx() {
        try {
            // log.info("Binding JMX Executor '"+name+"' to mbean server");
            MBeanServer mbs = getMBeanServer();
            String tmp = toFullJmxName();
            ObjectName monitorName = new ObjectName(tmp);
            mbs.registerMBean(this, monitorName);
        } catch(Exception e) {
            log.error("failed to start JMX for named threads: "+name, e);
        }
    }

	private String toFullJmxName() {
		String tmp = "com.cubeia.firebase.threads:type=executor,name=" + name;
		if(this.jmxId != null) {
			tmp += ",id=" + ObjectName.quote(jmxId);
		}
		return tmp;
	}
	
    private Future<?> sumbitHalt() {
		return haltThread.submit(new SafeRunnable() {
			public void innerRun() {
				log.debug("Attempting to halt executor '" + name + "'... (exec count: " + getActiveThreadCount() + ")");
				haltLock.writeLock().lock();
				log.debug("Executor '" + name + "' halted (exec count: " + getActiveThreadCount() + ")");
				isHalted.set(true);
			}
		});
	}
	
	private Future<?> submitResume() {
		return haltThread.submit(new SafeRunnable() {
			public void innerRun() {
				log.debug("Attempting to resume executor '" + name + "'... (exec count: " + getActiveThreadCount() + ")");
				haltLock.writeLock().unlock();
				log.debug("Executor '" + name + "' resumed (exec count: " + getActiveThreadCount() + ")");
				isHalted.set(false);
			}
		});
	}
    
    private void destroyJmx() {
        try {
            // log.info("Unbinding JMX Executor '"+name+"' from mbean server");
            MBeanServer mbs = getMBeanServer();
            String tmp = toFullJmxName();
            ObjectName monitorName = new ObjectName(tmp);
            if(mbs.isRegistered(monitorName)) {
            	mbs.unregisterMBean(monitorName);
            }
        } catch(Exception e) {
            log.error("failed to start JMX for named threads: "+name, e);
        }
	}
    
    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
	
	public long getHaltThreadQueueSize() {
		return ((ThreadPoolExecutor)haltThread).getQueue().size();
	}
	
    public long getHaltThreadActiveThreadCount() {
        return ((ThreadPoolExecutor)haltThread).getActiveCount();
    }
}
