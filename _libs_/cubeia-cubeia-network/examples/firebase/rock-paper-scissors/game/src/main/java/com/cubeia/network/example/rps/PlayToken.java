/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.network.example.rps;

import static java.lang.Integer.signum;
import static java.lang.Math.abs;

public enum PlayToken {
	ROCK, 
	PAPER, 
	SCISSORS;
	
	/**
	 * Returns 0 it the tokens are the same.
	 * Returns 1 if this token wins over the given one.
	 * Returns -1 if this token loses to the given one.
	 * @param token token to compare to
	 * @return -1, 0, 1
	 */
	public int winsOver(PlayToken token) {
		int diff = this.ordinal() - token.ordinal();
		return abs(diff) < 2 ? diff : -signum(diff);
	}
}