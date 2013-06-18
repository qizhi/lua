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
package com.cubeia.firebase.service.messagebus;

/**
 * This interface can be used to listen for redistributions. The
 * difference between the methods on this interface and those on
 * the {@link MBusListener} is that these methods are called only
 * locally, ie. in the VM where the {@link MBusRedistributor} was 
 * called, and after not only the partition was added but also all
 * channels have been balanced.
 * 
 * @author Larsan
 */
public interface RedistributionListener {

	/**
	 * @param p The new partition, never null
	 */
	public void partitionCreated(Partition p);
	
	/**
	 * @param p The dropped partition, never null
	 */
	public void partitionDropped(Partition p);
	
}
