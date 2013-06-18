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

import static com.cubeia.network.example.rps.PlayToken.PAPER;
import static com.cubeia.network.example.rps.PlayToken.ROCK;
import static com.cubeia.network.example.rps.PlayToken.SCISSORS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class TokenTest {

	@Test
	public void testCompare() {
		assertThat(PAPER.winsOver(ROCK) == 1, is(true));
		assertThat(ROCK.winsOver(SCISSORS) == 1, is(true));
		assertThat(SCISSORS.winsOver(PAPER) == 1, is(true));
		
		assertThat(ROCK.winsOver(PAPER) == -1, is(true));
		assertThat(SCISSORS.winsOver(ROCK) == -1, is(true));
		assertThat(PAPER.winsOver(SCISSORS) == -1, is(true));

		assertThat(ROCK.winsOver(ROCK) == 0, is(true));
		assertThat(SCISSORS.winsOver(SCISSORS) == 0, is(true));
		assertThat(PAPER.winsOver(PAPER) == 0, is(true));
	}
	
}
