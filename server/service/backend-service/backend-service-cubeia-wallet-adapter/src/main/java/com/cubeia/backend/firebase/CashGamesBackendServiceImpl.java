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

package com.cubeia.backend.firebase;

import com.cubeia.backend.cashgame.CashGamesBackend;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.network.wallet.firebase.api.WalletServiceContract;
import org.apache.log4j.Logger;

import static com.cubeia.backend.cashgame.dto.OpenTournamentSessionRequest.TOURNAMENT_ACCOUNT;
import static java.util.Collections.singleton;

public class CashGamesBackendServiceImpl extends CashGamesBackendServiceBase implements CashGamesBackendService, Service, RoutableService {

    private static final Logger log = Logger.getLogger(CashGamesBackendServiceImpl.class);
    private CashGamesBackendAdapter adapter;
    private ServiceRouter router;

    public CashGamesBackendServiceImpl() {
        super(20, 500);
    }

    @Override
    protected CashGamesBackend getCashGamesBackend() {
        return adapter;
    }

    @Override
    protected ServiceRouter getServiceRouter() {
        return router;
    }

    @Override
    public void init(ServiceContext con) throws SystemException {
        WalletServiceContract walletService = con.getParentRegistry().getServiceInstance(WalletServiceContract.class);
        closeOpenSessionAccounts(walletService);
        adapter = new CashGamesBackendAdapter(walletService, new AccountLookupUtil());
    }

    private void closeOpenSessionAccounts(WalletServiceContract walletService) {
        log.debug("Attempting to close open accounts.");
        try {
            walletService.closeOpenSessionAccounts(singleton(TOURNAMENT_ACCOUNT));
        } catch (Exception e) {
            log.error("Failed closing open session accounts due to: " + e + ". This could be because the wallet service is not running.");
        }
    }

    @Override
    public void setRouter(ServiceRouter router) {
        this.router = router;
    }

    @Override
    public void onAction(ServiceAction e) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

}
