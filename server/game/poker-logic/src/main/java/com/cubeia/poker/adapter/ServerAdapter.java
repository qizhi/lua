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

package com.cubeia.poker.adapter;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.ExposeCardsHolder;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.model.BlindsLevel;
import com.cubeia.poker.model.GameStateSnapshot;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.tournament.RoundReport;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface ServerAdapter {

    /**
     * Checks if the system is down.
     *
     * @return true if the system is down, false otherwise.
     */
    boolean isSystemShutDown();

    void scheduleTimeout(long millis);

    void requestAction(ActionRequest request);

    /**
     * Requests multiple actions sharing the same sequence number and timeout.
     *
     */
    void requestMultipleActions(Collection<ActionRequest> requests);


    void notifyCommunityCards(List<Card> cards);

    /**
     * Notify all players who is dealer.
     *
     */
    void notifyDealerButton(int seatId);

    /**
     * Sends the private cards to the given player and notify
     * all other players with hidden cards.
     *
     */
    void notifyPrivateCards(int playerId, List<Card> cards);

    /**
     * Notify the user of his best possible hand using both pocket (hidden and exposed) and community cards.
     *
     * @param playerId    player id
     * @param handType    hand type classification
     * @param cardsInHand cards used in best hand
     * @param publicHand  if the bestHand should be broadcast to all players or just the owner
     */
    void notifyBestHand(int playerId, HandType handType, List<Card> cardsInHand, boolean publicHand);

    /**
     * Sends the private cards to the given player and notify
     * all other players with exposed cards.
     *
     */
    void notifyPrivateExposedCards(int playerId, List<Card> cards);


    /**
     * A new hand is about to start.
     *
     * @throws SystemShutdownException If the system is shutting down and the table should close
     */
    void notifyNewHand() throws SystemShutdownException;

    /**
     * Notify about market references.
     * If any reference is null then it is replaced by a minus sign.
     *
     */
    void notifyExternalSessionReferenceInfo(int playerId, String externalTableReference, String externalTableSessionReference);

    void exposePrivateCards(ExposeCardsHolder holder);

    /**
     * Notifies that the hand has ended.
     *
     * @param handResult    Summary of the results or null if hand was cancelled
     * @param handEndStatus the way the hand ended, for example normal or canceled
     * @param tournamentTable indicates if the hand was part of a tournament
     */
    void notifyHandEnd(HandResult handResult, HandEndStatus handEndStatus, boolean tournamentTable);

    /**
     * Notify all players about an updated player balance.
     *
     */
    void notifyPlayerBalance(PokerPlayer player);

    /**
     * Called after an action from the player has been successfully
     * dealt with.
     *
     * @param pokerPlayer the player who performed an action
     * @param action,     not null.
     */
    void notifyActionPerformed(PokerAction action, PokerPlayer pokerPlayer);

    /**
     * Reports the end of a round to a tournament coordinator.
     *
     * @param report, a report value object. Not null.
     */
    void reportTournamentRound(RoundReport report);

    /**
     * Sends a rebuy response to the tournament coordinator.
     *
     */
    void sendRebuyResponseToTournament(int playerId, boolean response, BigDecimal chipsAtHandFinish);

    /**
     * Sends a request for performing an add-on to the tournament coordinator.
     */
    void sendAddOnRequestToTournament(int playerId);

    /**
     * Remove all players in state LEAVING or DISCONNECTED
     */
    void cleanupPlayers(SitoutCalculator sitoutCalculator);

    /**
     * Notifies the client about pot updates by sending the post and pot transitions.
     *
     * @param pots           updated post
     * @param potTransitions pot transitions
     */
    void notifyPotUpdates(Collection<Pot> pots, Collection<PotTransition> potTransitions);

    void notifyPlayerStatusChanged(int playerId, PokerPlayerStatus status, boolean inCurrentHand, boolean away,
                                   boolean sitOutNextHand);

    /**
     * Send information if the deck in use.
     *
     * @param size    total number of cards in deck
     * @param rankLow lowest used rank in deck, this is normally TWO, but if the deck is stripped
     *                it might be different.
     */
    void notifyDeckInfo(int size, Rank rankLow);

    void notifyNewRound();

    /**
     * Send information to client about buy-ins.
     *
     */
    void notifyBuyInInfo(int playerId, boolean mandatoryBuyin);

    /**
     * Notify the client of the current total rake ant pot sizes.
     *
     * @param rakeInfoContainer rake info
     */
    void notifyRakeInfo(RakeInfoContainer rakeInfoContainer);

    public void unseatPlayer(int playerId, boolean setAsWatcher);

    /**
     * Notify that a bet was taken back from bet stack to balance since it was uncalled
     *
     */
    void notifyTakeBackUncalledBet(int playerId, BigDecimal amount);

    /**
     * Notify that the player will be able to do this actions later when it is the players turn
     *
     */
    void notifyFutureAllowedActions(PokerPlayer player, List<PokerActionType> optionList, BigDecimal callAmount, BigDecimal minBet);

    /**
     * Request buy in:s for the given players that has {@link PokerPlayer#getRequestedBuyInAmount()} > 0.
     *
     */
    void performPendingBuyIns(Collection<PokerPlayer> players);

    /**
     * Send out a player status for a new hand starting
     *
     */
    void notifyHandStartPlayerStatus(int playerId, PokerPlayerStatus status, boolean away, boolean sitOutNextHand);

    void notifyDisconnected(int playerId);

    /**
     * Returns the identifier of the hand that was provided by the backend.
     *
     * @return backend integration hand id
     */
    String getIntegrationHandId();
    
    Random getSystemRNG();

    void notifyWaitingToStartBreak();

    void notifyWaitingForPlayers();

    void notifyTournamentDestroyed();

    void notifyBlindsLevelUpdated(BlindsLevel level);

    void notifyRebuyOffer(Collection<Integer> players, String rebuyCost, String rebuyChips);

    void notifyAddOnsAvailable(String rebuyCost, String rebuyChips);

    void notifyRebuyPerformed(int playerId);

    void notifyAddOnPerformed(int playerId);

    void notifyAddOnPeriodClosed();

    void sendGameStateTo(GameStateSnapshot snapshot, int playerId);

}
