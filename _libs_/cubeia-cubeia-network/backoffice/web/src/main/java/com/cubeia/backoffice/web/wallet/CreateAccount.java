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

package com.cubeia.backoffice.web.wallet;

import java.util.Arrays;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.web.BasePage;

@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class CreateAccount extends BasePage {
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(CreateAccount.class);

    @SpringBean(name="client.wallet-service")
    private WalletServiceClient walletService;
    
    private Account account;
    private MetaInformation info;
    private String currency;
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public CreateAccount(final PageParameters parameters) {
        resetFormData();
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);
        
        
        Form<?> accountForm = new Form<Void>("userForm") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit() {
                CreateAccountRequest createAccountRequest = new CreateAccountRequest(null, account.getUserId(), currency, account.getType(), info);
                createAccountRequest.setNegativeBalanceAllowed(account.getNegativeAmountAllowed());
                
                CreateAccountResult createResponse = null;
                try {
                	createResponse = walletService.createAccount(createAccountRequest);
                } catch (Exception e) {
					log.error("Failed to create new account", e);
					feedback.error("Failed to create new account. Cause: " + e.getMessage());
					return;
				}
                log.debug("created account id = " + createResponse.getAccountId());
                feedback.info("Account created!");
            }
        };
        
        CompoundPropertyModel<CreateAccount> cpm = new CompoundPropertyModel<CreateAccount>(this);
        
        accountForm.add(new RequiredTextField<String>("userId", cpm.<String>bind("account.userId")));
        accountForm.add(new DropDownChoice<AccountType>(
                "accountType", 
                cpm.<AccountType>bind("account.type"), 
                Arrays.asList(AccountType.values())).setRequired(true));
        accountForm.add(new DropDownChoice<AccountStatus>(
                "accountStatus", 
                cpm.<AccountStatus>bind("account.status"), 
                Arrays.asList(AccountStatus.values())).setRequired(true));
        accountForm.add(new RequiredTextField<String>("currency", cpm.<String>bind("currency")));
        accountForm.add(new TextField<String>("name", cpm.<String>bind("info.name")));
        accountForm.add(new CheckBox("negativeAmountAllowed",  cpm.<Boolean>bind("account.negativeAmountAllowed")));
        
        
        add(accountForm);        
    }
    
    public Account getAccount() {
		return account;
	}



	public void setAccount(Account account) {
		this.account = account;
	}



	public MetaInformation getInfo() {
		return info;
	}



	public void setInfo(MetaInformation info) {
		this.info = info;
	}


	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	private void resetFormData() {
        account = new Account();
        info = new MetaInformation();     
        currency = null;
    }
    
    @Override
    public String getPageTitle() {
        return "Create account";
    }
}
