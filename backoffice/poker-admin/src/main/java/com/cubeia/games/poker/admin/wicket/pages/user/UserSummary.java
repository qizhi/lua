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

package com.cubeia.games.poker.admin.wicket.pages.user;

import com.cubeia.backoffice.users.api.dto.Gender;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.util.DateDisplayModel;
import com.cubeia.games.poker.admin.wicket.pages.util.OddEvenRowsAttributeModifier;
import com.cubeia.games.poker.admin.wicket.pages.wallet.AccountDetails;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;


/**
 */
@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class UserSummary extends BasePage {
    private static final long serialVersionUID = 1L;

    public static final String PARAM_USER_ID = "userId";
    
    private static Logger log = LoggerFactory.getLogger(UserSummary.class);

    @SpringBean(name="userClient")
    private UserServiceClient userService;
    
    @SpringBean(name="walletClient")
    private WalletServiceClient walletService;
    
    private User user;     
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param params
	 *            Page parameters
	 */
    public UserSummary(PageParameters params) {
        super(params);
    	if (!assertValidUserid(params)) {
    		return;
    	}
    	
        final Long userId = params.get(PARAM_USER_ID).toLongObject();
        loadFormData(userId);

        if (getUser() == null  ||  getUser().getStatus() == UserStatus.REMOVED) {
            log.debug("user is removed, id = " + userId);
            setInvalidUserResponsePage(userId);
            return;
        } 
        
        CompoundPropertyModel<UserSummary> cpm = new CompoundPropertyModel<UserSummary>(this);
        
        add(new Label("userId", cpm.<Long>bind("user.userId")));
        add(new Label("status", cpm.<UserStatus>bind("user.status")));
        add(new Label("operatorId", cpm.<Long>bind("user.operatorId")));
        add(new Label("externalUserId", cpm.<String>bind("user.externalUserId")));
        add(new Label("userName", cpm.<String>bind("user.userName")));
        add(new Label("firstName",cpm.<String>bind("user.userInformation.firstName")));
        add(new Label("lastName", cpm.<String>bind("user.userInformation.lastName")));
        add(new Label("email", cpm.<String>bind("user.userInformation.email")));
        add(new Label("userTitle", cpm.<String>bind("user.userInformation.title")));
        add(new Label("city", cpm.<String>bind("user.userInformation.city")));
        add(new Label("billingAddress", cpm.<String>bind("user.userInformation.billingAddress")));
        add(new Label("fax", cpm.<String>bind("user.userInformation.fax")));
        add(new Label("cellphone", cpm.<String>bind("user.userInformation.cellphone")));
        add(new Label("country", cpm.<String>bind("user.userInformation.country")));        
        add(new Label("zipcode", cpm.<String>bind("user.userInformation.zipcode")));
        add(new Label("state", cpm.<String>bind("user.userInformation.state")));
        add(new Label("phone", cpm.<String>bind("user.userInformation.phone")));
        add(new Label("workphone", cpm.<String>bind("user.userInformation.workphone")));
        add(new Label("gender", cpm.<Gender>bind("user.userInformation.gender")));        
        add(new FeedbackPanel("feedback"));        
        add(createAttributesListView());
         
        
        
        ListAccountsRequest request = new ListAccountsRequest();
        request.setUserId(userId);
        request.setStatus(AccountStatus.OPEN);
        request.setLimit(100);
        log.debug("sending accounts request: {}", request);
		AccountQueryResult listAccounts = walletService.listAccounts(request);
		add(createAccountListView(listAccounts)); 
        
        add(new BookmarkablePageLink<Void>("editLink", EditUser.class, params).add(new Label("editLabel", "Edit user")));
    }

    private boolean assertValidUserid(PageParameters parameters) {
    	try {
    		parameters.get("userId").toLongObject();
    		return true;
    	} catch (Exception e) {
    		setResponsePage(InvalidUser.class, params(InvalidUser.PARAM_USER_ID, parameters.get(PARAM_USER_ID).toString()));
    		return false;
    	}
	}

	private void setInvalidUserResponsePage(final Long userId) {
        setResponsePage(InvalidUser.class, params(InvalidUser.PARAM_USER_ID, userId));
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user; 
    }
   
    private void loadFormData(Long userId) {
        user = userService.getUserById(userId);
    }
    
    @Override
    public String getPageTitle() {
        return "User summary: " + user.getUserName() + " (" + user.getUserId() + ")";
    }
    
    private ListView<String[]> createAttributesListView() {
        Model<ArrayList<String[]>> attributeModel = new Model<ArrayList<String[]>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public ArrayList<String[]> getObject() {
                ArrayList<String[]> keyValueList = new ArrayList<String[]>();
                
                if(user.getAttributes() != null){                
	                ArrayList<String> keysSorted = new ArrayList<String>(user.getAttributes().keySet());
	                Collections.sort(keysSorted);
	                for (String key : keysSorted) {
	                    keyValueList.add(new String[] {key, user.getAttributes().get(key)});
	                }
                }
                return keyValueList;
            }
        }; 
        
        return new ListView<String[]>("attributes", attributeModel) {
            private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String[]> item) {
				String[] keyValue = item.getModelObject();
                item.add(new Label("key", keyValue[0]));
                item.add(new Label("value", keyValue[1]));				
			}			            
        };
    }
    
    private ListView<Account> createAccountListView(AccountQueryResult accountResult) {
    	log.debug("account result: {}", accountResult);
        List<Account> accounts = accountResult.getAccounts();
        if(accounts == null) {
        	accounts = new ArrayList<Account>();
        }
		HashSet<Account> accountSet = new HashSet<Account>(accounts);
        List<Account> accountList = new ArrayList<Account>(accountSet);
        Collections.sort(accountList, Collections.reverseOrder(new Comparator<Account>() {
			@Override
			public int compare(Account a1, Account a2) {
				return a1.getType().name().compareTo(a2.getType().name());
			}
		}));
		return new ListView<Account>("accounts", Model.ofList(accountList)) {
            private static final long serialVersionUID = 1L;
            
			@SuppressWarnings("serial")
            @Override
			protected void populateItem(ListItem<Account> item) {
				final Account account = item.getModelObject();
				
				
				item.add(new LabelLinkPanel("accountId", "" + account.getId(), "View account " + account.getId(),
				    AccountDetails.class, params(AccountDetails.PARAM_ACCOUNT_ID, account.getId())));
				    
				item.add(new Label("name", new PropertyModel<String>(account, "information.name")));
				item.add(new Label("currency", new PropertyModel<String>(account, "currencyCode")));						
				item.add(new Label("created", new DateDisplayModel(account, "created")));
				item.add(new Label("type", new PropertyModel<String>(account, "type")));	
				
				item.add(new Label("balance", new Model<BigDecimal>() {
				    public BigDecimal getObject() {
                        BigDecimal amount;
                        try {
                            amount = walletService.getAccountBalance(account.getId()).getBalance().getAmount();
                        } catch (AccountNotFoundException e) {
                            throw new RuntimeException(e);
                        }                       
                        return amount;
				    };
				}));
				
				item.add(new OddEvenRowsAttributeModifier(item.getIndex()));
			}			            
        };
    }
}
