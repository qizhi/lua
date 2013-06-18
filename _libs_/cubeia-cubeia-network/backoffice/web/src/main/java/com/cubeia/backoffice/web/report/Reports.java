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

package com.cubeia.backoffice.web.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.cubeia.backoffice.report.ReportServlet;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.web.BasePage;

/**
 */
@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class Reports extends BasePage {
    private static final long serialVersionUID = 1L;
    
    @SpringBean(name="client.user-service")
    private UserServiceClient userService;
    
    @SpringBean(name="client.wallet-service")
    private WalletServiceClient walletService;
    
    public Reports(PageParameters params) {
    	add(new AjaxLink<String>("openAccounts"){
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				ListAccountsRequest request = new ListAccountsRequest();
	            request.setStatus(AccountStatus.OPEN);
	            request.setOffset(0);
	            request.setLimit(1000);
	            request.setSortOrder(AccountsOrder.CREATION_DATE);
	            request.setAscending(true);
	            
	            AccountQueryResult accountsResult = walletService.listAccounts(request);
	            ArrayList<AccountHolder> accounts = new ArrayList<AccountHolder>();
	            
	            for (Account a : accountsResult.getAccounts()) {
	                BigDecimal balance = BigDecimal.ZERO;
	                try {
	                    balance = walletService.getAccountBalance(a.getId()).getBalance().getAmount();
	                } catch (AccountNotFoundException e) {
	                    // do nothing
	                }
	                accounts.add(new AccountHolder(a, balance));
	            }
				
	            HttpServletRequest req = ((ServletWebRequest) getRequest()).getContainerRequest();
				req.getSession().setAttribute(ReportServlet.REPORTS_COLLECTION_DATA_SOURCE, accounts);
	            
				String url = getRequest().getContextPath() + "/reportbuilder/reports/open accounts?format=pdf";				
				target.appendJavaScript("document.location = '" + url + "'");
			}   		
    	}.setVisible(false));
    	
    	add(new AjaxLink<String>("userCount"){
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				UserQueryResult usersResult = userService.findUsers(null, null, 0, 10000, null, true);
	            Map<Long, Long> operatorUserCount = new HashMap<Long, Long>();
	            for (User u : usersResult.getUsers()) {
	                Long count = operatorUserCount.get(u.getOperatorId());
	                count = count == null ? 0l : count + 1;
	                operatorUserCount.put(u.getOperatorId(), count);
	            }
	            HttpServletRequest request = ((ServletWebRequest) getRequest()).getContainerRequest();
				request.getSession().setAttribute(ReportServlet.REPORTS_COLLECTION_DATA_SOURCE, operatorUserCount.entrySet());
				
				String url = getRequest().getContextPath() + "/reportbuilder/reports/users by operator?format=pdf";
				target.appendJavaScript("document.location = '" + url + "'");
			}   		
    	});
    }

    @Override
    public String getPageTitle() {
        return "Reports";
    }
    
    public class AccountHolder {
        private Account account;
        private BigDecimal balance;
        
        public AccountHolder(Account account, BigDecimal balance) {
            super();
            this.account = account;
            this.balance = balance;
        }

        public Account getAccount() {
            return account;
        }
        
        public void setAccount(Account account) {
            this.account = account;
        }
        
        public BigDecimal getBalance() {
            return balance;
        }
        
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }
}
