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

package com.cubeia.poker.blinds;

import org.slf4j.Logger;

import java.io.Serializable;
import java.util.*;

import static com.cubeia.poker.blinds.utils.PokerUtils.*;

/**
 * Used for calculating where the dealer button, small blind and big blind should be.
 * <p/>
 * Also calculates who should post entry bets and who should be marked as having missed blinds.
 *
 * @author viktor
 */
public class BlindsCalculator implements Serializable {

    /**
     * The blinds info from last hand.
     */
    private BlindsInfo lastHandsBlinds;

    /**
     * The blinds info for the new hand.
     */
    private BlindsInfo blindsInfo = new BlindsInfo();

    /**
     * Used for fetching a random player to get the dealer button.
     */
    private final RandomSeatProvider randomSeatProvider;

    /**
     * Contains all players at the table. Used for calculating missed blinds.
     */
    private Collection<? extends BlindsPlayer> players;

    /**
     * Maps seat ids to players. Only contains seated players.
     */
    private SortedMap<Integer, BlindsPlayer> seatedPlayers;

    /**
     * List of players who should pay the entry bet.
     */
    private final Queue<EntryBetter> entryBetters = new LinkedList<EntryBetter>();

    /**
     * List of players who missed blinds.
     */
    private final List<MissedBlind> missedBlinds = new ArrayList<MissedBlind>();

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BlindsCalculator.class);

    /**
     * Constructor.
     */
    public BlindsCalculator() {
        this(new FeedableSeatProvider());
    }

    /**
     * Constructor.
     *
     * @param randomSeatProvider the random seat provider to use
     */
    public BlindsCalculator(final RandomSeatProvider randomSeatProvider) {
        this.randomSeatProvider = randomSeatProvider;
    }

    /**
     * Initializes the blinds for a new hand.
     *
     * @param lastHandsBlinds The blinds info from last hand, cannot be null.
     * @param players         A list of players at the table, cannot be null.
     *                        Should contain all players, including players sitting out.
     * @param isTournamentBlinds Boolean to indicate if this is tournament blinds.
     * @return the {@link BlindsInfo} for the new hand, or null if no hand could be started
     */
    public BlindsInfo initializeBlinds(final BlindsInfo lastHandsBlinds, final Collection<? extends BlindsPlayer> players, boolean isTournamentBlinds) {
        this.lastHandsBlinds = lastHandsBlinds;
        this.players = players;

        clearLists();
        initPlayerMap(isTournamentBlinds);
        if (enoughPlayers()) {
            initBlinds();
            markMissedBlinds();
            return blindsInfo;
        } else {
            return null;
        }
    }

    public void removePlayerFromCurrentHand(BlindsPlayer player) {
        seatedPlayers.remove(player.getSeatId());
    }

    private void clearLists() {
        missedBlinds.clear();
        entryBetters.clear();
    }

    /**
     * Calculates which players should post the entry bet, and what the should post.
     */
    private void calculateEntryBets() {
        // Players between the big blind and the dealer button are eligible to pay an entry bet.
        final int dealerSeatId = blindsInfo.getDealerSeatId();
        final int bigBlindSeatId = blindsInfo.getBigBlindSeatId();
        final int smallBlindSeatId = blindsInfo.getSmallBlindSeatId();

        final BlindsPlayer nextPlayer = getElementAfter(bigBlindSeatId, seatedPlayers);
        final List<BlindsPlayer> playerList = unwrapList(seatedPlayers, nextPlayer.getSeatId());
        for (BlindsPlayer player : playerList) {
            log.debug("Checking if player " + player.getId() + " should post an entry bet.");
            if (!player.hasPostedEntryBet()) {
                // A player on the dealer button cannot post entry bet.
                final boolean onDealer = player.getSeatId() == dealerSeatId;
                // Nor can a player between the dealer and the big blind.
                final boolean betweenDealerAndBig = isBetween(player.getSeatId(), dealerSeatId, bigBlindSeatId);
                // Nor can a player between the dealer and the small blind.
                final boolean betweenDealerAndSmall = isBetween(player.getSeatId(), dealerSeatId, smallBlindSeatId);

                if (player.getMissedBlindsStatus() == MissedBlindsStatus.NO_MISSED_BLINDS) {
                    /*
                     * This should not happen, a player who has not missed any blinds should still
                     * be considered as having paid the entry bet.
                     */
                    log("WARN: Player with id " + player.getId() + " has not missed any blinds, but has not posted the entry bet.");
                } else if (!onDealer && !betweenDealerAndBig && !betweenDealerAndSmall) {
                    log.debug("Adding entry better " + player.getId());
                    entryBetters.add(new EntryBetter(player, getEntryBetType(player)));
                }
            }
        }
    }

    /**
     * Gets a queue of players who should pay the entry bet. The queue will be ordered
     * in the order the players should be asked.
     *
     * @param dealerSeatId the seat id of the dealer
     * @param bigBlindSeatId the seat id of the player who actually posted the big blind
     * @return the queue of players who should pay the entry bet
     */
    public Queue<EntryBetter> getEntryBetters(final int dealerSeatId, final int smallBlindSeatId, final int bigBlindSeatId) {
        Queue<EntryBetter> remainingEntryBetters = new LinkedList<EntryBetter>();
        /*
         * We will use the queue of entry betters calculated at initialization time, but we'll remove players
         * between the dealer and the bb. Note that the dealer might have ended up on the dealer button if
         * everyone rejected the bb, so we also remove players between the dealer and the sb.
         *
         * Also, a player who was up for posting an entry bet might have actually posted the bb if the original
         * bb declined, so we remove potential entry betters who have already posted the entry bet.
         */
        for (EntryBetter entryBetter : entryBetters) {
            final boolean betweenDealerAndBig = isBetween(entryBetter.getPlayer().getSeatId(), dealerSeatId, bigBlindSeatId);
            final boolean betweenDealerAndSmall = isBetween(entryBetter.getPlayer().getSeatId(), dealerSeatId, smallBlindSeatId);
            final boolean hasPostedEntryBet = entryBetter.getPlayer().hasPostedEntryBet();
            if (!betweenDealerAndBig && !betweenDealerAndSmall && !hasPostedEntryBet) {
                remainingEntryBetters.add(entryBetter);
            }
        }
        log.debug("Remaining entry betters: " + remainingEntryBetters);
        return remainingEntryBetters;
    }

    private EntryBetType getEntryBetType(final BlindsPlayer player) {
        EntryBetType result = null;
        switch (player.getMissedBlindsStatus()) {
            case MISSED_BIG_BLIND_AND_SMALL_BLIND:
                result = EntryBetType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
                break;
            case MISSED_SMALL_BLIND:
                result = EntryBetType.DEAD_SMALL_BLIND;
                break;
            case NOT_ENTERED_YET:
                result = EntryBetType.BIG_BLIND;
                break;
        }
        return result;
    }

    /**
     * Checks if we have enough players to start a hand.
     */
    private boolean enoughPlayers() {
        return (seatedPlayers.size() > 1);
    }

    /**
     * Initializes the player map, by mapping players to their seat ids.
     *
     * @param tournamentBlinds a boolean to indicate if this is tournament blinds
     */
    private void initPlayerMap(boolean tournamentBlinds) {
        seatedPlayers = new TreeMap<Integer, BlindsPlayer>();
        for (BlindsPlayer player : players) {
            if (tournamentBlinds) {
                // Tournament players have never missed blinds and are always sitting in.
                player.setHasPostedEntryBet(true);
                seatedPlayers.put(player.getSeatId(), player);
            } else if (player.isSittingIn()) {
                seatedPlayers.put(player.getSeatId(), player);
            }
        }
    }

    private void initBlinds() {
        log.debug("Initializing blinds. Last hand's blinds: " + lastHandsBlinds);
        if (firstHandAtTable()) {
            initFirstHandAtTable();
        } else if (onlyOneEnteredPlayer()) {
            initHandWhenOnlyOneEnteredPlayer();
        } else if (lastHandCanceled()) {
            initHandWhenLastHandCanceled();
        } else {
            initNonFirstHandAtTable();
            calculateEntryBets();
        }
    }

    private void initHandWhenLastHandCanceled() {
        if (dealerStillSeated()) {
            log("Initializing hand last hand was canceled and dealer is still seated.");
            final BlindsPlayer dealer = getPlayerInSeat(lastHandsBlinds.getDealerSeatId());
            initWithDealer(dealer);
        } else {
            initFirstHandAtTable();
        }
    }

    private void initWithDealer(final BlindsPlayer dealer) {
        setDealerSeat(dealer.getSeatId());

        if (headsUp()) {
            // The entered player gets the small blind.
            setSmallBlind(dealer);

            // The other player gets the big blind.
            BlindsPlayer bigBlind = getElementAfter(dealer.getSeatId(), seatedPlayers);
            setBigBlind(bigBlind);
        } else {
            // The next player gets the small blind.
            BlindsPlayer smallBlind = getElementAfter(dealer.getSeatId(), seatedPlayers);
            setSmallBlind(smallBlind);

            // The next player gets the big blind.
            BlindsPlayer bigBlind = getElementAfter(smallBlind.getSeatId(), seatedPlayers);
            setBigBlind(bigBlind);
        }
    }

    private boolean dealerStillSeated() {
        BlindsPlayer dealer = getPlayerInSeat(lastHandsBlinds.getDealerSeatId());
        return dealer != null && dealer.isSittingIn();
    }

    private boolean lastHandCanceled() {
        return lastHandsBlinds.handCanceled();
    }

    private boolean onlyOneEnteredPlayer() {
        return countEnteredPlayers() == 1;
    }

    private void markMissedBlinds() {
        // If small blind is sitting out, mark him as having missed the small blind.
        final BlindsPlayer smallBlind = getPlayerInSeat(blindsInfo.getSmallBlindSeatId());
        if (smallBlind != null && !smallBlind.isSittingIn() && smallBlind.getId() == lastHandsBlinds.getBigBlindPlayerId()) {
            blindsInfo.setSmallBlindPlayerId(-1);
            addMissedBlind(smallBlind, MissedBlindsStatus.MISSED_SMALL_BLIND);
        }

        /*
         * All sitting out players between the old dealer button position and the
         * new dealer button position should be marked as having missed big+small.
         */
        final int lastDealerSeatId = lastHandsBlinds.getDealerSeatId();
        final int newDealerSeatId = blindsInfo.getDealerSeatId();
        for (final BlindsPlayer player : players) {
            if (player.isSittingIn()) {
                continue;
            }

            final boolean betweenOldAndNewDealerButton = isBetween(player.getSeatId(), lastDealerSeatId, newDealerSeatId);
            final boolean betweenSmallAndBigBlind = isBetween(player.getSeatId(), blindsInfo.getSmallBlindSeatId(), blindsInfo.getBigBlindSeatId());
            if (betweenOldAndNewDealerButton || betweenSmallAndBigBlind) {
                addMissedBlind(player, MissedBlindsStatus.MISSED_BIG_BLIND_AND_SMALL_BLIND);
            }
        }
    }

    private void addMissedBlind(final BlindsPlayer player, final MissedBlindsStatus status) {
        // If the player has not entered yet, don't mark him as having missed any blinds.
        if (player.getMissedBlindsStatus() != MissedBlindsStatus.NOT_ENTERED_YET) {
            log("Marking player in seat " + player.getSeatId() + " as having missed blinds status: " + status);
            missedBlinds.add(new MissedBlind(player, status));
        }
    }

    /**
     * Gets the player in the given seat, or null if there is no player in that seat.
     *
     * @param seatId the id of the seat for which to get the player
     * @return the player in the given seat or null if there is no player in that seat
     */
    private BlindsPlayer getPlayerInSeat(final int seatId) {
        BlindsPlayer result = null;
        for (final BlindsPlayer player : players) {
            if (player.getSeatId() == seatId) {
                result = player;
            }
        }
        return result;
    }

    /**
     * Initializes a hand when there is only one entered player.
     * This should mean that the last hand was canceled or that game has been on pause since the last hand finished.
     * The rule is that the entered player does _not_ have to pay the big blind.
     */
    private void initHandWhenOnlyOneEnteredPlayer() {
        log("Initializing hand when only one entered player.");
        final BlindsPlayer enteredPlayer = getFirstEnteredPlayer();
        initWithDealer(enteredPlayer);
    }

    /**
     * Gets the first player who has posted the entry bet.
     *
     * @return the first player who has posted the entry bet, or null if no player has posted the entry bet
     */
    private BlindsPlayer getFirstEnteredPlayer() {
        BlindsPlayer result = null;

        for (BlindsPlayer player : seatedPlayers.values()) {
            if (player.hasPostedEntryBet()) {
                result = player;
            }
        }
        return result;
    }

    private void initNonFirstHandAtTable() {
        log("Initializing non first heads up hand at table.");
        if (lastHandsBlinds.isHeadsUpLogic()) {
            if (headsUp()) {
                // Keeping heads up logic.
                initHeadsUpHand();
            } else {
                // Moving from heads up to non heads up logic.
                moveFromHeadsUpToNonHeadsUp();
            }
        } else {
            if (headsUp()) {
                // Moving from non heads up to heads up logic.
                moveFromNonHeadsUpToHeadsUp();
            } else {
                // Keeping non heads up logic.
                initNonHeadsUpHand();
            }
        }
    }

    private void initNonHeadsUpHand() {
        log("Initializing non heads up hand.");
        // Last hand's small blind gets the dealer button.
        final int dealerSeatId = lastHandsBlinds.getSmallBlindSeatId();
        setDealerSeat(dealerSeatId);

        // Last hand's big blind gets the small blind.
        final int smallBlindSeatId = lastHandsBlinds.getBigBlindSeatId();
        setSmallBlind(smallBlindSeatId, lastHandsBlinds.getBigBlindPlayerId());

        // The next player gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(smallBlindSeatId, seatedPlayers);
        setBigBlind(bigBlind);
    }

    private void moveFromHeadsUpToNonHeadsUp() {
        log("Moving from heads up to non heads up.");
        // Dealer button stays where it is.
        final int dealerSeatId = lastHandsBlinds.getDealerSeatId();
        blindsInfo.setDealerSeatId(dealerSeatId);

        // The big blind from last hand gets the small blind.
        final int smallBlindSeatId = lastHandsBlinds.getBigBlindSeatId();
        setSmallBlind(smallBlindSeatId, lastHandsBlinds.getBigBlindPlayerId());

        // The next player gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(smallBlindSeatId, seatedPlayers);
        setBigBlind(bigBlind);
    }

    private void moveFromNonHeadsUpToHeadsUp() {
        log("Moving from non heads up to heads up.");
        // The player after last hand's big blind gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(lastHandsBlinds.getBigBlindSeatId(), seatedPlayers);
        setBigBlind(bigBlind);

        // The other player gets the dealer button and the small blind.
        final BlindsPlayer smallBlind = getElementAfter(bigBlind.getSeatId(), seatedPlayers);
        setDealerSeat(smallBlind.getSeatId());
        setSmallBlind(smallBlind);
    }

    private void initHeadsUpHand() {
        log("Initializing heads up hand.");
        // The big blind from last hand gets the dealer button and small blind.
        BlindsPlayer dealer = getPlayerInSeat(lastHandsBlinds.getBigBlindSeatId());
        if (dealer == null) {
            // If the dealer is not there, find the first player after last hand's big blind.
            dealer = getElementAfter(lastHandsBlinds.getBigBlindSeatId(), seatedPlayers);
        }
        final int dealerSeatId = dealer.getSeatId();

        setDealerSeat(dealer.getSeatId());
        setSmallBlind(dealer);

        // The next player gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(dealerSeatId, seatedPlayers);
        setBigBlind(bigBlind);
    }

    private void initFirstHandAtTable() {
        log("Initializing first hand at table.");
        markAllPlayersAsHavingPostedEntryBet();
        if (headsUp()) {
            initFirstHeadsUpHand();
        } else {
            initFirstNonHeadsUpHand();
        }
    }

    private void markAllPlayersAsHavingPostedEntryBet() {
        for (BlindsPlayer player : seatedPlayers.values()) {
            player.setHasPostedEntryBet(true);
        }
    }

    private void initFirstNonHeadsUpHand() {
        log("Initializing first non heads up hand at table.");
        // Random player gets the button.
        final BlindsPlayer dealer = getRandomSeatedPlayer();
        setDealerSeat(dealer.getSeatId());

        // The next player gets the small blind.
        final BlindsPlayer smallBlind = getElementAfter(dealer.getSeatId(), seatedPlayers);
        setSmallBlind(smallBlind);

        // The next player gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(smallBlind.getSeatId(), seatedPlayers);
        setBigBlind(bigBlind);
    }

    private void setSmallBlind(BlindsPlayer smallBlind) {
        setSmallBlind(smallBlind.getSeatId(), smallBlind.getId());
    }

    private void setSmallBlind(int seatId, int playerId) {
        if (getPlayerInSeat(seatId) == null) {
            log.debug("No one is sitting in the small blind seat {}. Marking it as dead.", seatId);
            blindsInfo.setSmallBlindPlayerId(-1);
        } else if (getPlayerInSeat(seatId).getId() != playerId) {
            log.debug("There's a new player {} in the sb seat {}, marking sb as dead. ", playerId, seatId);
            blindsInfo.setSmallBlindPlayerId(-1);
        } else {
            log.debug("Small blind is on player " + playerId + " in seat " + seatId);
            blindsInfo.setSmallBlindPlayerId(playerId);
        }
        blindsInfo.setSmallBlindSeatId(seatId);
    }

    private void initFirstHeadsUpHand() {
        log("Initializing first heads up hand at table.");
        // Random player gets the button and small blind.
        final BlindsPlayer dealer = getRandomSeatedPlayer();

        setDealerSeat(dealer.getSeatId());
        setSmallBlind(dealer);

        // The other player gets the big blind.
        final BlindsPlayer bigBlind = getElementAfter(dealer.getSeatId(), seatedPlayers);
        setBigBlind(bigBlind);
    }

    private void setDealerSeat(int dealerSeat) {
        log.debug("Dealer button is on seat " + dealerSeat);
        blindsInfo.setDealerSeatId(dealerSeat);
    }

    private void setBigBlind(BlindsPlayer bigBlind) {
        log.debug("Big blind is on player " + bigBlind.getId() + " in seat " + bigBlind.getSeatId());
        blindsInfo.setBigBlindSeatId(bigBlind.getSeatId());
        blindsInfo.setBigBlindPlayerId(bigBlind.getId());
    }

    /**
     * Gets a random player from the players who are sitting in.
     *
     * @return a random player from the players who are sitting in
     */
    private BlindsPlayer getRandomSeatedPlayer() {
        final List<Integer> seatIds = getSeatIdsOfSeatedPlayers();
        return getPlayerInSeat(randomSeatProvider.getRandomSeatId(seatIds));
    }

    private List<Integer> getSeatIdsOfSeatedPlayers() {
        final List<Integer> seatIds = new ArrayList<Integer>();
        for (BlindsPlayer player : seatedPlayers.values()) {
            seatIds.add(player.getSeatId());
        }
        return seatIds;
    }

    /**
     * Checks whether this hand is heads up.
     *
     * @return <code>true</code> if this hand is heads up, <code>false</code> otherwise
     */
    private boolean headsUp() {
        return seatedPlayers.size() == 2;
    }

    /**
     * Checks whether this is the first hand at the table.
     * <p/>
     * We consider the hand to be the "first" if 1 or fewer players have payed
     * the entry bet.
     *
     * @return <code>true</code> is this is the first hand, <code>false</code> otherwise
     */
    private boolean firstHandAtTable() {
        return countEnteredPlayers() == 0 || !lastHandsBlinds.isDefined();
    }

    /**
     * Counts the number of players who have payed the entry bet.
     *
     * @return the number of players who have payed the entry bet
     */
    private int countEnteredPlayers() {
        int enteredPlayers = 0;
        for (final BlindsPlayer player : seatedPlayers.values()) {
            if (player.hasPostedEntryBet()) {
                enteredPlayers++;
            }
        }
        log.debug("Entered players: " + enteredPlayers);
        return enteredPlayers;
    }

    /**
     * Gets the blinds info for the new hand.
     *
     * @return the blinds info for the new hand, never null
     */
    public BlindsInfo getBlindsInfo() {
        return blindsInfo;
    }

    /**
     * Gets a list of {@link MissedBlind}s, representing who should be marked as having missed blinds.
     *
     * @return a list of {@link MissedBlind}s, representing who should be marked as having missed blinds
     */
    public List<MissedBlind> getMissedBlinds() {
        return missedBlinds;
    }

    /**
     * Returns the next player to ask for the big blind.
     *
     * @param lastAskedSeatId the id of the player who was last asked to post the big blind
     * @return the next player to ask for the big blind, or null if there are no more eligible players
     */
    public BlindsPlayer getNextBigBlindPlayer(final int lastAskedSeatId) {
        BlindsPlayer nextBigBlindPlayer;
        final int dealerSeatId = blindsInfo.getDealerSeatId();
        final int smallBlindSeatId = blindsInfo.getSmallBlindSeatId();

        if (lastAskedSeatId == -1) {
            nextBigBlindPlayer = getPlayerInSeat(blindsInfo.getBigBlindSeatId());
        } else {
            nextBigBlindPlayer = getElementAfter(lastAskedSeatId, seatedPlayers);
            if (nextBigBlindPlayer != null) {
                boolean onSmallBlind = nextBigBlindPlayer.getSeatId() == smallBlindSeatId;
                boolean betweenDealerAndSmall = isBetween(nextBigBlindPlayer.getSeatId(), dealerSeatId, smallBlindSeatId);
                if (onSmallBlind || betweenDealerAndSmall) {
                    // Small and big blind cannot be the same player, hand should be canceled.
                    nextBigBlindPlayer = null;
                }
            }
        }

        return nextBigBlindPlayer;
    }

    /**
     * Gets a list of players who are between the dealer button and the big blind.
     *
     * @return a list of players who are between the dealer button and the big blind
     */
    public List<BlindsPlayer> getPlayersBetweenDealerAndBig() {
        List<BlindsPlayer> result = new ArrayList<BlindsPlayer>();
        for (BlindsPlayer player : seatedPlayers.values()) {
            boolean betweenDealerAndSmall = isBetween(player.getSeatId(), blindsInfo.getDealerSeatId(), blindsInfo.getSmallBlindSeatId());
            boolean betweenSmallAndBig = isBetween(player.getSeatId(), blindsInfo.getSmallBlindSeatId(), blindsInfo.getBigBlindSeatId());
            if (betweenDealerAndSmall || betweenSmallAndBig) {
                result.add(player);
            }
        }
        return result;
    }

    /**
     * Gets a list of players who are eligible to play this hand.
     * Excludes players who have not paid their entry bet.
     *
     * @return a list of players eligible to play in the hand, excluding players who have to post an entry bet
     */
    public List<BlindsPlayer> getEligiblePlayerList() {
        List<BlindsPlayer> result = new ArrayList<BlindsPlayer>();
        final int dealerSeatId = blindsInfo.getDealerSeatId();
        final int bigBlindSeatId = blindsInfo.getBigBlindSeatId();
        final int smallBlindSeatId = blindsInfo.getSmallBlindSeatId();
        final int smallBlindPlayerId = blindsInfo.getSmallBlindPlayerId();

        for (BlindsPlayer player : seatedPlayers.values()) {
            if (!player.hasPostedEntryBet()) {
                log.debug("Player " + player.getId() + " has not posted the entry bet.");
                continue;
            }
            // Players between the dealer and the small blind are not eligible to play.
            final boolean betweenDealerAndSmall = isBetween(player.getSeatId(), dealerSeatId, smallBlindSeatId);
            // Nor are players between the small blind and the big blind.
            final boolean betweenDealerAndBig = isBetween(player.getSeatId(), smallBlindSeatId, bigBlindSeatId);
            // If the player who sits in the small blind seat is not actually the small blind, he's not eligible either.
            final boolean wrongPlayerOnSmallBlind = player.getSeatId() == smallBlindSeatId && player.getId() != smallBlindPlayerId;

            if (!betweenDealerAndBig && !betweenDealerAndSmall && !wrongPlayerOnSmallBlind) {
                result.add(player);
            }
        }
        return result;
    }

    private void log(String message) {
        log.debug(message);
    }


}
