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

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.api.util.UnmodifiableSet;

import java.util.UUID;

public class MockScheduler implements Scheduler<MttAction> {

    public void cancelScheduledAction(UUID id) {
        // TODO Auto-generated method stub

    }

    public void cancelAllScheduledActions() {
        // TODO Auto-generated method stub

    }

    public UnmodifiableSet<UUID> getAllScheduledGameActions() {
        // TODO Auto-generated method stub
        return null;
    }

    public Action getScheduledGameAction(UUID id) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getScheduledGameActionDelay(UUID id) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hasScheduledGameAction(UUID id) {
        // TODO Auto-generated method stub
        return false;
    }

    public UUID scheduleAction(MttAction action, long delay) {
        // TODO Auto-generated method stub
        return null;
    }

}
