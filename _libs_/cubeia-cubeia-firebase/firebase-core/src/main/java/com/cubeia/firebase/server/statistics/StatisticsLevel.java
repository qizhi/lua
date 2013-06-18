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
package com.cubeia.firebase.server.statistics;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * <p>This class is used to hold the current level of statistics that are harvested
 * and exposed runtime by Firebase.</p>
 * 
 * <p>The flag may be changed runtime, but it is up to the statistical implementations
 * to check if the flag has changed.</p>
 *
 * <p>The implementation follows the singleton pattern, meaning that the statistic level
 * will be uniform on the server.</p>
 * 
 * <p>The levels are defined by the Level enum.</p>
 * 
 * <p>The default level will be PROFILING.</p>
 *
 * <p>You can register a listener to this class if you are interested in knowing when
 * the statistics level has changed. This comes in handy when you might need to 
 * refconfigure things locally in order to accomodate a change.</p>
 *  
 *  <p>TODO: The life cycle is not defined for this class so I am not cleaning up
 *  the JMX bean properly. Not sure if this is an issue, but nevertheless we
 *  might look into this if we have the time.</p>
 *
 *
 *
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class StatisticsLevel implements StatisticsLevelMBean {

	private static transient Logger log = Logger.getLogger(StatisticsLevel.class);
	
	/** The single instance */
	private static StatisticsLevel instance = new StatisticsLevel();
	
	private Level level = Level.PROFILING;
	
	private List<StatisticsLevelListener> listeners = new ArrayList<StatisticsLevelListener>();
	
	/** Private constructor */
	private StatisticsLevel() {
		bindToJMX();
	}
	
	public static StatisticsLevel getInstance() {
		return instance;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public String getLevelName() {
		return level.name();
	}
	
	public void addListener(StatisticsLevelListener listener) {
		listeners.add(listener);
	}
	
	public boolean removeListener(StatisticsLevelListener listener) {
		return listeners.remove(listener);
	}
	
	
	
	/*--------------------------------
	 * Runtime setters of the level
	 * 
	 *--------------------------------*/
	
	
	public void setLevel(Level value) {
		log.info("Statistics level for the system: "+value.name());
		Level oldLevel = level;
		level = value;
		for (StatisticsLevelListener listener : listeners) {
			listener.statisticsLevelChanged(oldLevel, level);
		}
	}
	
	public void setLevelInt(int threshold) {
		setLevel(Level.values()[threshold]);
	}
	
	public void setLevelString(String name) {
		setLevel(Level.valueOf(name));
	}
	
	
	
	/*--------------------------------
	 * Logic
	 * 
	 *--------------------------------*/
	
	/**
	 * <p>Check if the supplied statistics level is currently active.</p>
	 * 
	 * <p>i.e. you can use this to filter statistics:</br>
	 * <code>
	 * if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {</br>
	 * &nbsp;    ... // Do you statistics thing here</br>
	 * }
	 * </code>
	 * </p>
	 * 
	 * 
	 * 
	 * @param check
	 * @return true if enabled
	 */
	public boolean isEnabled(Level check) {
		return check.getThreshold() <= level.getThreshold();
	}
	
	
	
	/*------------------------------------------------
	 
		JMX

	 ------------------------------------------------*/
	
	private void bindToJMX() {
	    try{
	        MBeanServer mbs = getMBeanServer();
	        ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=Statistics");
	        mbs.registerMBean(this, monitorName);
	    }catch(Exception ex) {
	        log.error("Could not bind Statistics Level to the JMX Server", ex);
	    }
	}
	
	@SuppressWarnings("unused")
	private void unbindFromJMX() {
	    try{
	        MBeanServer mbs = getMBeanServer();
	        ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=Statistics");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
	    }catch(Exception ex) {
	        log.error("Could not unbind Statistics Level from the JMX Server", ex);
	    }
	}
	
	private MBeanServer getMBeanServer() {
	    return ManagementFactory.getPlatformMBeanServer();
	}

}
