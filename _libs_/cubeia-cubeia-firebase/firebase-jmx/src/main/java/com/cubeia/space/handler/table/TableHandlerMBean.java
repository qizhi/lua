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
package com.cubeia.space.handler.table;

/**
 * This is a small diagnostics interface for the distribution 
 * of tables within Firebase. This interface is only used if
 * the statistics level in the server is set to PROFILING.
 * 
 * @author Larsan
 */
public interface TableHandlerMBean {
	
	/**
	 * After events are processed in Firebase their state is 
	 * replicated across the cluster. This is may be done in a 
	 * transaction, this method returns the average commit time
	 * in micro seconds. If the cache does not use transaction this
	 * time represents the time it takes to write data to the
	 * socket, ie a "semi-transaction commit". 
	 * 
	 * <p>This value is only calculated if the statistics level in the 
	 * server is set to PROFILING.
	 * 
	 * @return The average commit time in micro seconds, or -1
	 */
	public double getAverageCommitTimeMicros();
	
	/**
	 * This method returns the average game state size in bytes. The size
	 * is calculated from the serialized state.
	 * 
	 * <p>This value is only calculated if the statistics level in the 
	 * server is set to PROFILING.
	 * 
	 * @return The average game state size in bytes, or -1
	 */
	public double getAverageGameStateObjectSize();
	
}
