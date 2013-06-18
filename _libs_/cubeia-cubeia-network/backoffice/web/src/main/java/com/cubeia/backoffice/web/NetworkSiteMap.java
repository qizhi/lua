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
package com.cubeia.backoffice.web;


import com.cubeia.backoffice.web.operator.CreateOperator;
import com.cubeia.backoffice.web.operator.EditOperator;
import com.cubeia.backoffice.web.operator.OperatorList;
import com.cubeia.backoffice.web.user.CreateUser;
import com.cubeia.backoffice.web.user.EditUser;
import com.cubeia.backoffice.web.user.UserList;
import com.cubeia.backoffice.web.user.UserSummary;
import com.cubeia.backoffice.web.wallet.*;
import com.cubeia.network.shared.web.wicket.navigation.PageNode;

import java.util.ArrayList;
import java.util.List;
import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.*;

public class NetworkSiteMap {

    private final static List<PageNode> pages = new ArrayList<PageNode>();

    static {
        add(pages,"Home",Home.class, "icon-home");
        add(pages,"Users", UserList.class, "icon-user",
                node("List Users", UserList.class, "icon-list-alt",true),
                node("Create User", CreateUser.class,"icon-tags",true),
                node("User Summary", UserSummary.class,"",false),
                node("Edit User", EditUser.class,"",false));

        add(pages, "Accounting", AccountList.class,"icon-list-alt",
                nodeWithChildren(node("List Accounts", AccountList.class,"icon-list-alt",true),
                        node("Account details", AccountDetails.class,"",false)
                ),
                node("Create Account", CreateAccount.class, "icon-tags", true),
                node("List Transactions", TransactionList.class,"icon-list-alt",true),
                node("Create Transaction", CreateTransaction.class, "icon-tags",true),
                node("Supported Currencies", EditCurrencies.class,"icon-book",true));


        add(pages, "Operators", OperatorList.class, "icon-book",
                nodeWithChildren(node("List Operators",OperatorList.class,"",true),
                        node("Edit Operator", EditOperator.class,"",false)),
                node("Create Operator",CreateOperator.class,"",true)
                );

    }

    public static List<PageNode> getPages() {
        return pages;
    }

}
