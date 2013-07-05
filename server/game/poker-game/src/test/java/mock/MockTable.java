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

package mock;

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.*;

public class MockTable implements Table {

    private TableGameState state = new MockTableGameState();

    private GameNotifier notifier = new MockNotifier();

    private TableScheduler scheduler = new MockScheduler();

    public GameNotifier getNotifier() {
        return notifier;
    }

    public TableGameState getGameState() {
        return state;
    }

    public TableScheduler getScheduler() {
        return scheduler;
    }

    public ExtendedDetailsProvider getExtendedDetailsProvider() {
        return null;
    }

    public TableInterceptor getInterceptor() {
        return null;
    }

    public TableListener getListener() {
        return null;
    }

    public TableMetaData getMetaData() {
        return null;
    }

    public TablePlayerSet getPlayerSet() {
        return null;
    }

    public TournamentNotifier getTournamentNotifier() {
        return null;
    }

    public TableWatcherSet getWatcherSet() {
        return null;
    }

    public int getId() {
        return 0;
    }

    public LobbyTableAttributeAccessor getAttributeAccessor() {
        return null;
    }

}
