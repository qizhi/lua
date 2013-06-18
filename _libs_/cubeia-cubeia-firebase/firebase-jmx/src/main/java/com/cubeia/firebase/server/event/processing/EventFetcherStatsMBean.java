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

/**
 * The event daemons in the system are processing devices that manages events as 
 * received from the message bus and forwards them for local processing. This bean
 * contains some statistics for this operation, which is multi threaded
 * 
 * @author Larsan
 */
public interface EventFetcherStatsMBean {

	/**
	 * Each daemon is backed by a thread pool, this method returns
	 * the current number of executing threads.
	 * 
	 * @return The aprox current number of executing threads
	 */
	public int getNumberOfExecutingThreads();
	
	
	/*
	 * @return The aprox current number of waiting threads
	 */
	// public int getNumberOfWaitingForExecutionThreads();
	
	
	/*
	 * @return The aprox current number of sleeping threads
	 */
	// public int getNumberOfSleepingThreads();
	
	
	/**
	 * This method returns the number of executions per second. This is in effect the
	 * the number of times the underlying message bus have delivered a message and this
	 * message has been executed, but excludes scheduled events.
	 * 
	 * @return The aprox number of executed actions per second
	 */
	public int getExecutionActionsPerSecond();
	
	/**
	 * This method returns the number of events dispatched per second. This 
	 * includes both message bus events and scheduled events.
	 * 
	 * @return The aprox number of dispatched events
	 */
	public int getDispatchedEventsPerSecond();
	
	
	/*
	 * This method returns the number of non-execution per second. This is in effect the
	 * the number of times the underlying message bus have failed to deliver a message and the 
	 * execution is bypassed. 
	 * 
	 * @return The aprox number of executed actions per second
	 */
	// public int getPollActionsPerSecond();
	
	
	/*
	 * This method returns the time in millis from global synch lock, to the end of the
	 * executions chian, inluding optional waiting for out-of order synchronization. This number is 
	 * aproximate and is based on a bounded queue of 10k events. This method will return -1 of 
	 * a non-waiting algorithm is used.
	 * 
	 * @return The aprox avarage execution time in millis
	 */
	// public double getAvarageWaitExecutionTime();
	
	
	/*
	 * This method returns the time in millis from global synch lock, to the end of the
	 * executions chain, exluding optional waiting for out-of order synchronization. This number is 
	 * aproximate and is based on a bounded queue of 10k events. This method will return -1 of 
	 * a waiting algorithm is used.
	 * 
	 * @return The aprox avarage execution time in millis
	 */
	// public double getAvarageDirectExecutionTime();
	
	
	/**
	 * This method returns the time in millis of the raw execution. This number is 
	 * approximate and is based on a bounded queue of 10k events. 
	 * 
	 * @return The aprox average execution time in millis
	 */
	public double getAverageRawExecutionTime();
	
	
	/*
	 * This method returns the time in millis of a failed poll, ie a poll wich 
	 * returns nuyll and thus bypasses execution. This number is aproximate and 
	 * is based on a bounded queue of 10k events. 
	 * 
	 * @return The aprox avarage execution time in millis
	 */
	// public double getAvaragePollTime();
	
}
