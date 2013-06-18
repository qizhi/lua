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

package com.cubeia.poker.handhistory.api;

import com.cubeia.firebase.api.service.Contract;

import java.util.List;

/**
 * Service contract for collecting hand history events. The service
 * is assumed to keep hand state until the hand is finished and is
 * responsible for persisting the hand when it is stopped.
 *
 * @author Lars J. Nilsson
 */
public interface HandHistoryCollectorService extends Contract {

    /**
     * @param id    Hand id, must not be null
     * @param seats Players in hand, must not be null
     */
    public void startHand(String id, Table table, List<Player> seats);

    /**
     * @param tableId Firebase table id
     * @param event   Event to report, must not be null
     */
    public void reportEvent(int tableId, HandHistoryEvent event);

    /**
     * @param tableId  Firebase table id
     * @param deckInfo Deck information, must not be null
     */
    public void reportDeckInfo(int tableId, DeckInfo deckInfo);

    /**
     * @param tableId Firebase table id
     * @param res     Hand results, must not be null
     */
    public void reportResults(int tableId, Results res);

    /**
     * @param tableId Firebase table id
     */
    public void stopHand(int tableId);

    /**
     * @param tableId Firebase table id
     */
    public void cancelHand(int tableId);

}
