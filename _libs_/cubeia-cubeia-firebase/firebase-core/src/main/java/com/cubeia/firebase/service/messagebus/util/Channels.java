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
package com.cubeia.firebase.service.messagebus.util;

import com.cubeia.firebase.service.messagebus.Channel;

/**
 * Static utility method for working with channels.
 * 
 * @author Larsan
 */
public class Channels {

	private Channels() { }
	
	/**
	 * @param channels Channels to get ids from, may be null
	 * @return The channel ids, or null if the channels are null
	 */
	public static int[] toIds(Channel[] channels) {
		if(channels == null) return null;
		int[] arr = new int[channels.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = channels[i].getId();
		}
		return arr;
	}
	
	
	/**
	 * @param channels Channel ids, may be null
	 * @param names Channel names, may be null only if channel ids are null
	 * @return New channel objects, or null if the channel ids are null
	 */
	public static Channel[] toChannels(int[] channels, String[] names) {
		if(channels == null) return null;
		Channel[] arr = new Channel[channels.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new Channel(channels[i], names[i]);
		}
		return arr;
	}

	
	/**
	 * @param channels Channels to get names from, may be null
	 * @return The channel names, or null if the channels are null
	 */
	public static String[] toNames(Channel[] channels) {
		if(channels == null) return null;
		String[] arr = new String[channels.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = channels[i].getName();
		}
		return arr;
	}
}
