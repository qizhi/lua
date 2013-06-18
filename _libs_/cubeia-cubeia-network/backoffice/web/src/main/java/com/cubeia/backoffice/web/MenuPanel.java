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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cubeia.backoffice.web.operator.CreateOperator;
import com.cubeia.backoffice.web.operator.OperatorList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.cubeia.backoffice.web.user.CreateUser;
import com.cubeia.backoffice.web.user.UserList;
import com.cubeia.backoffice.web.wallet.AccountList;
import com.cubeia.backoffice.web.wallet.CreateAccount;
import com.cubeia.backoffice.web.wallet.CreateTransaction;
import com.cubeia.backoffice.web.wallet.EditCurrencies;
import com.cubeia.backoffice.web.wallet.TransactionList;

public class MenuPanel extends Panel {
    private static final long serialVersionUID = 1L;
    
    @SpringBean(name="external-menu-links", required=false)
    private Map<String, String> externalMenuLinks;
    
	@SuppressWarnings("serial")
    public MenuPanel(String id, BasePage currentPage) {
		super(id);
		
		Class<? extends BasePage> currentPageClass = currentPage.getClass();
        add(createPageLink("homePage", Home.class, currentPageClass));
        add(createPageLink("userListPage", UserList.class, currentPageClass));
        add(createPageLink("createUserPage", CreateUser.class, currentPageClass));
        add(createPageLink("accountListPage", AccountList.class, currentPageClass));
        add(createPageLink("createAccountPage", CreateAccount.class, currentPageClass));
        add(createPageLink("createTransactionPage", CreateTransaction.class, currentPageClass));
        add(createPageLink("currenciesPage", EditCurrencies.class, currentPageClass));
        add(createPageLink("transactionListPage", TransactionList.class, currentPageClass));
        add(createPageLink("operatorListPage", OperatorList.class,currentPageClass));
        add(createPageLink("createOperatorPage", CreateOperator.class,currentPageClass));
        // add(createPageLink("reportsPage", Reports.class, currentPageClass));

        String signedInMessage;
        String signInOutMessage; 
        Link<Void> signInOutLink;
        
        BackofficeAuthSession session = currentPage.getBackofficeSession();
        if (session.isSignedIn()) {
            signedInMessage = "Logged in as " + session.getUserName();
            signInOutMessage = "log out";
            signInOutLink = new BookmarkablePageLink<Void>("signInOutLink", BackofficeSignOutPage.class);
        } else {
            signedInMessage = "Not logged in.";
            signInOutMessage = "log in";
            
            // TODO: make this a panel
            signInOutLink = new BookmarkablePageLink<Void>("signInOutLink", Home.class);
            signInOutLink.setEnabled(false);
            signInOutLink.add(AttributeModifier.replace("style", "display: none;"));
        }
        
        signInOutLink.add(new Label("signInOutMessage", signInOutMessage));
        add(signInOutLink);
        add(new Label("signedInMessage", signedInMessage));
        
        // create external links
        List<String> extKeys = new ArrayList<String>();
        if(externalMenuLinks != null) {
        	extKeys.addAll(externalMenuLinks.keySet());
        }
        Collections.sort(extKeys);
        add(new ListView<String>("externalLinkList", extKeys) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String linkText = item.getModelObject();
                String url = externalMenuLinks.get(linkText);
                item.add(new ExternalLink("link", url, linkText));
            }
        });
	}
	
	private Component createPageLink(
        String id, 
        Class<? extends Page> pageClass, 
        Class<? extends BasePage> currentPageClass) {
	    
	    BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(id, pageClass);
	    Component li = new WebMarkupContainer(id + "Item").add(link); 
	    if (pageClass.equals(currentPageClass)) {
	        link.add(new AttributeModifier("class", Model.of("currentMenuItem")));
	        li.add(new AttributeModifier("class", Model.of("current")));
	    }
	    
        return li;
	}
}
