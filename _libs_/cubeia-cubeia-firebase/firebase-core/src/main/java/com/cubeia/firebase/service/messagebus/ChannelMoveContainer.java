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


public class ChannelMoveContainer {

	private Partition fromPartition;
	private Partition toPartition;
	
	private Channel[] channels;
	
	
	public ChannelMoveContainer(Partition fromPartition, Partition toPartition, Channel[] channels) {
		this.fromPartition = fromPartition;
		this.toPartition = toPartition;
		this.channels = channels;
	}
	

	public Partition getFromPartition() {
		return fromPartition;
	}

	public void setFromPartition(Partition fromPartition) {
		this.fromPartition = fromPartition;
	}

	public Partition getToPartition() {
		return toPartition;
	}

	public void setToPartition(Partition toPartition) {
		this.toPartition = toPartition;
	}

	public Channel[] getChannels() {
		return channels;
	}

	public void setChannels(Channel[] channels) {
		this.channels = channels;
	}

	@Override
	//FIXME: Use type as well as id!!
	public boolean equals(Object obj) {
		if(!(obj instanceof ChannelMoveContainer)) return false;
		else return 
		((ChannelMoveContainer)obj).toPartition.equals(toPartition) &&
		((ChannelMoveContainer)obj).fromPartition.equals(fromPartition);
	}
	
	@Override
	//FIXME: Use type as well as id!!
	public int hashCode() {
		return (fromPartition.getId() + fromPartition.getId()).hashCode();
	}
}
