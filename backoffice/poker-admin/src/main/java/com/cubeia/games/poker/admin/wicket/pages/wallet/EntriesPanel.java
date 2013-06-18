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
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;

public class EntriesPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public EntriesPanel(
        final Collection<Long> highlightAccountIds, 
        final Collection<Long> highlightUserIds, 
        String id, Collection<Entry> entries) {
        super(id);

        ArrayList<Entry> entryList = new ArrayList<Entry>();
        if (entries != null) {
            entryList.addAll(entries);
        }
        
        Collections.sort(entryList, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return o1.getAccountId().compareTo(o2.getAccountId());
            }
        });
        
        ListView<Entry> entryListView = new ListView<Entry>("entries", entryList) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Entry> item) {
                Entry e = item.getModelObject();
                
                String label = "" + e.getAccountId() + " (" + e.getAccountUserId() + ")"; 
                
                String toolTip = "Details for account id = " + e.getAccountId() + 
                    ". User id = " + e.getAccountUserId() + 
                    ", entry id = " + e.getId() + ".";
                    
                LabelLinkPanel labelLink = new LabelLinkPanel(
                     "accountEntriesLink", label, toolTip, AccountDetails.class, 
                     params(AccountDetails.PARAM_ACCOUNT_ID, e.getAccountId()));
                item.add(labelLink);
                
                Label amountLabel = new Label("amount", "" + e.getAmount());
                item.add(amountLabel);
                
                boolean highlight = highlightAccountIds.contains(e.getAccountId())
                    ||  highlightUserIds.contains(e.getAccountUserId());
                
                if (highlight) {
                    AttributeModifier highlightModifier = new AttributeModifier("class", Model.of("highlight"));
                    labelLink.add(highlightModifier);
                    amountLabel.add(highlightModifier);
                }
            }
        };
        
        add(entryListView);
    }
}
