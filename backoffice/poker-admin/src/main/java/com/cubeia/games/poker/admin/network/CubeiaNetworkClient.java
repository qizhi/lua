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

package com.cubeia.games.poker.admin.network;

import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;
import com.cubeia.backoffice.wallet.client.WalletServiceClientHTTP;
import org.apache.log4j.Logger;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class CubeiaNetworkClient implements NetworkClient {
    
    private static final Logger log = Logger.getLogger(CubeiaNetworkClient.class);

    private WalletServiceClientHTTP walletClient;
    
    private OperatorServiceClient operatorService;

    @Override
    public List<OperatorDTO> getOperators() {
        List<OperatorDTO> ops;
        
        try {
            log.info("Retrieve Operators");
            ops = operatorService.getOperators();
        } catch (Exception e) {
            log.warn("Failed to retrieve operators from operator-service. The Exeption was:\n", e);    
            ops = newArrayList();
            log.info("Continuing with default operators (no operators).");
        }
        
        return ops;
    }
    
    @Override
    public List<String> getCurrencies() {
        List<String> currencies = newArrayList();
        try {
            log.info("Retrieve Supported Currencies");
            CurrencyListResult supportedCurrencies = walletClient.getSupportedCurrencies();
            for (Currency currency : supportedCurrencies.getCurrencies()) {
                currencies.add(currency.getCode());
            }
        } catch(Exception e) {
            log.warn("Failed to retrieve supported currencies from wallet service. The Exeption was:\n", e);
            //TODO: possibly fetch defaults from system-wide config-file
            currencies.add("EUR");
            log.info("Continuing with default currencies.");
        }
        return currencies;
    }
    
    public void setOperatorService(OperatorServiceClient operatorService) {
        this.operatorService = operatorService;
    }

    public void setWalletClient(WalletServiceClientHTTP walletClient) {
        this.walletClient = walletClient;
    }
}
