package com.cubeia.games.poker.admin.wicket;


import com.cubeia.games.poker.admin.wicket.pages.operator.EditOperator;
import com.cubeia.games.poker.admin.wicket.pages.operator.OperatorList;
import com.cubeia.games.poker.admin.wicket.pages.system.BroadcastMessage;
import com.cubeia.games.poker.admin.wicket.pages.system.Clients;
import com.cubeia.games.poker.admin.wicket.pages.history.HandHistory;
import com.cubeia.games.poker.admin.wicket.pages.history.ShowHand;
import com.cubeia.games.poker.admin.wicket.pages.rakes.CreateRake;
import com.cubeia.games.poker.admin.wicket.pages.rakes.EditRake;
import com.cubeia.games.poker.admin.wicket.pages.rakes.ListRakes;
import com.cubeia.games.poker.admin.wicket.pages.system.SystemManagement;
import com.cubeia.games.poker.admin.wicket.pages.tables.CreateTable;
import com.cubeia.games.poker.admin.wicket.pages.tables.EditTable;
import com.cubeia.games.poker.admin.wicket.pages.tables.ListTables;
import com.cubeia.games.poker.admin.wicket.pages.timings.CreateTiming;
import com.cubeia.games.poker.admin.wicket.pages.timings.EditTiming;
import com.cubeia.games.poker.admin.wicket.pages.timings.ListTimings;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.blinds.CreateOrEditBlindsStructure;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.blinds.ListBlindsStructures;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.history.SearchTournamentHistory;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.payouts.CreateOrEditPayoutStructure;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.payouts.ListPayoutStructures;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.scheduled.CreateTournament;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.scheduled.EditTournament;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.scheduled.ListTournaments;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.sitandgo.CreateSitAndGo;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.sitandgo.EditSitAndGo;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.sitandgo.ListSitAndGoTournaments;
import com.cubeia.games.poker.admin.wicket.pages.user.EditUser;
import com.cubeia.games.poker.admin.wicket.pages.user.UserList;
import com.cubeia.games.poker.admin.wicket.pages.user.UserSummary;
import com.cubeia.games.poker.admin.wicket.pages.wallet.*;
import com.cubeia.network.shared.web.wicket.navigation.PageNode;

import java.util.ArrayList;
import java.util.List;

import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.add;
import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.node;
import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.nodeWithChildren;

public class SiteMap {

    private final static List<PageNode> pages = new ArrayList<PageNode>();

    static {
        add(pages, "Home", HomePage.class, "icon-home");

        add(pages,"Hand History", HandHistory.class, "icon-signal",
                node("Show Hand", ShowHand.class,false));

        add(pages,"Tournaments", ListTournaments.class, "icon-list-alt",
                nodeWithChildren("Scheduled Tournaments", ListTournaments.class, "icon-calendar",
                    node("Create tournament", CreateTournament.class, false),
                    node("Edit Tournament", EditTournament.class, false)),
                nodeWithChildren("Sit & Go Tournaments", ListSitAndGoTournaments.class, "icon-tags",
                    node("Create Sit & Go", CreateSitAndGo.class, false),
                    node("Edit Sit & Go", EditSitAndGo.class, false)),
                nodeWithChildren("Blinds Structures", ListBlindsStructures.class, "icon-list-alt",
                        node("Create/Edit Blinds Structure", CreateOrEditBlindsStructure.class, false)),
                nodeWithChildren("Payout Structures", ListPayoutStructures.class, "icon-gift",
                        node("Create/Edit Payout Structure", CreateOrEditPayoutStructure.class, false)),
                node("Tournament History", SearchTournamentHistory.class, "icon-book")
                );

        add(pages,"Table Templates", ListTables.class, "icon-list-alt",
                node("Create Table Template", CreateTable.class),
                node("Edit Table Template", EditTable.class, false));
        add(pages, "Timing Configurations", ListTimings.class, "icon-list-alt",
                node("Create Timing Configuration", CreateTiming.class),
                node("Edit Timing Configuration", EditTiming.class, false));

        add(pages, "Rake Configurations", ListRakes.class, "icon-list-alt",
                node("Create Rake Configuration", CreateRake.class),
                node("Edit Rake Configuration", EditRake.class, false));

//TODO: define which pages should be visible
        add(pages, "Users", UserList.class, "icon-list-alt",
                node("User Summary", UserSummary.class, false),
                node("Edit User", EditUser.class, false));

        add(pages, "Accounting", AccountList.class, "icon-list-alt",
                node("Account Details", AccountDetails.class, false),
                node("List Accounts", AccountList.class),
                node("Create Account", CreateAccount.class),
                node("Edit Account", EditAccount.class, false),
                node("List Transactions", TransactionList.class),
                node("Transaction Info", TransactionInfo.class, false),
                node("Create Transaction", CreateTransaction.class),
                node("Supported Currencies", EditCurrencies.class));

        add(pages, "Operators", OperatorList.class, "icon-list-alt",
                node("Edit Operator", EditOperator.class, false));

        add(pages,"System Management", SystemManagement.class, "icon-hdd",
                node("Shutdown Management", SystemManagement.class, "icon-off"),
                node("Live Players", Clients.class, "icon-user"),
                node("Broadcast Message", BroadcastMessage.class, "icon-bullhorn"));
    }

    public static List<PageNode> getPages() {
        return pages;
    }




}
