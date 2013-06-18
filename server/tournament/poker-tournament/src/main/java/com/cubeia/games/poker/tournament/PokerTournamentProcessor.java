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

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.cubeia.backend.cashgame.dto.OpenSessionFailedResponse;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.firebase.CashGamesBackendService;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.firebase.guice.tournament.TournamentAssist;
import com.cubeia.firebase.guice.tournament.TournamentHandler;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.io.protocol.RequestBlindsStructure;
import com.cubeia.games.poker.io.protocol.RequestPayoutInfo;
import com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData;
import com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList;
import com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo;
import com.cubeia.games.poker.io.protocol.RequestTournamentTable;
import com.cubeia.games.poker.tournament.lobby.TournamentLobby;
import com.cubeia.games.poker.tournament.lobby.TournamentLobbyFactory;
import com.cubeia.games.poker.tournament.messages.AddOnRequest;
import com.cubeia.games.poker.tournament.messages.CancelTournament;
import com.cubeia.games.poker.tournament.messages.CloseTournament;
import com.cubeia.games.poker.tournament.messages.PlayerLeft;
import com.cubeia.games.poker.tournament.messages.RebuyResponse;
import com.cubeia.games.poker.tournament.messages.RebuyTimeout;
import com.cubeia.games.poker.tournament.util.PacketSender;
import com.cubeia.games.poker.tournament.util.PacketSenderFactory;
import com.cubeia.network.users.firebase.api.UserServiceContract;
import com.cubeia.poker.domainevents.api.DomainEventsService;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

/**
 * The responsibility of this class is to un-marshal incoming messages and pass them on to the PokerTournament or TournamentLobby.
 *
 */
public class PokerTournamentProcessor implements TournamentHandler, PlayerInterceptor, PlayerListener {

    // Use %X{tournamentId} in the layout pattern to include this information.
    private static final String MDC_TAG = "tournamentId";

    private static transient Logger log = Logger.getLogger(PokerTournamentProcessor.class);

    private PokerTournamentUtil util = new PokerTournamentUtil();

    @Inject
    private SystemTime dateFetcher;

    @Inject
    private PacketSenderFactory senderFactory;

    @Inject
    private TournamentLobbyFactory lobbyFactory;

    @Inject
    private TournamentAssist support;

    @Service
    private TournamentHistoryPersistenceService historyService;

    @Service
    private CashGamesBackendService backend;
    
    @Service
    private UserServiceContract userService;

    @Service(proxy = true)
    private ShutdownServiceContract shutdownService;

    @Service(proxy = true)
    private TournamentPlayerRegistry tournamentPlayerRegistry;

    @Service DomainEventsService domainEventService;

    @Override
    public PlayerInterceptor getPlayerInterceptor(MTTStateSupport state) {
        return this;
    }

    @Override
    public PlayerListener getPlayerListener(MTTStateSupport state) {
        return this;
    }

