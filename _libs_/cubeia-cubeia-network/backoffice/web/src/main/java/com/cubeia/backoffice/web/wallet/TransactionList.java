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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.wallet.api.dto.Entry;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.TransactionsOrder;
import com.cubeia.backoffice.wallet.api.dto.request.ListTransactionsRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.web.BasePage;
import com.cubeia.backoffice.web.util.DatePropertyColumn;
import com.cubeia.backoffice.web.util.LabelLinkPanel;

@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class TransactionList extends BasePage {
    private static final long serialVersionUID = 1L;

    protected static final String PARAM_ACCOUNT_ID = "accountId";
    
    private static Logger log = LoggerFactory.getLogger(TransactionList.class);

    @SpringBean(name="client.wallet-service")
    private WalletServiceClient walletService;

    private Date startDate = null;
    private Date endDate = null;
    
    private Long id1;
    private Long id2;
    
    private Boolean id1ByUserId = true;
    private Boolean id2ByUserId = true;
    
    private enum DEBIT_CREDIT {
        DEBIT(false), CREDIT(true), BOTH(null);
        public Boolean boolValue;
        DEBIT_CREDIT(Boolean boolValue) { this.boolValue = boolValue; }
    }
    
    private DEBIT_CREDIT id1Direction = DEBIT_CREDIT.BOTH;
    private DEBIT_CREDIT id2Direction = DEBIT_CREDIT.CREDIT;
    
    /**
     * Transaction entry column containing a list view of the entry data.
     * @author w
     */
	private final class TxEntryColumn extends AbstractColumn<Transaction,String> {
        private static final long serialVersionUID = 1L;

        private TxEntryColumn(IModel<String> displayModel) {
            super(displayModel);
        }

        @Override
        public void populateItem(Item<ICellPopulator<Transaction>> item, String componentId, IModel<Transaction> model) {
            Collection<Entry> entries = model.getObject().getEntries();
            
            // get id's to highlight
            ArrayList<Long> accountIds = new ArrayList<Long>();
            ArrayList<Long> userIds = new ArrayList<Long>();
            if (getId1ByUserId()) {
                userIds.add(getId1());
            } else {
                accountIds.add(getId1());
            }
            
            if (getId2ByUserId()) {
                userIds.add(getId2());
            } else {
                accountIds.add(getId2());
            }
            
            item.add(new EntriesPanel(accountIds, userIds, componentId, entries));
        }

        @Override
        public boolean isSortable() {
            return false;
        }
    }

    private final class TxDataProvider extends SortableDataProvider<Transaction,String> {
        private static final long serialVersionUID = 1L;
        
        public TxDataProvider() {
            setSort(TransactionsOrder.ID.name(), SortOrder.DESCENDING);
        }
        
        @Override
        public void detach() {
            super.detach();
        }
        
        @Override
        public Iterator<Transaction> iterator(long first, long count) {
            SortParam<String> sort = getSort();
            TransactionQueryResult result = getTransactionList(
                    getId1(), getId1Direction().boolValue, getId1ByUserId(),
                    getId2(), getId2Direction().boolValue, getId2ByUserId(),
                    getStartDate(), getEndDate(), (int)first,(int) count,
                    sort.getProperty(), sort.isAscending()); 
            return result == null || result.getTransactions() == null ? null :result.getTransactions().iterator();
        }

        @Override
        public IModel<Transaction> model(Transaction tx) {
            return Model.of(tx);
        }

        @Override
        public long size() {
            return getTransactionList(
                getId1(), getId1Direction().boolValue, getId1ByUserId(),
                getId2(), getId2Direction().boolValue, getId2ByUserId(),
                getStartDate(), getEndDate(), 0, 1, null, true).getTotalQueryResultSize();
        }
    }

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public TransactionList(final PageParameters parameters) {
        long accountIdParam = parameters.get(PARAM_ACCOUNT_ID).toLong(-1);
        long userIdParam = parameters.get("userId").toLong(-1);
        if (accountIdParam != -1) {
            setId1(accountIdParam);
            setId1ByUserId(false);
            setId1Direction(DEBIT_CREDIT.BOTH);
        } else if (userIdParam != -1) {
            setId1(userIdParam);
            setId1ByUserId(true);
            setId1Direction(DEBIT_CREDIT.BOTH);
        }
        
        Form<TransactionList> searchForm = new Form<TransactionList>("searchForm", new CompoundPropertyModel<TransactionList>(this));

        searchForm.add(new TextField<Long>("id1"));
        searchForm.add(new TextField<Long>("id2"));
        
        RadioGroup<Boolean> id1RadioGroup = new RadioGroup<Boolean>("id1Group", new PropertyModel<Boolean>(this, "id1ByUserId"));
        id1RadioGroup.add(new Radio<Boolean>("id1ByUser", new Model<Boolean>(Boolean.TRUE)));
        id1RadioGroup.add(new Radio<Boolean>("id1ByAccount", new Model<Boolean>(Boolean.FALSE)));
        searchForm.add(id1RadioGroup);

        RadioGroup<Boolean> id2RadioGroup = new RadioGroup<Boolean>("id2Group", new PropertyModel<Boolean>(this, "id2ByUserId"));
        id2RadioGroup.add(new Radio<Boolean>("id2ByUser", new Model<Boolean>(Boolean.TRUE)));
        id2RadioGroup.add(new Radio<Boolean>("id2ByAccount", new Model<Boolean>(Boolean.FALSE)));
        searchForm.add(id2RadioGroup);
        
        DateField startDatePicker = new DateField("startDate", new PropertyModel<Date>(this, "startDate"));
        searchForm.add(startDatePicker);
        DateField endDatePicker = new DateField("endDate", new PropertyModel<Date>(this, "endDate"));
        searchForm.add(endDatePicker);
        
        searchForm.add(new DropDownChoice<DEBIT_CREDIT>("id1Direction", new PropertyModel<DEBIT_CREDIT>(this, "id1Direction"), 
            Arrays.asList(DEBIT_CREDIT.values())));
        searchForm.add(new DropDownChoice<DEBIT_CREDIT>("id2Direction", new PropertyModel<DEBIT_CREDIT>(this, "id2Direction"), 
            Arrays.asList(DEBIT_CREDIT.values())));
        
        add(searchForm);
        add(new FeedbackPanel("feedback"));
        
    	ISortableDataProvider<Transaction,String> dataProvider = new TxDataProvider();
    	List<IColumn<Transaction,String>> columns = new ArrayList<IColumn<Transaction,String>>();
        
        columns.add(new PropertyColumn<Transaction,String>(Model.of("Tx Id"), TransactionsOrder.ID.name(), "id") {
            private static final long serialVersionUID = 1L;
            @Override
            public void populateItem(Item<ICellPopulator<Transaction>> item, String componentId, IModel<Transaction> model) {
                Transaction tx = (Transaction) model.getObject();
                Long txId = tx.getId();
                PageParameters pageParams = new PageParameters();
                pageParams.set("transactionId", txId);
                item.add(new LabelLinkPanel(
                    componentId, 
                    "" + txId, 
                    "transaction details for tx id " + txId,
                    TransactionInfo.class,
                    pageParams));
            }
        });
        columns.add(new DatePropertyColumn<Transaction,String>(new Model<String>("Date"), "timestamp"));
        columns.add(new PropertyColumn<Transaction,String>(Model.of("Comment"), "comment"));
        columns.add(new TxEntryColumn(new Model<String>("Entries")));

    	AjaxFallbackDefaultDataTable<Transaction,String> txTable = new AjaxFallbackDefaultDataTable<Transaction,String>(
    	    "txTable", columns, dataProvider , 20);
    	add(txTable);
    }

    public Long getId1() {
        return id1;
    }
    
    public void setId1(Long fromAccountId) {
        this.id1 = fromAccountId;
    }
    
    public Long getId2() {
        return id2;
    }
    
    public void setId2(Long toAccountId) {
        this.id2 = toAccountId;
    }
    
	public Date getStartDate() {
        return startDate;
    }
	
	public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
	
	public Date getEndDate() {
        return endDate;
    }
	
	public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
	
    public DEBIT_CREDIT getId1Direction() {
        return id1Direction;
    }

    public void setId1Direction(DEBIT_CREDIT account1Direction) {
        this.id1Direction = account1Direction;
    }

    public DEBIT_CREDIT getId2Direction() {
        return id2Direction;
    }

    public void setId2Direction(DEBIT_CREDIT account2Direction) {
        this.id2Direction = account2Direction;
    }
    
    public void setId1ByUserId(Boolean id1ByUserId) {
        this.id1ByUserId = id1ByUserId;
    }
    
    public Boolean getId1ByUserId() {
        return id1ByUserId;
    }
    
    public void setId2ByUserId(Boolean id2ByUserId) {
        this.id2ByUserId = id2ByUserId;
    }
    
    public Boolean getId2ByUserId() {
        return id2ByUserId;
    }

    private TransactionQueryResult getTransactionList(
        Long id1, 
        Boolean id1Credit,
        boolean id1IsUserId,
        Long id2, 
        Boolean id2Credit,
        boolean id2IsUserId,
        Date startDate, Date endDate, 
        int offset, int limit, 
        String sortProperty, boolean ascending) {
	    TransactionsOrder sortOrder = convertPropertyToSortOrder(sortProperty);
	    
	    ListTransactionsRequest requestTx = new ListTransactionsRequest();
	    requestTx.setId1(id1);
	    requestTx.setId1credit(id1Credit);
	    requestTx.setId1IsUserId(id1IsUserId);
	    requestTx.setId1(id2);
	    requestTx.setId1credit(id2Credit);
	    requestTx.setId1IsUserId(id2IsUserId);
		requestTx.setOffset(offset);
		requestTx.setLimit(limit);
		requestTx.setOrder(sortOrder);
		requestTx.setAscending(ascending);
        requestTx.setStartDate(getStartDate());
        requestTx.setEndDate(getEndDate());
		
        log.debug("sending tx-list request: {}", requestTx);
        
        return walletService.listTransactions(requestTx);
	}

    private TransactionsOrder convertPropertyToSortOrder(String sortProperty) {
        return sortProperty == null ? null : TransactionsOrder.valueOf(sortProperty);
    }
    
    @Override
    public String getPageTitle() {
        return "Transaction list";
    }
}
