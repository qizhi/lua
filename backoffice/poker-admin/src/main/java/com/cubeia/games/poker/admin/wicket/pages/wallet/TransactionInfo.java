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

import com.cubeia.backoffice.wallet.api.dto.Entry;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.util.OddEvenRowsAttributeModifier;
import com.cubeia.games.poker.admin.wicket.util.ExternalLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;
import static org.apache.commons.httpclient.util.DateUtil.formatDate;

/**
 */
@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class TransactionInfo extends BasePage {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(TransactionInfo.class);

    @SpringBean(name="walletClient")
    private WalletServiceClient walletService;
    
    private Long transactionId;
    
    @SpringBean(name="transaction-attribute-link-props", required=false)
    private Map<String, String> transactionLinkTemplates;
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    @SuppressWarnings("serial")
    public TransactionInfo(PageParameters parameters) {
        super(parameters);
        transactionId = parameters.get("transactionId").toLongObject();
        Transaction tx = walletService.getTransactionById(transactionId);

        if (tx == null) {
            // TODO: create invalid tx page
            setInvalidTransactionResponsePage(transactionId);
            return;
        }
        
        add(new Label("txId", "" + transactionId));
        add(new Label("timestamp", "" + formatDate(tx.getTimestamp())));
        add(new Label("comment", "" + tx.getComment()));
        
        ArrayList<Map.Entry<String, String>> attribs;

        if (tx.getAttributes() != null) {
            attribs = new ArrayList<Map.Entry<String, String>>(tx.getAttributes().entrySet());
        } else {
            attribs = new ArrayList<Map.Entry<String,String>>();
        }

        ListView<Map.Entry<String, String>> attribList = new ListView<Map.Entry<String, String>>("attributeList", attribs) {
            @Override
            protected void populateItem(ListItem<Map.Entry<String, String>> item) {
                String key = item.getModelObject().getKey();
                String value = item.getModelObject().getValue();
                item.add(new Label("key", Model.of(key)));
            	String url = getAttributeLinkUrl(key, value);
                if(url == null) {
	                item.add(new Label("value", Model.of(value)));
                } else {
                	item.add(new ExternalLinkPanel("value", value, url));
                }
            }

			private String getAttributeLinkUrl(String key, String value) {
				if(transactionLinkTemplates != null && transactionLinkTemplates.containsKey(key)) {
					String templ = transactionLinkTemplates.get(key);
					return templ.replace("${value}", value);
				} else {
					return null;
				}
			}
        };
        add(attribList);
        
        
        ArrayList<Entry> entryList = new ArrayList<Entry>();
        
        if (tx.getEntries() != null) {
            entryList.addAll(tx.getEntries());
        }
        
        Collections.sort(entryList, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return o1.getAccountId().compareTo(o2.getAccountId());
            }
        });
        
        ListView<Entry> entryListView = new ListView<Entry>("entryList", entryList) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<Entry> item) {
                Entry e = (Entry) item.getModelObject();
                item.add(new Label("id", "" + e.getId()));
                
                LabelLinkPanel accountLink = new LabelLinkPanel("accountEntriesLink", "" + e.getAccountId(),
                    AccountDetails.class, params(AccountDetails.PARAM_ACCOUNT_ID, e.getAccountId()));
                item.add(accountLink);
                item.add(new Label("amount", "" + e.getAmount()));
                item.add(new Label("resultingBalance", "" + e.getResultingBalance()));
                item.add(new OddEvenRowsAttributeModifier(item.getIndex()));
            }
        };
        add(entryListView);
    }
    
    private void setInvalidTransactionResponsePage(final Long accountId) {
        PageParameters pageParams = new PageParameters();
        pageParams.set("transactionId", accountId);
        setResponsePage(InvalidTransaction.class, pageParams);
    }

    @Override
    public String getPageTitle() {
        return "Transaction information: " + transactionId;
    }
}