    @Override
    public void process(MttRoundReportAction action, MttInstance instance) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            prepareTournament(instance).processRoundReport(action);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void process(MttTablesCreatedAction action, MttInstance instance) {
        log.info("Tables created: " + action + " instance: " + instance);
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            prepareTournament(instance).handleTablesCreated(action);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void process(MttObjectAction action, MttInstance instance) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            Object object = action.getAttachment();
            PokerTournament tournament = prepareTournament(instance);
            if (object instanceof TournamentTrigger) {
                TournamentTrigger trigger = (TournamentTrigger) object;
                tournament.handleTrigger(trigger);
            } else if (object instanceof OpenSessionResponse) {
                tournament.handleOpenSessionResponse((OpenSessionResponse) object);
            } else if (object instanceof OpenSessionFailedResponse) {
                tournament.handleOpenSessionResponseFailed((OpenSessionFailedResponse) object);
            } else if (object instanceof ReserveResponse) {
                tournament.handleReservationResponse((ReserveResponse) object);
            } else if (object instanceof ReserveFailedResponse) {
                tournament.handleReservationFailed((ReserveFailedResponse) object);
            } else if (object instanceof CloseTournament) {
                tournament.closeTournament();
            } else if (object instanceof PlayerLeft) {
                tournament.handlePlayerLeft((PlayerLeft) object);
            } else if (object instanceof CancelTournament) {
                tournament.cancelTournament();
            } else if (object instanceof RebuyResponse) {
                tournament.handleRebuyResponse((RebuyResponse) object);
            } else if (object instanceof AddOnRequest) {
                tournament.handleAddOnRequest((AddOnRequest) object);
            } else if (object instanceof RebuyTimeout) {
                RebuyTimeout timeout = (RebuyTimeout) object;
                tournament.handleRebuyTimeout(timeout.getTableId());
            } else {
                log.warn("Unexpected attachment: " + object);
            }
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void process(MttDataAction action, MttInstance instance) {
        StyxSerializer serializer = new StyxSerializer(new ProtocolObjectFactory());
        ProtocolObject packet = serializer.unpack(action.getData());
        int playerId = action.getPlayerId();
        if (packet instanceof RequestTournamentPlayerList) {
            prepareTournamentLobby(instance).sendPlayerListTo(playerId);
        } else if (packet instanceof RequestBlindsStructure) {
            prepareTournamentLobby(instance).sendBlindsStructureTo(playerId);
        } else if (packet instanceof RequestPayoutInfo) {
            prepareTournamentLobby(instance).sendPayoutInfoTo(playerId);
        } else if (packet instanceof RequestTournamentLobbyData) {
            prepareTournamentLobby(instance).sendTournamentLobbyDataTo(playerId);
        } else if (packet instanceof RequestTournamentTable) {
            prepareTournamentLobby(instance).sendTournamentTableTo(playerId);
        } else if (packet instanceof RequestTournamentRegistrationInfo) {
            prepareTournamentLobby(instance).sendRegistrationInfoTo(playerId);
        }
    }

    @Override
    public void process(MttSeatingFailedAction mttSeatingFailedAction, MttInstance instance) {
        log.error("Seating failed: " + mttSeatingFailedAction);
    }

    @Override
    public void tournamentCreated(MttInstance instance) {
        log.info("Tournament created: " + instance);
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            prepareTournament(instance).tournamentCreated();
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void tournamentDestroyed(MttInstance instance) {
        log.debug("Tournament " + instance + " destroyed.");
    }

    @Override
    public MttRegisterResponse register(MttInstance instance, MttRegistrationRequest request) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            return prepareTournament(instance).checkRegistration(request);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public MttRegisterResponse unregister(MttInstance instance, int pid) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            return prepareTournament(instance).checkUnregistration(pid);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void playerRegistered(MttInstance instance, MttRegistrationRequest request) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            prepareTournament(instance).playerRegistered(request);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    @Override
    public void playerUnregistered(MttInstance instance, int pid) {
        try {
            MDC.put(MDC_TAG, "Tournament[" + instance.getId() + "]");
            prepareTournament(instance).playerUnregistered(pid);
        } finally {
            MDC.remove(MDC_TAG);
        }
    }

    public void setSupport(TournamentAssist support) {
        this.support = support;
    }

    private void injectDependencies(PokerTournament tournament, MttInstance instance) {
        initializeServices(instance);

        PacketSender sender = senderFactory.create(instance.getMttNotifier(), instance);
        tournament.injectTransientDependencies(instance, support, util.getStateSupport(instance), historyService,
                backend, dateFetcher, shutdownService, tournamentPlayerRegistry, sender, userService, domainEventService);
    }

    private void initializeServices(MttInstance instance) {
        if (historyService == null) {
            historyService = instance.getServiceRegistry().getServiceInstance(TournamentHistoryPersistenceService.class);
        }
        if (backend == null) {
            backend = instance.getServiceRegistry().getServiceInstance(CashGamesBackendService.class);
        }
        if (shutdownService == null) {
            shutdownService = instance.getServiceRegistry().getServiceInstance(ShutdownServiceContract.class);
        }
        if (tournamentPlayerRegistry == null) {
            tournamentPlayerRegistry = instance.getServiceRegistry().getServiceInstance(TournamentPlayerRegistry.class);
        }
        if (domainEventService == null) {
        	domainEventService = instance.getServiceRegistry().getServiceInstance(DomainEventsService.class);
        }
    }

    private PokerTournament prepareTournament(MttInstance instance) {
        PokerTournament tournament = (PokerTournament) instance.getState().getState();
        injectDependencies(tournament, instance);
        return tournament;
    }

    private TournamentLobby prepareTournamentLobby(MttInstance instance) {
        if (backend == null) {
            backend = instance.getServiceRegistry().getServiceInstance(CashGamesBackendService.class);
        }
        return lobbyFactory.create(instance, util.getStateSupport(instance), util.getPokerState(instance), backend);
    }

    @VisibleForTesting
    public void setHistoryService(TournamentHistoryPersistenceService historyService) {
        this.historyService = historyService;
    }
    
    @VisibleForTesting
    public void setUserService(UserServiceContract userService) {
		this.userService = userService;
	}

    @VisibleForTesting
    public void setBackend(CashGamesBackendService backend) {
        this.backend = backend;
    }

    @VisibleForTesting
    public void setShutdownService(ShutdownServiceContract shutdownService) {
        this.shutdownService = shutdownService;
    }

    @VisibleForTesting
    void setDateFetcher(SystemTime dateFetcher) {
        this.dateFetcher = dateFetcher;
    }

    @VisibleForTesting
    public void setSenderFactory(PacketSenderFactory senderFactory) {
        this.senderFactory = senderFactory;
    }

    @VisibleForTesting
    public void setTournamentRegistryService(TournamentPlayerRegistry tournamentPlayerRegistry) {
        this.tournamentPlayerRegistry = tournamentPlayerRegistry;
    }
}
