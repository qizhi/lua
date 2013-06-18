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

package com.cubeia.games.poker.admin.wicket.pages.wallet;

import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class InvalidAccount extends BasePage {
    public static final String PARAM_ACCOUNT_ID = "accountId";  

    private long accountId;

    public InvalidAccount(PageParameters parameters) {
        super(parameters);
        accountId = parameters.get(PARAM_ACCOUNT_ID).toLong();
        add(new Label("accountId", "" + accountId));
    }
    
    @Override
    public String getPageTitle() {
        return "Invalid account (" + accountId + ")";
    }

}
