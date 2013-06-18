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

package com.cubeia.games.poker.tournament.messages;

import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingProfile;

import java.io.Serializable;

public class TournamentTableSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private TimingProfile timingProfile;

    private final BetStrategyType betStrategyType;

    public TournamentTableSettings(TimingProfile timingProfile, BetStrategyType betStrategyType) {
        this.timingProfile = timingProfile;
        this.betStrategyType = betStrategyType;
    }

    public BetStrategyType getBetStrategyType() {
        return betStrategyType;
    }

    public TimingProfile getTimingProfile() {
        return timingProfile;
    }

}
