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

import java.io.Serializable;

import com.cubeia.firebase.api.action.Action;

/**
 * A simple "struct" like bean for pairing a scheduled action
 * with the initial delay it was scheduled for.
 * 
 * @author Larsan
 */
public final class ScheduledAction<T extends Action> implements Serializable {
	
	private static final long serialVersionUID = 4258686126179884262L;
	
	/**
	 * The initial delay in millis.
	 */
	public final long delay;
	
	/**
	 * The scheduled action.
	 */
	private T action;
	
	/**
	 * @param delay Initial delay in millis
	 * @param action Scheduled action
	 */
	public ScheduledAction(long delay, T action) {
		this.delay = delay;
		this.setAction(action);
	}

    private void setAction(T action) {
        this.action = action;
    }

    public T getAction() {
        return action;
    }
	
}
