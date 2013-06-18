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
package com.cubeia.firebase.server.jmx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class SystemMonitor implements SystemMonitorMBean {    
    private final static String LOAD_AVG_FILE = "/proc/loadavg";
    private final static String CPU_INFO_FILE = "/proc/cpuinfo"; 
    private final static String MEM_INFO_FILE = "/proc/meminfo";  
    private final static String CPU_USAGE_FILE = "/proc/stat";
    private final static String OS_VERSION_FILE = "/proc/version";    
    
    private final static int LOAD_AVG_1_MIN = 0;
    private final static int LOAD_AVG_5_MIN = 1; 
    private final static int LOAD_AVG_15_MIN = 2; 
      
    private double loadAvg[] = new double[3];
    
    private final static int CPU_USER = 0;
    private final static int CPU_USER_NICE = 1; 
    private final static int CPU_SYSTEM = 2;
    private final static int CPU_IDLE = 3;
    private final static int CPU_IO_WAIT = 4; 
    private final static int CPU_IRQ = 5;
    private final static int CPU_SOFT_IRQ = 6; 
    private final static int CPU_TOTAL = 7;     
    
    private long cpuJiffies[] = new long[8];
    private long lastCpuJiffies[] = new long[8];
    
    private final static int MEM_FREE = 0;
    private final static int MEM_BUFFED = 1; 
    private final static int MEM_CACHED = 2;
    private final static int MEM_SWAPPED = 3;    
    private long memory[] = new long[4];    
    
    private Object updateLoadLock = new Object();
    private Object updateMemLock = new Object();
    private Object updateCPULock = new Object();
    
    private static transient Logger logger = Logger.getLogger(SystemMonitor.class);
    
    private static Timer updateTimer = new Timer(true);  
    private static final int UPDATE_PERIOD = 5000;
    
    private String systemInfo[] = null;

    // private boolean serverStarted = false;
    
    private static SystemMonitor instance = new SystemMonitor();    
    
    /**
     * Creates the scheduled task and reads the static system info
     *
     */
    public SystemMonitor(){
    	for (int i = 0; i < loadAvg.length; i++) {
    		loadAvg[i] = 0.0;
    	}
    	
    	for (int i = 0; i < cpuJiffies.length; i++) {
    		cpuJiffies[i] = 0;
    	}  
    	
    	updateSystemInfo();
    	
    	updateTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                	updateData();
                } catch (Throwable e) {
                    logger.error("PerformanceMonitor.updateTimer : " + e, e);
                }
            }
        }, UPDATE_PERIOD, UPDATE_PERIOD);    	
    }
    
    /*public double getAvarageGotPingReplyTime() {
        return SeatInfo.PING_REPLY_AVARAGER.calculate();
    }
    
    public void setServerStarted() {
    	serverStarted = true;
    }*/
    
    public static SystemMonitor getInstance(){
        return instance;
    }    
    
    private void updateData() {
    	updateLoad();
    	updateMemory();
    	updateCPU();
    }
    
    /**
     * Updates all dynamic data
     *
     */
    private void updateLoad() {
    	synchronized (updateLoadLock) {
	        try {
	            BufferedReader in = new BufferedReader(new FileReader(LOAD_AVG_FILE));
	            String str;
	            StringTokenizer tokens = null;
	            while ((str = in.readLine()) != null) {
	            	tokens = new StringTokenizer(str, " ");
	            	loadAvg[LOAD_AVG_1_MIN] = Double.parseDouble(tokens.nextToken());
	            	loadAvg[LOAD_AVG_5_MIN] = Double.parseDouble(tokens.nextToken());
	            	loadAvg[LOAD_AVG_15_MIN] = Double.parseDouble(tokens.nextToken());
	            }
	            in.close();
	        } 
	        catch (Exception e) {
	        	// Ignore.
	        }
    	}    	
    }
    
    private void updateMemory() {
    	synchronized (updateMemLock) {
	        try {
	            BufferedReader in = new BufferedReader(new FileReader(MEM_INFO_FILE));
	            String str;
	            StringTokenizer tokens = null;
	            long totalSwap = 0, freeSwap = 0;
	            while ((str = in.readLine()) != null) {
	            	if(str.startsWith("MemFree:")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		memory[MEM_FREE] = Long.parseLong(tokens.nextToken());
	            	}
	            	if(str.startsWith("Buffers:")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		memory[MEM_BUFFED] = Long.parseLong(tokens.nextToken());
	            	}
	            	if(str.startsWith("Cached:")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		memory[MEM_CACHED] = Long.parseLong(tokens.nextToken());
	            	}
	            	if(str.startsWith("SwapTotal:")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		totalSwap = Long.parseLong(tokens.nextToken());
	            	}
	            	if(str.startsWith("SwapFree:")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		freeSwap = Long.parseLong(tokens.nextToken());
	            		memory[MEM_SWAPPED] = totalSwap - freeSwap;
	            	}		            		
	            }
	            in.close();
	        } 
	        catch (Exception e) {
	         // Ignore.
	        }
    	}    	
    } 
    
    private void updateCPU() {
    	synchronized (updateCPULock) {
	        try {
	            BufferedReader in = new BufferedReader(new FileReader(CPU_USAGE_FILE));
	            String str;
	            StringTokenizer tokens = null;
	            // long totalSwap = 0, freeSwap = 0;
	            
        		for(int i = 0; i < cpuJiffies.length; i++) {
        			lastCpuJiffies[i] = cpuJiffies[i];
        		}	            
	            
	            while ((str = in.readLine()) != null) {
	            	if(str.startsWith("cpu ")) {
	            		tokens = new StringTokenizer(str, " ");
	            		tokens.nextToken();
	            		cpuJiffies[CPU_USER] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_USER_NICE] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_SYSTEM] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_IDLE] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_IO_WAIT] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_IRQ] = Long.parseLong(tokens.nextToken());
	            		cpuJiffies[CPU_SOFT_IRQ] = Long.parseLong(tokens.nextToken());
	            		
	            		long totalJiffies = 0;
	            		
	            		for(int i = 0; i < CPU_TOTAL; i++) {
	            			totalJiffies += cpuJiffies[i];
	            		}
	            		cpuJiffies[CPU_TOTAL] = totalJiffies;	            	
	            	}
	            }
	            in.close();
	        } 
	        catch (Exception e) {
	         // Ignore.
	        }
    	}    	
    }       
    
    private void updateSystemInfo() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(CPU_INFO_FILE));
            String str;
            // int nofCpus = 0;
            ArrayList<String> cpu = new ArrayList<String>();
            ArrayList<String> cache = new ArrayList<String>();
            while ((str = in.readLine()) != null) {
            	if(str.startsWith("processor")) {
            		// nofCpus++;
            	}
            	if(str.startsWith("model name")) {
            		cpu.add(str.substring(str.indexOf(":")+ 1));
            	}
            	if(str.startsWith("cache size")) {
            		cache.add(str.substring(str.indexOf(":")+ 1));
            	}            	
            }
            in.close();
            
            // Add 3 Strings to CPU info
            // total ram
            // swap space
            // os version
            systemInfo = new String[cpu.size() + 3];
            int i = 0;
            for(; i < cpu.size(); i++) {
            	systemInfo[i] = "CPU : " + (int)(i+1) + (String)cpu.get(i) + " Cache : " + (String)cache.get(i);
            }
            
            in = new BufferedReader(new FileReader(OS_VERSION_FILE));
            while ((str = in.readLine()) != null) {
                systemInfo[i] = str;
            }
            in.close();   
            
            in = new BufferedReader(new FileReader(MEM_INFO_FILE));
            while ((str = in.readLine()) != null) {
            	if(str.startsWith("MemTotal:")) {
            		i++;
                	systemInfo[i] = str;            		
            	}       
            	if(str.startsWith("SwapTotal:")) {
            		i++;
                	systemInfo[i] = str;            		
            	}              	
            }
            in.close();             
        } 
        catch (Exception e) {
         // Ignore.
        	systemInfo = new String[1];
        	systemInfo[0] = "This plattform is not supported by this MBean";
        }	
    }    
	
	public String[] getSystemInfo() {
		return systemInfo;
	}

	public long getMemoryFree() {
		synchronized (updateMemLock) {
			return memory[MEM_FREE];
		}			
	}
	public long getMemoryBuffered() {
		synchronized (updateMemLock) {
			return memory[MEM_BUFFED];
		}		
	}
	public long getMemoryCached() {
		synchronized (updateMemLock) {
			return memory[MEM_CACHED];
		}			
	}
	public long getMemorySwapped() {
		synchronized (updateMemLock) {
			return memory[MEM_SWAPPED];
		}	
	}

	public double getLoadAverage1Min() {
		synchronized (updateLoadLock) {
			return loadAvg[LOAD_AVG_1_MIN];
		}		
	}
	
	public double getLoadAverage5Min() {
		synchronized (updateLoadLock) {
			return loadAvg[LOAD_AVG_5_MIN];
		}		
	}
	
	public double getLoadAverage15Min() {
		synchronized (updateLoadLock) {
			return loadAvg[LOAD_AVG_15_MIN];
		}		
	}

	private double getCPUPercent(int index) {
		synchronized (updateCPULock) {
			long diff = cpuJiffies[index] - lastCpuJiffies[index];
			if(0 != diff) {
				long totDiff = cpuJiffies[CPU_TOTAL] - lastCpuJiffies[CPU_TOTAL];
				
				if(totDiff >= 0) {
					return diff *100/totDiff;
				}
			}			
			return 0;
		}			
	}		

	public double getCPUUser() {
		return getCPUPercent(CPU_USER);
	}

	public double getCPUUserNice() {
		return getCPUPercent(CPU_USER_NICE);		
	}
	public double getCPUSystem() {
		return getCPUPercent(CPU_SYSTEM);		
	}
	
	public double getCPUIdle() {
		return getCPUPercent(CPU_IDLE);			
	}
	
	public double getCPUIOWait() {
		return getCPUPercent(CPU_IO_WAIT);		
	}
	public double getCPUIRQ() {
		return getCPUPercent(CPU_IRQ);			
	}
	public double getCPUSoftIRQ() {
		return getCPUPercent(CPU_SOFT_IRQ);			
	}
	
	/*public long getPokerAllLoggedIn() {
		return SecurityManager.getInstance().getGlobalLoggedInCount();
	}
	public long getPokerLocallyLoggedIn() {
		return SecurityManager.getInstance().getLocallyLoggedInCount();
	}
	public long getPokerAllSeated() {
		return PlayerTableHandlerManager.getInstance().countGloballyActivePlayers();
	}
	public long getPokerLocallySeated() {
		return PlayerTableHandlerManager.getInstance().countLocallyActivePlayers();
	}
	public long getPokerLocalTables() {
		if(!serverStarted) {
			return 0;
		}
		else {
			return PokerModuleManager.getInstance().getLocalModuleCount();
		}
	}
	public long getPokerHandsPerHour() {
		if(!serverStarted) {
			return 0;
		}
		else {
			return pokerServer.getHandPerHourCounter().getLastHandCount();
		}
	}*/

}
