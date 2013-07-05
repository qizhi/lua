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

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.backend.cashgame.TournamentSessionId;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.activator.CreationParticipant;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.tournament.PokerTournament;
import com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes;
import com.cubeia.games.poker.tournament.configuration.RebuyConfiguration;
import com.cubeia.games.poker.tournament.configuration.TournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.rebuy.RebuySupport;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.timing.Timings;
import com.cubeia.poker.tournament.history.api.HistoricPlayer;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static com.cubeia.games.poker.common.money.MoneyFormatter.format;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.*;

public abstract class PokerTournamentCreationParticipant implements CreationParticipant {

    private static transient Logger log = Logger.getLogger(PokerTournamentCreationParticipant.class);
    protected final TournamentConfiguration config;
    protected final TournamentHistoryPersistenceService storageService;
    private Timings timing = Timings.DEFAULT;
    private Set<HistoricPlayer> resurrectingPlayers = new HashSet<HistoricPlayer>();
    private boolean isResurrection = false;
    private String historicId = null;
    private SystemTime dateFetcher;
    private String tournamentSessionId;
    private String datePattern = "yyyy-MM-dd HH:mm";
    private CashGamesBackendService cashGamesBackendService;

    public PokerTournamentCreationParticipant(TournamentConfiguration config, TournamentHistoryPersistenceService storageService,
                                              SystemTime dateFetcher, CashGamesBackendService cashGamesBackendService) {
        log.debug("Creating tournament participant with config " + config);
        this.dateFetcher = dateFetcher;
        this.storageService = storageService;
        this.config = config;
        this.cashGamesBackendService = cashGamesBackendService;
    }

    public LobbyPath getLobbyPathForTournament(MTTState mtt) {
        return new LobbyPath(mtt.getMttLogicId(), getType());
    }

    public void setHistoricId(String historicId) {
        this.historicId = historicId;
    }

    public void setTournamentSessionId(String tournamentSessionId) {
        this.tournamentSessionId = tournamentSessionId;
    }

    public final void tournamentCreated(MTTState mtt, LobbyAttributeAccessor acc) {
        log.debug("Poker tournament created. MTT: [" + mtt.getId() + "]" + mtt.getName());
        MTTStateSupport stateSupport = ((MTTStateSupport) mtt);
        stateSupport.setGameId(PokerTournamentActivatorImpl.POKER_GAME_ID);
        stateSupport.setSeats(config.getSeatsPerTable());
        stateSupport.setName(config.getName());
        stateSupport.setCapacity(config.getMaxPlayers());
        stateSupport.setMinPlayers(config.getMinPlayers());

        PokerTournamentState pokerState = new PokerTournamentState();
        pokerState.setTiming(config.getTimingType());
        pokerState.setBetStrategy(config.getBetStrategy());
        pokerState.setBlindsStructure(config.getBlindsStructure());
        pokerState.setBuyIn(config.getBuyIn());
        pokerState.setFee(config.getFee());
        pokerState.setCurrency(cashGamesBackendService.getCurrency(config.getCurrency()));
        pokerState.setPayoutStructure(config.getPayoutStructure(), config.getMinPlayers());

        pokerState.setStartingChips(config.getStartingChips());
        TournamentLifeCycle tournamentLifeCycle = getTournamentLifeCycle();
        pokerState.setLifecycle(tournamentLifeCycle);
        pokerState.setStartDateString(tournamentLifeCycle.getStartTime().toString(datePattern));
        pokerState.setRegistrationStartDate(tournamentLifeCycle.getOpenRegistrationTime());
        pokerState.setMinutesVisibleAfterFinished(getMinutesVisibleAfterFinished());
        pokerState.setTemplateId(getConfigurationTemplateId());
        pokerState.setSitAndGo(isSitAndGo());
        pokerState.setRebuySupport(createRebuySupport(config.getRebuyConfiguration()));
        pokerState.setGuaranteedPrizePool(config.getGuaranteedPrizePool());
        pokerState.getAllowedOperators().addAll(config.getOperatorIds());

        PokerTournament tournament = new PokerTournament(pokerState);
        stateSupport.setState(tournament);

        acc.setStringAttribute("SPEED", timing.name());
        // TODO: Table size should be configurable.
        acc.setIntAttribute(PokerTournamentLobbyAttributes.TABLE_SIZE.name(), 10);
        String opString = getOperatorLobbyIdString();
        acc.setStringAttribute(OPERATOR_IDS.name(), opString);
        createHistoricTournament(stateSupport, pokerState);
        tournamentCreated(stateSupport, pokerState, acc);
    }

