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

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.game.GameNotifier;

import java.util.ArrayList;
import java.util.Collection;

public class MockNotifier implements GameNotifier {

    private Collection<GameAction> cachedActions = new ArrayList<GameAction>();

    public void clear() {
        cachedActions.clear();
    }

    public Collection<GameAction> getActions() {
        return cachedActions;
    }

    public void broadcast(SystemMessageAction arg0) {

    }

    public void notifyAllPlayers(GameAction action) {
        cachedActions.add(action);
    }

    public void notifyAllPlayers(Collection<? extends GameAction> actions) {
        cachedActions.addAll(actions);
    }

    public void notifyAllPlayers(GameAction action, boolean arg1) {
        cachedActions.add(action);
    }

    public void notifyAllPlayers(Collection<? extends GameAction> actions, boolean arg1) {
        cachedActions.addAll(actions);
    }

    public void notifyAllPlayersExceptOne(GameAction action, int arg1) {
        cachedActions.add(action);
    }

    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> actions, int arg1) {
        cachedActions.addAll(actions);
    }

    public void notifyAllPlayersExceptOne(GameAction action, int arg1, boolean arg2) {
        cachedActions.add(action);
    }

    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> actions, int arg1, boolean arg2) {
        cachedActions.addAll(actions);
    }

    public void notifyPlayer(int arg0, GameAction action) {
        cachedActions.add(action);
    }

    public void notifyPlayer(int arg0, Collection<? extends GameAction> actions) {
        cachedActions.addAll(actions);
    }

    public void sendToClient(int arg0, GameAction action) {
        cachedActions.add(action);
    }

    public void sendToClient(int arg0, Collection<? extends GameAction> actions) {
        cachedActions.addAll(actions);
    }

}
