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

/**
 * This JMX interface represents an executor in the Firebase
 * system. It contains information on the underlying thread pool.
 * 
 * @author Larsan
 */
public interface JmxExecutorMBean {
	
	public boolean isHalted();
    
	/**
	 * Get the number of items in the underlying queue for
	 * the thread pool.
	 * 
	 * @return The number of items in the queue
	 */
	public long getQueueSize();
    
	public long getHaltThreadQueueSize();
	
	/**
	 * This method returns the approximate number of currently 
	 * executing threads from the pool.
	 * 
	 * @return The current number of executing threads
	 */
    public long getActiveThreadCount();
    
    public long getHaltThreadActiveThreadCount();
    
    /**
     * Get the number of threads that currently exists in the pool.
     * 
     * @return The number of threads in the pool
     */
    public long getThreadCount();
    
    /**
     * Returns the approximate total number of tasks that have been scheduled for execution.
     * 
     * @return The number of tasks scheduled for execution
     */
    public long getTaskCount();
    
    /**
     * Returns the approximate total number of tasks that have completed execution.
     * 
     * @return The total number of tasks that have completed execution
     */
    public long getCompletedTaskCount();
    
}