    private String getOperatorLobbyIdString() {
        StringBuilder b = new StringBuilder();
        for (Long l : config.getOperatorIds()) {
            b.append(l).append(",");
        }
        String s = b.toString();
        if(s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private RebuySupport createRebuySupport(RebuyConfiguration config) {
        if (config == null) {
            return RebuySupport.NO_REBUYS;
        } else {
            log.debug("Setting rebuy support. Rebuys allowed: " + config.getNumberOfRebuysAllowed());
            boolean rebuysEnabled = config.getNumberOfRebuysAllowed() != 0;
            return new RebuySupport(rebuysEnabled, config.getChipsForRebuy(), config.getChipsForAddOn(), config.getNumberOfRebuysAllowed(),
                    config.getMaxStackForRebuy(), config.isAddOnsEnabled(), config.getNumberOfLevelsWithRebuys(), config.getRebuyCost(), config.getAddOnCost());
        }
    }

    public void setResurrectingPlayers(Set<HistoricPlayer> resurrectingPlayers) {
        isResurrection = true;
        if (resurrectingPlayers != null) {
            this.resurrectingPlayers = resurrectingPlayers;
        }
    }

    protected void setStatus(PokerTournamentState pokerState, LobbyAttributeAccessor lobbyAttributeAccessor, PokerTournamentStatus status) {
        lobbyAttributeAccessor.setStringAttribute(PokerTournamentLobbyAttributes.STATUS.name(), status.name());
        pokerState.setStatus(status);
        storageService.statusChanged(status.name(), historicId, dateFetcher.now());
    }

    protected void tournamentCreated(MTTStateSupport state, PokerTournamentState pokerState, LobbyAttributeAccessor lobbyAttributeAccessor) {
        pokerState.setResurrectingTournament(isResurrection);
        log.debug("Tournament created " + pokerState.getHistoricId() + " is resurrections? " + isResurrection);
        if (isResurrection) {
            pokerState.setResurrectingPlayers(resurrectingPlayers);
            pokerState.setTournamentSessionId(new TournamentSessionId(tournamentSessionId));
            log.debug("Tournament session id: " + tournamentSessionId);
        }
        setLobbyAttributes(lobbyAttributeAccessor);
    }

    protected void createHistoricTournament(MTTStateSupport state, PokerTournamentState pokerState) {
        if (historicId != null) {
            pokerState.setHistoricId(historicId);
        } else {
            historicId = storageService.createHistoricTournament(state.getName(), state.getId(), pokerState.getTemplateId(), isSitAndGo());
            pokerState.setHistoricId(historicId);
            storageService.statusChanged(PokerTournamentStatus.ANNOUNCED.name(), historicId, dateFetcher.now());
        }
    }

    private void setLobbyAttributes(LobbyAttributeAccessor lobbyAttributeAccessor) {
        //        lobbyAttributeAccessor.setStringAttribute(IDENTIFIER.name(), config.getIdentifier());
        lobbyAttributeAccessor.setStringAttribute(BUY_IN.name(), format(config.getBuyIn()));
        lobbyAttributeAccessor.setStringAttribute(FEE.name(), format(config.getFee()));
        lobbyAttributeAccessor.setStringAttribute(SIT_AND_GO.name(), isSitAndGo() ? "true" : "false");
        lobbyAttributeAccessor.setStringAttribute(BUY_IN_CURRENCY_CODE.name(), config.getCurrency());
    }

    protected abstract int getConfigurationTemplateId();

    protected abstract int getMinutesVisibleAfterFinished();

    protected abstract TournamentLifeCycle getTournamentLifeCycle();

    protected abstract String getType();

    protected abstract boolean isSitAndGo();
}
