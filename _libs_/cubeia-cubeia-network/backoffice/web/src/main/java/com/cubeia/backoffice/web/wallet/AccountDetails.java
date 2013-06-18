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

import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Entry;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.request.ListEntriesRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.web.BasePage;
import com.cubeia.backoffice.web.user.EditUser;
import com.cubeia.backoffice.web.util.LabelLinkPanel;
import com.cubeia.backoffice.web.util.ParamBuilder;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.cubeia.backoffice.web.util.ParamBuilder.params;

/**
 */
@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class AccountDetails extends BasePage {
    private static final long serialVersionUID = 1L;
    
    public static final String PARAM_ACCOUNT_ID = "accountId";
    
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(AccountDetails.class);

    @SpringBean(name="client.wallet-service")
    private WalletServiceClient walletService;
    
    private Account account;
    
    private final class EntriesDataProvider extends SortableDataProvider<Entry,String> {
        private static final long serialVersionUID = 1L;
        
        public EntriesDataProvider() {
            setSort(TransactionsOrder.ID.name(), SortOrder.DESCENDING);
        }
        
        @Override
        public void detach() {
            super.detach();
        }
        
        @Override
        public Iterator<Entry> iterator(long first, long count) {
            SortParam sort = getSort();
            return getEntries(getAccount().getId(), (int)first, (int)count, sort.isAscending()).getEntries().iterator();
        }

        @Override
        public IModel<Entry> model(Entry e) {
            return Model.of(e);
        }

        @Override
        public long size() {
            return getEntries(getAccount().getId(), 0, 0, true).getTotalQueryResultSize();
        }
    }
    
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param params
	 *            Page parameters
	 */
    public AccountDetails(PageParameters params) {
        Long accountId = params.get(PARAM_ACCOUNT_ID).toLongObject();
        account = walletService.getAccountById(accountId);

        if (getAccount() == null) {
            setInvalidAccountResponsePage(accountId);
            return;
        }
        
        Long accountUserId = getAccount().getUserId();

        add(new LabelLinkPanel(
            "editAccount", 
            "edit", 
            EditAccount.class, 
            ParamBuilder.params(EditAccount.PARAM_ACCOUNT_ID, accountId)));
        
        CompoundPropertyModel<?> cpm = new CompoundPropertyModel<AccountDetails>(this);
        add(new Label("balance", cpm.bind("balance")));
        add(new Label(PARAM_ACCOUNT_ID, cpm.bind("account.id")));
        add(new Label("userId", cpm.bind("account.userId")));
        add(new Label("name", cpm.bind("account.information.name")));
        add(new Label("currency", cpm.bind("account.currencyCode")));
        add(new Label("status", cpm.bind("account.status")));
        add(new Label("created", cpm.bind("account.created")));
        add(new Label("closed", cpm.bind("account.closed")));
        add(new Label("gameId", cpm.bind("account.information.gameId")));
        add(new Label("objectId", cpm.bind("account.information.objectId")));
        add(new Label("type", cpm.bind("account.type")));
        add(new Label("negativeAmountAllowed", cpm.bind("account.negativeAmountAllowed")));
        
        add(new LabelLinkPanel(
            "editUser", 
            "edit", 
            EditUser.class, 
            ParamBuilder.params(EditUser.PARAM_USER_ID, accountUserId)));
        
        ISortableDataProvider<Entry,String> dataProvider = new EntriesDataProvider();
        List<IColumn<Entry,String>> columns = new ArrayList<IColumn<Entry,String>>();
        
        columns.add(new PropertyColumn<Entry,String>(Model.of("Tx id"), TransactionsOrder.ID.name(), "transactionId") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void populateItem(Item<ICellPopulator<Entry>> item, String componentId, IModel<Entry> rowModel) {
                Long txId = rowModel.getObject().getTransactionId();
                PageParameters pageParams = new PageParameters();
                pageParams.set("transactionId", txId);
                item.add(new LabelLinkPanel(componentId, "" + txId, TransactionInfo.class, pageParams));
            }
        });
        
        columns.add(new PropertyColumn<Entry,String>(Model.of("Date"), "timestamp") {
            private static final long serialVersionUID = 1L;
            @Override
            public void populateItem(Item<ICellPopulator<Entry>> item, String componentId, IModel<Entry> model) {
                item.add(new Label(componentId, formatDate(model.getObject().getTimestamp()))); 
            }
        });
        
        columns.add(new PropertyColumn<Entry,String>(Model.of("Comment"), "transactionComment"));
        columns.add(new PropertyColumn<Entry,String>(Model.of("Amount"), "amount"));
        columns.add(new PropertyColumn<Entry,String>(Model.of("Resulting balance"), "resultingBalance"));

        AjaxFallbackDefaultDataTable<Entry,String> entryTable = new AjaxFallbackDefaultDataTable<Entry,String>("entryTable", columns, dataProvider , 20);
        add(entryTable);
    }

    private void setInvalidAccountResponsePage(final Long accountId) {
        setResponsePage(InvalidAccount.class, params(InvalidAccount.PARAM_ACCOUNT_ID, accountId));
    }
    
    private EntriesQueryResult getEntries(Long accountId, int offset, int limit, boolean ascending) {
    	ListEntriesRequest request = new ListEntriesRequest();
    	request.setAccountId(accountId);
    	request.setIncludeBalances(true);
    	request.setLimit(limit);
    	request.setOffset(offset);
    	request.setAscending(ascending);
    	
        return walletService.listEntries(request);
    }
    
    private Account getAccount() {
        return account;
    }
    
    public BigDecimal getBalance() {
        Long accountId = getAccount().getId();
        BigDecimal amount;
        try {
            amount = walletService.getAccountBalance(accountId).getBalance().getAmount();
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        return amount;
    }
    
    @Override
    public String getPageTitle() {
        return "Account details: " + getAccount().getId() + " (user " + getAccount().getUserId() + ")";
    }

}
