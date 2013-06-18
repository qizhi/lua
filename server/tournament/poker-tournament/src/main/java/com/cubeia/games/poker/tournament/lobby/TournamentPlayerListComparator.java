/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.lobby;

import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;

import java.math.BigDecimal;
import java.util.Comparator;

import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.OUT;
import static com.cubeia.firebase.api.mtt.model.MttPlayerStatus.PLAYING;

/**
 * Sorts players in an order suitable for presenting in the tournament lobby.
 *
 * The player with the most chips is highest and players who have no chips left are sorted
 * by their position in the tournament.
 *
 * Finally, before the tournament starts, players will be sorted alphabetically by their screen name.
 *
 * This comparator will sort in "natural" order, meaning that the "lowest" position is first, so to show it
 * as a top list use Collections.sort(unorderedList, Collections.reverseOrder(new TournamentPlayerListComparator()));
 */
public class TournamentPlayerListComparator implements Comparator<MttPlayer> {

    private PokerTournamentState state;

    public TournamentPlayerListComparator(PokerTournamentState state) {
        this.state = state;
    }

    /**
     * Compares two tournament players and sorts them like this:
     *
     * - First by chip stack (if status is Playing)
     * - Then by position (if status is Out)
     * - Then by name
     *
     */
    @Override
    public int compare(MttPlayer first, MttPlayer second) {
        int result;
        if (bothPlaying(first, second)) {
            // First sort by chip stack (if both players are still in the tournament).
            // (Return negative number if first has less chips than second)
            result = getStackSizeFor(first).compareTo(getStackSizeFor(second));
        } else if (bothOut(first, second)) {
            // Then sort by position, if both players are out.
            // (Return negative number if first has a worse position (higher number) than second)
            result = second.getPosition() - first.getPosition();
        } else if (first.getStatus() == PLAYING && second.getStatus() != PLAYING) {
            // A player who is not playing is always "below" a player who is.
            result = 1;
        } else if (second.getStatus() == PLAYING && first.getStatus() != PLAYING) {
            // And vice versa.
            result = -1;
        } else {
            // Sort by name.
            result = second.getScreenname().toLowerCase().compareTo(first.getScreenname().toLowerCase());
        }
        return result;
    }

    private BigDecimal getStackSizeFor(MttPlayer player) {
        return state.getPlayerBalance(player.getPlayerId());
    }

    private boolean bothPlaying(MttPlayer first, MttPlayer second) {
        return first.getStatus() == PLAYING && second.getStatus() == PLAYING;
    }

    private boolean bothOut(MttPlayer first, MttPlayer second) {
        return first.getStatus() == OUT && second.getStatus() == OUT;
    }

}
