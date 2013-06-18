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

import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;

import java.util.Collection;

public class MockTableCreator implements MttTableCreator {

    private PokerTournamentProcessor tournamentProcessor;

    private MttInstance instance;

    private PokerTournamentUtil util = new PokerTournamentUtil();

    public MockTableCreator(PokerTournamentProcessor tournamentProcessor, MttInstance instance) {
        this.tournamentProcessor = tournamentProcessor;
        this.instance = instance;
    }

    public void createTables(int gameId, int mttId, int tableCount, int seats, String baseName, Object attachment) {
        MttTablesCreatedAction action = new MttTablesCreatedAction(mttId);
        for (int i = 0; i < tableCount; i++) {
            action.addTable(i);
        }
        MTTStateSupport state = util.getStateSupport(instance);
        state.getTables().addAll(action.getTables());
        tournamentProcessor.process(action, instance);
    }

    public void removeTables(int gameId, int mttId, Collection<Integer> tableIds) {
        removeTables(tableIds);
    }

    private void removeTables(Collection<Integer> tableIds) {
        MTTStateSupport state = util.getStateSupport(instance);
        for (Integer tableId : tableIds) {
            state.getTables().remove(tableId);
        }
    }

    public void removeTables(int gameId, int mttId, Collection<Integer> unusedTables, long delayMs) {
        removeTables(unusedTables);
    }

}
