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

import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.user.EditUser;
import com.cubeia.games.poker.admin.wicket.pages.util.DateDisplayModel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.ParamBuilder;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;

@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class EditAccount extends BasePage {
    private static final long serialVersionUID = 1L;

    public static final String PARAM_ACCOUNT_ID = "accountId";
    
    private static Logger log = LoggerFactory.getLogger(EditAccount.class);

    @SpringBean(name="userClient")
    private UserServiceClient userService;
    
    @SpringBean(name="walletClient")
    private WalletServiceClient walletService;

    private Account account;

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public EditAccount(PageParameters parameters) {
        super(parameters);
        final Long accountId = parameters.get(PARAM_ACCOUNT_ID).toLongObject();
        loadFormData(accountId);

        if (account == null) {
            setInvalidUserResponsePage(accountId);
            return;
        }
        
        add(createCloseActionLink(accountId));
        add(new LabelLinkPanel("transactionsLink", "View transactions",
            TransactionList.class, params(TransactionList.PARAM_ACCOUNT_ID, account.getId())));
        
        
        Form<?> accountForm = new Form<Void>("accountForm") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit() {
                walletService.updateAccount(account);
                info("account updated, id = " + accountId);
                loadFormData(accountId);
            }
        };
        
        CompoundPropertyModel<Account> cpm = new CompoundPropertyModel<Account>(new PropertyModel<Account>(this, "account"));
        
        accountForm.add(new Label("accountId", cpm.<Long>bind("id")));
        accountForm.add(new TextField<Long>("userId", cpm.<Long>bind("userId")));
        accountForm.add(new TextField<String>("name", cpm.<String>bind("information.name")));
        accountForm.add(new Label("status", cpm.<AccountStatus>bind("status")));
        accountForm.add(new Label("created", new DateDisplayModel(cpm.<Date>bind("created"))));
        accountForm.add(new Label("closed", new DateDisplayModel(cpm.<Date>bind("closed"))));
        accountForm.add(new TextField<Long>("gameId", cpm.<Long>bind("information.gameId")));
        accountForm.add(new TextField<Long>("objectId", cpm.<Long>bind("information.objectId")));
        accountForm.add(new CheckBox("negativeAmountAllowed",  cpm.<Boolean>bind("negativeAmountAllowed")));
        accountForm.add(new DropDownChoice<AccountType>(
            "type", 
            cpm.<AccountType>bind("type"),
            Arrays.asList(AccountType.values())));
        
        Link<?> editUserLink = new Link<Void>("editUserLink") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                setResponsePage(EditUser.class, ParamBuilder.params(EditUser.PARAM_USER_ID, getAccount().getUserId()));
            }
            
            @Override
            public boolean isEnabled() {
                User u = getUserIfUserServiceIsRunning(getAccount().getUserId());
                return (u != null  &&  u.getStatus() != UserStatus.REMOVED);
            }
        };
        accountForm.add(editUserLink);
        
        
        add(accountForm);
        add(new FeedbackPanel("feedback"));
    }

    private void setInvalidUserResponsePage(final Long accountId) {
        setResponsePage(InvalidAccount.class, params(InvalidAccount.PARAM_ACCOUNT_ID, accountId));
    }

    private Link<?> createCloseActionLink(final Long accountId) {
        Link<?> blockActionLink = new Link<Void>("closeActionLink") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                if (account.getStatus() == AccountStatus.OPEN) {
                    try {
                        walletService.closeAccount(account.getId());
                    } catch (AccountNotFoundException e) {
                        log.error("error closing account", e);
                    }
                } else {
                    walletService.openAccount(account.getId());
                }
                
                loadFormData(accountId);
            }
        };
        
        blockActionLink.add(new Label("closeActionLabel", new Model<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                if (AccountStatus.OPEN == getAccount().getStatus()) {
                    return "Close account";
                } else {
                    return "Open account";
                }
            }
        }));
        return blockActionLink;
    }

    public Account getAccount() {
        return account;
    }

    private User getUserIfUserServiceIsRunning(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            log.info("couldn't reach user service, user edit is disabled. Error: " + e.getMessage());
        }
        return null;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    private void loadFormData(Long accountId) {
        account = walletService.getAccountById(accountId);
    }

    @Override
    public String getPageTitle() {
        String name = "no name";
        if (account.getInformation() != null) {
            name = account.getInformation().getName();
        }
        
        return "Edit account: " + name + " (" + getAccount().getId() + ")";
    }
}
