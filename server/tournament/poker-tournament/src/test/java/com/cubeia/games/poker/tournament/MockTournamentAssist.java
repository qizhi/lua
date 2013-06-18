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

package com.cubeia.games.poker.tournament;

import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;
import com.cubeia.firebase.guice.tournament.TournamentAssist;

public class MockTournamentAssist extends MTTSupport implements TournamentAssist {

    @Override
    public void process(MttRoundReportAction action, MttInstance mttInstance) {

    }

    @Override
    public void process(MttTablesCreatedAction action, MttInstance instance) {

    }

    @Override
    public void process(MttObjectAction action, MttInstance instance) {

    }

    @Override
    public void tournamentCreated(MttInstance mttInstance) {

    }

    @Override
    public void tournamentDestroyed(MttInstance mttInstance) {

    }

    @Override
    public PlayerListener getPlayerListener(MTTStateSupport state) {
        return null;
    }

    @Override
    public PlayerInterceptor getPlayerInterceptor(MTTStateSupport state) {
        return null;
    }
}
