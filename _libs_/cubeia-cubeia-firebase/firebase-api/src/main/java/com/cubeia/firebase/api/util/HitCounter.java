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
package com.cubeia.firebase.api.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Startable;


/**
 * This class is used for counting hits of different events and printing the statistics. 
 * 
 * Use <code>HitCounter.inc("identifier")</code> to count a hit for that identifier,
 * as soon as something interesting happens in the code, 
 *  
 * To print the hit count statistics, use <code>HitCounter.startStatisticPrinter()</code>
 * 
 * Access the service as a singleton.
 * 
 * @author viktor.nordling
 * 
 */
public class HitCounter implements Startable {     
	
	/** Milliseconds between printouts */
    private static final int PERIOD = 60000;

    /** Logger. */
    private static transient Logger log = Logger.getLogger("stats");
    
    /** Es kann nur ein geben */
    private static HitCounter instance = new HitCounter();

	/** Map containing the number of times each identifier has occured */
    private Map<String, AtomicLong> hitMap = new ConcurrentHashMap<String, AtomicLong>();
    
    
    /** Variable to keep track of whether we should count hits or not. */
    private volatile boolean enabled = false;

    /**
     * Holds reference to historically printed headers
     * so we can keep the columns constant.
     */
    private ArrayList<String> headers = new ArrayList<String>();
    
    private HitCounter() {
    	log.info("HitCounter Started\tPeriod (ms):\t"+PERIOD);
    }
    
    /** The timer thread */
    private Timer timer;
    
    public static HitCounter getInstance() {
    	return instance;
    }
    
    /**
     * Starts the printing of statistics.
     *
     */
    public void start() {    
    	timer = new Timer();
    	enabled = true;
    	PrintTask task = new PrintTask();
        timer.schedule(task, PERIOD);
    }
    
    /**
     * Start counting hits and printing stats
     * 
     * @param state the state to set
     */
    public void stop() {
    	if(timer != null) {
    		timer.cancel();
    	}
        enabled = false;
    }

    
    /**
     * Increments the hit counter for given key by one.
     * 
     * @param key
     */
    public void inc(String key) {
        incCustom(key,1);   
    }
    
    
    /**
     * Increments the hit counter for given key by given amount.
     * 
     * @param key
     */
    public void incCustom(String key, int amount) {
    	if (enabled) {
	        AtomicLong count = hitMap.get(key);
	        
	        if (count == null) {
	            count = new AtomicLong(amount);
	        } else {
	            count.addAndGet(amount);
	        }
	        
	        hitMap.put(key, count);        
    	}
    }
    
    /**
     * Set key with given amount (replace if present).
     * @param key
     * @param amount
     */
    public void set(String key, int amount) {
    	if (enabled) {
	        AtomicLong count = hitMap.get(key);
	        count = new AtomicLong(amount);
	        hitMap.put(key, count);        
    	}
    }
    
    
    /**
     * Prints the statistics.
     * First all keys are 
     *
     */
    public void printStatistics() {
        StringBuilder string = new StringBuilder();
        
        // Make sure all entries are cached and sorted.
        sortHeader (hitMap.entrySet());
        
        // Iterate all headers, get the associated value and output to logfile
        for (String key : headers) {
            string.append(key+"\t"+hitMap.get(key)+"\t");   
        }
        
        log.info(string);
    }     
    
    /**
     * Checks if all keys are cached in an ordered header set.
     * If not it is added last in the list.
     * 
     * @param entries
     */
    private void sortHeader(Set<Entry<String, AtomicLong>> entries){
        for (Entry<String, AtomicLong> entry : entries) {
            if (!headers.contains(entry.getKey())) {
                headers.add(entry.getKey().toString());
            }
        }
    }

    /**
     * Clears the statistics, by setting all values to zero.
     *
     */
    private void clearStatistics() {
        Set<Entry<String, AtomicLong>> entries = hitMap.entrySet();
        for (Entry<String, AtomicLong> entry : entries) {
            entry.setValue(new AtomicLong(0));   
        } 
    }

    /**
     * Checks if we should count hits.
     * 
     * @return true if so
     */
    public boolean isEnabled() {
        return enabled ;
    }        

    private class PrintTask extends TimerTask {
    	 @Override
         public void run() {
             try {
                 printStatistics();
                 clearStatistics();
             } catch (Exception e) {
                 log.error("run(): " + e);
             }            
             
             if (enabled) {
            	PrintTask task = new PrintTask();
             	timer.schedule(task, PERIOD);
             }
         }

    }
}
