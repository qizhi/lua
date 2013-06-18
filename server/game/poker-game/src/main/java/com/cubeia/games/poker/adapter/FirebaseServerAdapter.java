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

package com.cubeia.games.poker.adapter;

import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.*;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.service.random.api.RandomService;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.adapter.BuyInCalculator.MinAndMaxBuyInResult;
import com.cubeia.games.poker.adapter.domainevents.DomainEventAdapter;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.entity.HandIdentifier;
import com.cubeia.games.poker.handler.ActionTransformer;
import com.cubeia.games.poker.handler.Trigger;
import com.cubeia.games.poker.handler.TriggerType;
import com.cubeia.games.poker.io.protocol.*;
import com.cubeia.games.poker.io.protocol.Currency;
import com.cubeia.games.poker.io.protocol.Enums.BuyInInfoResultCode;
import com.cubeia.games.poker.jmx.PokerStats;
import com.cubeia.games.poker.logic.TimeoutCache;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.games.poker.tournament.messages.AddOnRequest;
import com.cubeia.games.poker.tournament.messages.PokerTournamentRoundReport;
import com.cubeia.games.poker.tournament.messages.RebuyResponse;
import com.cubeia.games.poker.util.ProtocolFactory;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.SystemShutdownException;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.ExposeCardsHolder;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.model.GameStateSnapshot;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.tournament.RoundReport;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.util.ThreadLocalProfiler;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.cubeia.firebase.api.game.player.PlayerStatus.DISCONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.LEAVING;
import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static com.cubeia.games.poker.handler.BackendCallHandler.EXT_PROP_KEY_TABLE_ID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Firebase implementation of the poker logic's server adapter.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class FirebaseServerAdapter implements ServerAdapter {

    private static Logger log = LoggerFactory.getLogger(FirebaseServerAdapter.class);

    @Inject
    @VisibleForTesting
    ActionCache cache;

    @Inject
    @VisibleForTesting
    GameContext gameContext;

    @Service
    @VisibleForTesting
    CashGamesBackendService backend;

    @Inject
    @VisibleForTesting
    Table table;

    @Inject
    @VisibleForTesting
    PokerState state;

    @Inject
    @VisibleForTesting
    ActionTransformer actionTransformer;

    @Inject
    @VisibleForTesting
    ActionSequenceGenerator actionSequenceGenerator;

    @Inject
    @VisibleForTesting
    TimeoutCache timeoutCache;

    @Inject
    @VisibleForTesting
    LobbyUpdater lobbyUpdater;

    @Inject
    @VisibleForTesting
    PlayerUnseater playerUnseater;

    @Inject
    @VisibleForTesting
    BuyInCalculator buyInCalculator;

    @VisibleForTesting
    ProtocolFactory protocolFactory = new ProtocolFactory();

    @Service
    @VisibleForTesting
    PokerConfigurationService configService;

    @Inject
    private HandResultBatchFactory handResultBatchFactory;

    @Inject
    @VisibleForTesting
    HandHistoryReporter handHistory;

    @Service
    @VisibleForTesting
    RandomService randomService;

    @Service
    ShutdownServiceContract shutdownService;

    @Inject
    @VisibleForTesting
    SystemTime dateFetcher;
    
    @Inject
    DomainEventAdapter achievements;

    /*------------------------------------------------

         ADAPTER METHODS

         These methods are the adapter interface
         implementations

      ------------------------------------------------*/

    public java.util.Random getSystemRNG() {
        return randomService.getSystemDefaultRandom();
    }

    @Override
    public void notifyWaitingToStartBreak() {
        sendPublicPacket(new WaitingToStartBreak());
    }

    @Override
    public void notifyWaitingForPlayers() {
        sendPublicPacket(new WaitingForPlayers());
    }

    @Override
    public void notifyTournamentDestroyed() {
        sendPublicPacket(new TournamentDestroyed());
    }

    @Override
    public void notifyBlindsLevelUpdated(com.cubeia.poker.model.BlindsLevel level) {
        sendPublicPacket(new BlindsAreUpdated(createBlindsLevelPacket(level), secondsToNextLevel(level)));
    }

    @Override
    public void notifyRebuyOffer(Collection<Integer> players, String rebuyCost, String rebuyChips) {
        for (Integer player : players) {
            GameDataAction rebuyOffer = protocolFactory.createGameAction(new RebuyOffer(rebuyCost, rebuyChips), player, table.getId());
            sendPublicPacket(rebuyOffer, -1);
        }
    }

    @Override
    public void notifyAddOnsAvailable(String cost, String chips) {
        sendPublicPacket(new AddOnOffer(cost, chips));
    }

    @Override
    public void notifyRebuyPerformed(int playerId) {
        GameDataAction rebuyPerformed = protocolFactory.createGameAction(new PlayerPerformedRebuy(), playerId, table.getId());
        sendPublicPacket(rebuyPerformed, -1);
    }

    @Override
    public void notifyAddOnPerformed(int playerId) {
        GameDataAction addOnPerformed = protocolFactory.createGameAction(new PlayerPerformedAddOn(), playerId, table.getId());
        sendPublicPacket(addOnPerformed, -1);
    }

    @Override
    public void notifyAddOnPeriodClosed() {
        sendPublicPacket(new AddOnPeriodClosed());
    }

    private int secondsToNextLevel(com.cubeia.poker.model.BlindsLevel level) {
        int secondsToNextLevel = Seconds.secondsBetween(dateFetcher.date(), new DateTime(level.getNextLevelStartTime())).getSeconds();
        log.debug("Now: " + dateFetcher.date() + " Next level starts on: " + new DateTime(level.getNextLevelStartTime()) + " Seconds to next level: " + secondsToNextLevel);
        return secondsToNextLevel;
    }

    @Override
    public void sendGameStateTo(GameStateSnapshot snapshot, int playerId) {
        HandStartInfo handStartInfo = new HandStartInfo(getIntegrationHandId());
        BlindsLevel blindsLevel = createBlindsLevelPacket(snapshot.getBlindsLevel());
        Enums.BetStrategy betStrategy = convertBetStrategy(state.getSettings().getBetStrategyType());
        Currency currency = new Currency(state.getSettings().getCurrency().getCode(),state.getSettings().getCurrency().getFractionalDigits());
        int tournamentId = snapshot.getTournamentId();
        int secondsToNextLevel = secondsToNextLevel(snapshot.getBlindsLevel());
        String name = table.getMetaData().getName();
        int capacity = state.getSettings().getTableSize();
        GameState state = new GameState(name, capacity, tournamentId, handStartInfo, blindsLevel, secondsToNextLevel, betStrategy,currency);
        sendPrivatePacket(playerId, state);
    }

    private Enums.BetStrategy convertBetStrategy(BetStrategyType betStrategyType) {
        return Enums.BetStrategy.valueOf(betStrategyType.name());
    }

    @Override
    public void notifyNewHand() throws SystemShutdownException {

        if (backend.isSystemShuttingDown()) {
            /*
             * This will be caught by the processors.
             */
            throw new SystemShutdownException();
        }

        String handId = backend.generateHandId();
        HandIdentifier playedHand = new HandIdentifier();
        playedHand.setIntegrationId(handId);
        getFirebaseState().setCurrentHandIdentifier(playedHand);

        sendPublicPacket(new HandStartInfo(handId), -1);

        log.trace("Starting new hand with ID '" + handId + "'. FBPlayers: " + table.getPlayerSet().getPlayerCount() + ", PokerPlayers: " + state.getSeatedPlayers().size());

        handHistory.notifyNewHand();
    }

    private BlindsLevel createBlindsLevelPacket(com.cubeia.poker.model.BlindsLevel level) {

        return new BlindsLevel(format(level.getSmallBlindAmount()),
                               format(level.getBigBlindAmount()),
                               format(level.getAnteAmount()),
                               level.isBreak(),
                               level.getDurationInMinutes());
    }

    /**
     * Notify about market references.
     * If any reference is null then it is replaced by a minus sign.
     */
    @Override
    public void notifyExternalSessionReferenceInfo(int playerId, String externalTableReference, String externalTableSessionReference) {
        ExternalSessionInfoPacket packet = new ExternalSessionInfoPacket(externalTableReference, externalTableSessionReference);
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        log.trace("--> Send ExternalSessionInfoPacket[" + packet + "] to player: {}", playerId);
        sendPrivatePacket(playerId, action);
    }

    @Override
    public void notifyDealerButton(int seat) {
        DealerButton packet = new DealerButton();
        packet.seat = (byte) seat;
        GameDataAction action = protocolFactory.createGameAction(packet, 0, table.getId());
        log.trace("--> Send DealerButton[" + packet + "] to everyone");
        sendPublicPacket(action, -1);
    }

    @Override
    public void notifyNewRound() {
        handHistory.notifyNewRound();
    }

    @Override
    public void requestAction(ActionRequest request) {
        checkNotNull(request);

        int sequenceNumber = actionSequenceGenerator.next();
        createAndSendActionRequest(request, sequenceNumber);
        setRequestSequence(sequenceNumber);

        // Schedule timeout inc latency grace period
        long latency = state.getTimingProfile().getTime(Periods.LATENCY_GRACE_PERIOD);
        schedulePlayerTimeout(request.getTimeToAct() + latency, request.getPlayerId(), sequenceNumber);
    }

    @Override
    public void requestMultipleActions(Collection<ActionRequest> requests) {
        checkNotNull(requests);
        checkArgument(!requests.isEmpty(), "request collection can't be empty");

        int sequenceNumber = actionSequenceGenerator.next();

        for (ActionRequest actionRequest : requests) {
            createAndSendActionRequest(actionRequest, sequenceNumber);
            long latency = state.getTimingProfile().getTime(Periods.LATENCY_GRACE_PERIOD);
            schedulePlayerTimeout(actionRequest.getTimeToAct() + latency, actionRequest.getPlayerId(), sequenceNumber);
        }

        setRequestSequence(sequenceNumber);
    }

    private void createAndSendActionRequest(ActionRequest request, int sequenceNumber) {
        RequestAction packet = actionTransformer.transform(request, sequenceNumber);
        GameDataAction action = protocolFactory.createGameAction(packet, request.getPlayerId(), table.getId());
        log.trace("--> Send RequestAction[" + packet + "] to everyone");
        sendPublicPacket(action, -1);
    }

    @Override
    public boolean isSystemShutDown() {
        return shutdownService.isSystemShutDown();
    }

    @Override
    public void scheduleTimeout(long millis) {
        log.trace("Scheduling timeout in " + millis + " millis.");
        GameObjectAction action = new GameObjectAction(table.getId());
        TriggerType type = TriggerType.TIMEOUT;
        Trigger timeout = new Trigger(type);
        timeout.setSeq(-1);
        action.setAttachment(timeout);
        table.getScheduler().scheduleAction(action, millis);
        setRequestSequence(-1);
    }

    @Override
    public void notifyActionPerformed(PokerAction pokerAction, PokerPlayer pokerPlayer) {
        PerformAction packet = actionTransformer.transform(pokerAction, pokerPlayer);
        GameDataAction action = protocolFactory.createGameAction(packet, pokerAction.getPlayerId(), table.getId());
        log.trace("--> Send PerformAction[" + packet + "] to everyone");
        sendPublicPacket(action, -1);
        handHistory.notifyActionPerformed(pokerAction, pokerPlayer);
    }

    @Override
    public void notifyFutureAllowedActions(PokerPlayer player, List<PokerActionType> optionList, BigDecimal callAmount, BigDecimal minBet) {
        InformFutureAllowedActions packet = new InformFutureAllowedActions(getFuturePlayerActions(optionList), callAmount.toPlainString(), minBet.toPlainString());
        sendPrivatePacket(player.getId(), packet);
    }

    private List<FuturePlayerAction> getFuturePlayerActions(List<PokerActionType> optionList) {
        List<FuturePlayerAction> options = new ArrayList<FuturePlayerAction>();

        for (PokerActionType actionType : optionList) {
            options.add(new FuturePlayerAction(actionTransformer.fromPokerActionTypeToProtocolActionType(actionType)));
        }
        return options;
    }

    @Override
    public void notifyCommunityCards(List<Card> cards) {
        DealPublicCards packet = actionTransformer.createPublicCardsPacket(cards);
        GameDataAction action = protocolFactory.createGameAction(packet, 0, table.getId());
        log.trace("--> Send DealPublicCards[" + packet + "] to everyone");
        sendPublicPacket(action, -1);
        handHistory.notifyCommunityCards(cards);
    }

    @Override
    public void notifyPrivateCards(int playerId, List<Card> cards) {
        // Send the cards to the owner with proper rank & suit information
        DealPrivateCards packet = actionTransformer.createPrivateCardsPacket(playerId, cards, false);
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        log.trace("--> Send DealPrivateCards[" + packet + "] to player[" + playerId + "]");
        sendPrivatePacket(playerId, action);

        // Send the cards as hidden to the other players
        DealPrivateCards hiddenCardsPacket = actionTransformer.createPrivateCardsPacket(playerId, cards, true);
        GameDataAction notifyAction = protocolFactory.createGameAction(hiddenCardsPacket, playerId, table.getId());
        log.trace("--> Send DealPrivateCards(hidden)[" + hiddenCardsPacket + "] to everyone");
        sendPublicPacket(notifyAction, playerId);

        handHistory.notifyPrivateCards(playerId, cards);
    }

    @Override
    public void notifyBestHand(int playerId, HandType handType, List<Card> cardsInHand, boolean publicHand) {
        if (cardsInHand == null) {
            log.error("cardsInHand is null, this will cause a NullPointerException if we try to notify the best hand. Ignoring. " +
                      "HandType: " + handType + " playerId " + playerId);
            return;
        }
        BestHand bestHandPacket = actionTransformer.createBestHandPacket(playerId, handType, cardsInHand);
        GameDataAction bestHandAction = protocolFactory.createGameAction(bestHandPacket, playerId, table.getId());
        log.trace("--> Send BestHandPacket[" + bestHandPacket + "] to player[" + playerId + "]");

        if (publicHand) {
            sendPublicPacket(bestHandAction, -1);
        } else {
            sendPrivatePacket(playerId, bestHandAction);
        }
    }

    @Override
    public void notifyPrivateExposedCards(int playerId, List<Card> cards) {
        // Send the cards as public to the other players
        DealPrivateCards hiddenCardsPacket = actionTransformer.createPrivateCardsPacket(playerId, cards, false);
        GameDataAction action = protocolFactory.createGameAction(hiddenCardsPacket, playerId, table.getId());
        log.trace("--> Send DealPrivateCards(exposed)[" + hiddenCardsPacket + "] to everyone");
        sendPublicPacket(action, -1);
        handHistory.notifyPrivateExposedCards(playerId, cards);
    }

    @Override
    public void exposePrivateCards(ExposeCardsHolder holder) {
        ExposePrivateCards packet = actionTransformer.createExposeCardsPacket(holder);
        GameDataAction action = protocolFactory.createGameAction(packet, 0, table.getId());
        log.trace("--> Send ExposePrivateCards[" + packet + "] to everyone");
        sendPublicPacket(action, -1);
        handHistory.exposePrivateCards(holder);
    }

    @Override
    public void performPendingBuyIns(Collection<PokerPlayer> players) {
        for (PokerPlayer player : players) {
            if (!player.isBuyInRequestActive() && player.getRequestedBuyInAmount().compareTo(BigDecimal.ZERO) > 0) {
                PokerPlayerImpl pokerPlayer = (PokerPlayerImpl) player;

                BigDecimal playerBalanceIncludingPending = pokerPlayer.getBalance().add(pokerPlayer.getBalanceNotInHand());
                BigDecimal amountToBuyIn = buyInCalculator.calculateAmountToReserve(state.getMaxBuyIn(), playerBalanceIncludingPending,
                                                                              player.getRequestedBuyInAmount());

                if (amountToBuyIn.compareTo(BigDecimal.ZERO) > 0) {
                    log.trace("sending reserve request to backend: player id = {}, amount = {}, amount requested by player = {}",
                            new Object[]{player.getId(), amountToBuyIn, player.getRequestedBuyInAmount()});

                    // ReserveCallback callback = backend.getCallbackFactory().createReserveCallback(table);
                    Money amountToBuyInMoney = new Money(amountToBuyIn,state.getSettings().getCurrency());
                    ReserveRequest reserveRequest = new ReserveRequest(pokerPlayer.getPlayerSessionId(), amountToBuyInMoney);
                    player.setRequestedBuyInAmount(amountToBuyIn);
                    backend.reserveMoneyForTable(reserveRequest, new TableId(table.getMetaData().getGameId(), table.getId()));
                    player.buyInRequestActive();
                } else {
                    log.trace("Won't reserve money, max reached: player id = {}, amount wanted = {}", player.getId(), player.getRequestedBuyInAmount());
                    player.clearRequestedBuyInAmountAndRequest();
                }
            }
        }
    }

    @Override
    public void notifyBuyInInfo(int playerId, boolean mandatoryBuyin) {
        try {
            PokerPlayer player = state.getPokerPlayer(playerId);
            BuyInInfoResponse resp = new BuyInInfoResponse();

            BigDecimal playerBalance = player == null ? BigDecimal.ZERO : (player.getBalance().add(player.getPendingBalanceSum()));
            BigDecimal balanceInWallet = BigDecimal.ZERO;
            resp.balanceOnTable = format(playerBalance);
            resp.mandatoryBuyin = mandatoryBuyin;

            try {
                String currency = state.getSettings().getCurrency().getCode();
                balanceInWallet = backend.getAccountBalance(playerId, currency).getAmount();
                resp.currencyCode = currency;
                resp.balanceInWallet = format(balanceInWallet);
            } catch (GetBalanceFailedException e) {
                log.error("error getting balance", e);
                resp.resultCode = BuyInInfoResultCode.UNSPECIFIED_ERROR;
                resp.balanceInWallet = "N/A";
                resp.minAmount = "0";
                resp.maxAmount = "0";
                resp.currencyCode = "";
            }

            if (resp.resultCode != BuyInInfoResultCode.UNSPECIFIED_ERROR) {
                MinAndMaxBuyInResult buyInRange = buyInCalculator.calculateBuyInLimits(state.getMinBuyIn(), state.getMaxBuyIn(),
                        state.getAnteLevel(), playerBalance);
                resp.minAmount = format(buyInRange.getMinBuyIn());
                resp.maxAmount = format(balanceInWallet.min(buyInRange.getMaxBuyIn()));
                resp.resultCode = buyInRange.isBuyInPossible() ? BuyInInfoResultCode.OK : BuyInInfoResultCode.MAX_LIMIT_REACHED;
            }

            log.trace("Sending buyin information to player[" + playerId + "]: " + resp);

            GameDataAction gda = new GameDataAction(playerId, table.getId());
            StyxSerializer styx = new StyxSerializer(null);
            gda.setData(styx.pack(resp));

            table.getNotifier().notifyPlayer(playerId, gda);
        } catch (Exception e) {
            log.error("Failed to create buy in info response for player[" + playerId + "], mandatory[" + mandatoryBuyin + "]", e);
        }
    }

    @Override
    public void notifyHandEnd(HandResult handResult, HandEndStatus handEndStatus, boolean tournamentTable) {
        ThreadLocalProfiler.add("FirebaseServerAdapter.notifyHandEnd.start");

        if (handEndStatus.equals(HandEndStatus.NORMAL) && handResult != null) {
            sendHandEndPacket(handResult);
            performBackEndTransactions(handResult, handEndStatus, tournamentTable);
            updateHandEndStatistics();
        } else {
            log.debug("The hand was cancelled on table: " + table.getId() + " - " + table.getMetaData().getName());
            cleanupPlayers(new SitoutCalculator());
            HandCanceled handCanceledPacket = new HandCanceled();
            GameDataAction action = protocolFactory.createGameAction(handCanceledPacket, -1, table.getId());
            log.trace("--> Send HandCanceled[" + handCanceledPacket + "] to everyone");
            sendPublicPacket(action, -1);
        }

        achievements.notifyHandEnd(handResult, handEndStatus, tournamentTable, state.getSettings());
        
        clearActionCache();
        ThreadLocalProfiler.add("FirebaseServerAdapter.notifyHandEnd.stop");

        if (isSystemShutDown()) {
            if (tournamentTable) {
                log.error("System is shut down but tournament seems to still be running. tableId: " + table.getId());
            }
            closeTable();
        }
    }

    private void closeTable() {
        log.trace("Closing table " + table.getId());
        GameObjectAction action = new GameObjectAction(table.getId());
        action.setAttachment(new CloseTableRequest(true));
        table.getScheduler().scheduleAction(action, 200);
    }

    private Map<Integer, String> getTransactionIds(BatchHandResponse batchHandResult) {
        Map<Integer, String> transIds = new HashMap<Integer, String>();
        for (TransactionUpdate u : batchHandResult.getResultingBalances()) {
            long transactionId = u.getTransactionId().transactionId;
            int userId = u.getBalance().getPlayerSessionId().playerId;
            transIds.put(userId, String.valueOf(transactionId));
        }
        return transIds;
    }

    private void updateHandEndStatistics() {
        PokerStats.getInstance().reportHandEnd();
        getFirebaseState().incrementHandCount();
    }

    private void sendHandEndPacket(HandResult handResult) {
        Collection<RatedPlayerHand> hands = handResult.getPlayerHands();
        List<PotTransfer> transfers = new ArrayList<PotTransfer>();
        PotTransfers potTransfers = new PotTransfers(false, transfers, null);

        for (PotTransition pt : handResult.getPotTransitions()) {
            log.trace("--> sending winner pot transfer to client: {}", pt);
            transfers.add(actionTransformer.createPotTransferPacket(pt));
        }
        HandEnd packet = actionTransformer.createHandEndPacket(hands, potTransfers, handResult.getPlayerRevealOrder());
        GameDataAction action = protocolFactory.createGameAction(packet, 0, table.getId());
        log.trace("--> Send HandEnd[" + packet + "] to everyone");
        log.trace("--> handResult.getPlayerRevealOrder: {}", handResult.getPlayerRevealOrder());
        sendPublicPacket(action, -1);
    }

    private void performBackEndTransactions(HandResult handResult, HandEndStatus handEndStatus, boolean isTournament) {
        String handId = getIntegrationHandId();
        TableId externalTableId = getIntegrationTableId();
        Map<Integer, String> transactionIds = new HashMap<Integer, String>();
        if (!isTournament) {
            BatchHandResponse batchHandResult = batchHand(handResult, handId, externalTableId);
            transactionIds = getTransactionIds(batchHandResult);
            validateAndUpdateBalances(batchHandResult);
        }
        handHistory.notifyHandEnd(handResult, handEndStatus, transactionIds);
    }

    private BatchHandResponse batchHand(HandResult handResult, String handId, TableId externalTableId) {
        BatchHandRequest batchHandRequest = handResultBatchFactory.createAndValidateBatchHandRequest(handResult, handId, externalTableId);
        batchHandRequest.setStartTime(state.getStartTime());
        batchHandRequest.setEndTime(System.currentTimeMillis());
        return doBatchHandResult(batchHandRequest);
    }

    private BatchHandResponse doBatchHandResult(BatchHandRequest batchHandRequest) {
        BatchHandResponse batchHandResult;
        try {
            batchHandResult = backend.batchHand(batchHandRequest);
        } catch (BatchHandFailedException e) {
            throw new RuntimeException(e);
        }
        return batchHandResult;
    }

    public String getIntegrationHandId() {
        HandIdentifier id = getFirebaseState().getCurrentHandIdentifier();
        return (id == null ? null : id.getIntegrationId());
    }

    private TableId getIntegrationTableId() {
        return (TableId) state.getExternalTableProperties().get(EXT_PROP_KEY_TABLE_ID);
    }

    @VisibleForTesting
    protected void validateAndUpdateBalances(BatchHandResponse batchHandResult) {
        for (TransactionUpdate tup : batchHandResult.getResultingBalances()) {
            PokerPlayerImpl pokerPlayer = null;
            BalanceUpdate bup = tup.getBalance();
            for (PokerPlayer pp : state.getCurrentHandPlayerMap().values()) {
                if (((PokerPlayerImpl) pp).getPlayerSessionId().equals(bup.getPlayerSessionId())) {
                    pokerPlayer = (PokerPlayerImpl) pp;
                }
            }

            if (pokerPlayer == null) {
                throw new IllegalStateException("error updating balance: unable to find player with session = " + bup.getPlayerSessionId());
            } else {
                BigDecimal gameBalance = pokerPlayer.getBalance().add(pokerPlayer.getBalanceNotInHand());
                BigDecimal backendBalance = bup.getBalance().getAmount();

                if (gameBalance.compareTo(backendBalance) != 0) {
                    //log.error("backend balance: {} not equal to game balance: {}, will reset to backend value", backendBalance, gameBalance);
                    throw new IllegalStateException("backend balance: " + backendBalance + " not equal to game balance: " + gameBalance + ", will reset to backend value");
                }
            }
        }
    }

    private void clearActionCache() {
        if (cache != null) {
            cache.clear(table.getId());
        }
    }

    @Override
    public void notifyPlayerBalance(PokerPlayer player) {
        if (player == null) return;

        BigDecimal playersTotalContributionToPot = state.getPlayersTotalContributionToPot(player);

        // First send public packet to all the other players but exclude the pending balance.
        GameDataAction publicAction = actionTransformer.createPlayerBalanceAction(
                player.getBalance(), BigDecimal.ZERO, playersTotalContributionToPot, player.getId(), table.getId());
        sendPublicPacket(publicAction, player.getId());

        // Then send private packet to the player.
        GameDataAction privateAction = actionTransformer.createPlayerBalanceAction(
                player.getBalance(), player.getPendingBalanceSum(),  playersTotalContributionToPot, player.getId(), table.getId());
        log.trace("Send private PBA: " + privateAction);
        sendPrivatePacket(player.getId(), privateAction);
    }

    /**
     * Sends a poker tournament round report to the tournament as set in the table meta-data.
     *
     * @param report, poker-logic protocol object, not null.
     */
    @Override
    public void reportTournamentRound(RoundReport report) {
        PokerStats.getInstance().reportHandEnd();

        // Map the report to a server specific round report
        PokerTournamentRoundReport.Level currentLevel = new PokerTournamentRoundReport.Level(report.getSmallBlindAmount(), report.getBigBlindAmount(), report.getAnteAmount());
        PokerTournamentRoundReport pokerReport = new PokerTournamentRoundReport(report.getBalanceMap(), currentLevel);
        MttRoundReportAction action = new MttRoundReportAction(table.getMetaData().getMttId(), table.getId());
        action.setAttachment(pokerReport);
        table.getTournamentNotifier().sendToTournament(action);
        clearActionCache();
    }

    @Override
    public void sendRebuyResponseToTournament(int playerId, boolean response, BigDecimal chipsAtHandFinish) {
        MttObjectAction action = new MttObjectAction(table.getMetaData().getMttId(), new RebuyResponse(table.getId(), playerId, chipsAtHandFinish, response));
        table.getTournamentNotifier().sendToTournament(action);
    }

    @Override
    public void sendAddOnRequestToTournament(int playerId) {
        MttObjectAction action = new MttObjectAction(table.getMetaData().getMttId(), new AddOnRequest(table.getId(), playerId));
        table.getTournamentNotifier().sendToTournament(action);
    }

    public void notifyPotUpdates(Collection<com.cubeia.poker.pot.Pot> pots, Collection<PotTransition> potTransitions) {
        boolean fromPlayerToPot = !potTransitions.isEmpty() && potTransitions.iterator().next().isFromPlayerToPot();
        List<Pot> clientPots = new ArrayList<Pot>();
        List<PotTransfer> transfers = new ArrayList<PotTransfer>();

        // notify return uncalled chips
        for (PotTransition potTransition : potTransitions) {
            if (potTransition.isFromBetStackToPlayer()) {
                log.trace("--> sending takeBackUncalledChips to client: {}", potTransition);
                notifyTakeBackUncalledBet(potTransition.getPlayer().getId(), potTransition.getAmount());
            }
        }

        for (com.cubeia.poker.pot.Pot pot : pots) {
            clientPots.add(actionTransformer.createPotUpdatePacket(pot.getId(), pot.getPotSize()));
        }

        for (PotTransition potTransition : potTransitions) {
            if (!potTransition.isFromBetStackToPlayer()) {
                log.trace("--> sending pot update to client: {}", potTransition);
                transfers.add(actionTransformer.createPotTransferPacket(potTransition));
            }
        }

        // notify bet stacks to pots
        PotTransfers potTransfers = new PotTransfers(fromPlayerToPot, transfers, clientPots);
        GameDataAction action = protocolFactory.createGameAction(potTransfers, 0, table.getId());
        sendPublicPacket(action, -1);

        handHistory.notifyPotUpdates(pots, potTransitions);
    }


    @Override
    public void notifyRakeInfo(RakeInfoContainer rakeInfoContainer) {
        log.trace("--> sending rake info to client: {}", rakeInfoContainer);
        RakeInfo rakeInfo = new RakeInfo(rakeInfoContainer.getTotalPot().toPlainString(), rakeInfoContainer.getTotalRake().toPlainString());
        GameDataAction action = protocolFactory.createGameAction(rakeInfo, 0, table.getId());
        sendPublicPacket(action, -1);
    }

    @Override
    public void notifyTakeBackUncalledBet(int playerId, BigDecimal amount) {
        log.trace("--> Taking back uncalled bet: {}", playerId, amount);
        ProtocolObject takeBackUncalledBet = new TakeBackUncalledBet(playerId, amount.toPlainString());
        GameDataAction action = protocolFactory.createGameAction(takeBackUncalledBet, playerId, table.getId());
        sendPublicPacket(action, -1);
    }

    @Override
    public void notifyHandStartPlayerStatus(int playerId, PokerPlayerStatus status, boolean away, boolean sitOutNextHand) {
        log.trace("Notify hand start player status: " + playerId + " -> " + status);
        PlayerHandStartStatus packet = new PlayerHandStartStatus();
        packet.player = playerId;
        switch (status) {
            case SITIN:
                packet.status = Enums.PlayerTableStatus.SITIN;
                break;
            case SITOUT:
                packet.status = Enums.PlayerTableStatus.SITOUT;
                break;
        }
        packet.away = away;
        packet.sitOutNextHand = sitOutNextHand;
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        sendPublicPacket(action, -1);
    }

    @Override
    public void notifyPlayerStatusChanged(int playerId, PokerPlayerStatus status, boolean inCurrentHand,
                                          boolean away, boolean sitOutNextHand) {
        log.trace("Notify player status changed: " + playerId + " -> " + status);
        PlayerPokerStatus packet = new PlayerPokerStatus();
        packet.player = playerId;
        switch (status) {
            case SITIN:
                packet.status = Enums.PlayerTableStatus.SITIN;
                break;
            case SITOUT:
                packet.status = Enums.PlayerTableStatus.SITOUT;
                break;
        }
        packet.inCurrentHand = inCurrentHand;
        packet.away = away;
        packet.sitOutNextHand = sitOutNextHand;
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        sendPublicPacket(action, -1);
    }

    /**
     * Schedule a player timeout trigger command.
     */
    public void schedulePlayerTimeout(long millis, int pid, int seq) {
        GameObjectAction action = new GameObjectAction(table.getId());
        TriggerType type = TriggerType.PLAYER_TIMEOUT;
        Trigger timeout = new Trigger(type, pid);
        timeout.setSeq(seq);
        action.setAttachment(timeout);
        UUID actionId = table.getScheduler().scheduleAction(action, millis);
        timeoutCache.addTimeout(table.getId(), pid, actionId);
    }

    /**
     * Remove all players in state LEAVING or DISCONNECTED
     */
    public void cleanupPlayers(SitoutCalculator sitoutCalculator) {
        if (table.getMetaData().getType().equals(TableType.NORMAL)) {
            // Check for disconnected and leaving players
            UnmodifiableSet<GenericPlayer> players = table.getPlayerSet().getPlayers();
            for (GenericPlayer p : players) {
                if (p.getStatus() == DISCONNECTED || p.getStatus() == LEAVING) {
                    log.debug("Player clean up - unseat leaving or disconnected player[" + p.getPlayerId() + "] from table[" + table.getId() + "]");
                    unseatPlayer(p.getPlayerId(), false);
                }
            }

            // Check sitting out players for time outs
            Collection<PokerPlayer> timeoutPlayers = sitoutCalculator.checkTimeoutPlayers(
                    state.getSeatedPlayers(), state.getSettings().getSitoutTimeLimitMilliseconds());
            for (PokerPlayer p : timeoutPlayers) {
                log.debug("Player clean up - unseat timed out sit-out player[" + p.getId() + "] from table[" + table.getId() + "]");
                unseatPlayer(p.getId(), true);
            }
        }

        lobbyUpdater.updateLobby((FirebaseState) state.getAdapterState(), table);
    }

    public void unseatPlayer(int playerId, boolean setAsWatcher) {
        PokerPlayer pokerPlayer = state.getPokerPlayer(playerId);
        boolean participatingInCurrentHand = state.getPlayerInCurrentHand(playerId) != null && state.isPlaying();

        if (!pokerPlayer.isBuyInRequestActive() && !participatingInCurrentHand) {
            playerUnseater.unseatPlayer(table, playerId, setAsWatcher);
        }
    }

    /**
     * This action will be cached and used for sending current state to
     * joining players.
     * <p/>
     * If skipPlayerId is -1 then no player will be skipped.
     *
     */
    private void sendPublicPacket(GameAction action, int skipPlayerId) {
        if (skipPlayerId < 0) {
            table.getNotifier().notifyAllPlayers(action);
        } else {
            table.getNotifier().notifyAllPlayersExceptOne(action, skipPlayerId);
        }
        // Add to state cache
        if (cache != null) {
            cache.addPublicActionWithExclusion(table.getId(), action, skipPlayerId);
        }
    }

    private void sendPublicPacket(ProtocolObject packet, int skipPlayerId) {
        GameDataAction action = protocolFactory.createGameAction(packet, 0, table.getId());
        sendPublicPacket(action, skipPlayerId);
    }

    private void sendPublicPacket(ProtocolObject packet) {
        sendPublicPacket(packet, -1);
    }

    /**
     * Send private packet to player and cache it as private. The cached action
     * will be sent to the player when rejoining.
     *
     * @param playerId player id
     * @param action   action
     */
    private void sendPrivatePacket(int playerId, GameAction action) {
        table.getNotifier().notifyPlayer(playerId, action);

        if (cache != null) {
            cache.addPrivateAction(table.getId(), playerId, action);
        }
    }

    private void sendPrivatePacket(int playerId, ProtocolObject packet) {
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        sendPrivatePacket(playerId, action);
    }

    private FirebaseState getFirebaseState() {
        return (FirebaseState) state.getAdapterState();
    }

    private void setRequestSequence(int seq) {
        getFirebaseState().setCurrentRequestSequence(seq);
    }


    @Override
    public void notifyDeckInfo(int size, com.cubeia.poker.hand.Rank rankLow) {
        DeckInfo deckInfoPacket = new DeckInfo(size, actionTransformer.convertRankToProtocolEnum(rankLow));
        GameDataAction action = protocolFactory.createGameAction(deckInfoPacket, 0, table.getId());
        sendPublicPacket(action, -1);
        handHistory.notifyDeckInfo(size, rankLow);
    }

    @Override
    public void notifyDisconnected(int playerId) {
        timeoutCache.removeTimeout(table.getId(), playerId, table.getScheduler());

        long timeout = state.getTimingProfile().getTime(Periods.DISCONNECT_EXTRA_TIME);
        long latencyTimeout = timeout + state.getTimingProfile().getTime(Periods.LATENCY_GRACE_PERIOD);
        PlayerDisconnectedPacket packet = new PlayerDisconnectedPacket();
        packet.playerId = playerId;
        packet.timebank = (int) timeout;

        log.trace("Notify disconnect: {}", packet);
        GameDataAction action = protocolFactory.createGameAction(packet, playerId, table.getId());
        sendPublicPacket(action, -1);

        log.trace("Schedule new timeout for player in {} ms", latencyTimeout);
        schedulePlayerTimeout(latencyTimeout, playerId, getFirebaseState().getCurrentRequestSequence());
    }

}
