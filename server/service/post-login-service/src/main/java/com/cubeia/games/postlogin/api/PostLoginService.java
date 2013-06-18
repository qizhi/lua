/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.postlogin.api;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.PostLoginProcessor;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.games.poker.tournament.messages.PlayerLeft;
import com.cubeia.network.users.firebase.api.UserServiceContract;

public class PostLoginService implements PostLoginProcessor, Service, RoutableService {

    private static final Logger log = Logger.getLogger(PostLoginService.class);

    private TournamentPlayerRegistry tournamentPlayerRegistry;

    private ServiceRouter router;

	private ServiceContext context;

    @Override
    public void clientDisconnected(int playerId) {
        log.info("Player " + playerId + " disconnected.");
        unregisterFromSitAndGoTournaments(playerId);
        invalidatePlayerSession(playerId);
    }

    @Override
    public void clientLoggedIn(int playerId, String screenName) {}

    @Override
    public void clientLoggedOut(int playerId) {
        log.info("Player " + playerId + " logged out.");
        unregisterFromSitAndGoTournaments(playerId);
        invalidatePlayerSession(playerId);
    }

	@Override
    public void destroy() {

    }

    @Override
    public void init(ServiceContext context) throws SystemException {
        this.context = context;
		tournamentPlayerRegistry = context.getParentRegistry().getServiceInstance(TournamentPlayerRegistry.class);
    }

    @Override
    public void setRouter(ServiceRouter router) {
        this.router = router;
    }

    @Override
    public void onAction(ServiceAction e) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    private void unregisterFromSitAndGoTournaments(int playerId) {
        int[] tournamentsForPlayer = tournamentPlayerRegistry.getTournamentsForPlayer(playerId);
        for (int tournamentId : tournamentsForPlayer) {
            log.debug("Notifying tournament " + tournamentId + " that player " + playerId + " left.");
            MttAction unregisterAction = new MttObjectAction(tournamentId, new PlayerLeft(playerId));
            router.dispatchToTournament(tournamentId, unregisterAction);
        }
    }
    
    private void invalidatePlayerSession(int playerId) {
        try {
            UserServiceContract userService = context.getParentRegistry().getServiceInstance(UserServiceContract.class);
            if (userService != null) {
                log.debug("Invalidate player session ("+playerId+") now");
                userService.invalidateUserSession(playerId);
                log.info("Invalidated player session ("+playerId+")");
            } else {
                log.warn("User service is null so I will skip remote session invalidation");
            }
        } catch (Throwable e) {
            log.error("Failed to invalidate player session against remote User Service. If you are running locally with firebase:run this is expected. PlayerId("+playerId+"). Exception: " +e);
        }
    }
}
