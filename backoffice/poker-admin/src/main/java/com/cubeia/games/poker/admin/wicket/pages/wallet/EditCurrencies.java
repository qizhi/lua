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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.util.ConfirmOnclickAttributeModifier;

@AuthorizeInstantiation({"SUPER_USER", "WALLET_ADMIN"})
public class EditCurrencies extends BasePage {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(EditCurrencies.class);

    @SpringBean(name="walletClient")
    private WalletServiceClient walletService;

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 *
	 */
    @SuppressWarnings("serial")
    public EditCurrencies(PageParameters parameters) {
        super(parameters);
        add(new FeedbackPanel("feedback", new ComponentFeedbackMessageFilter(EditCurrencies.this)));
        
        IModel<List<Currency>> currencyModel = new LoadableDetachableModel<List<Currency>>() {
            @Override
            protected List<Currency> load() {
                CurrencyListResult supportedCurrencies = walletService.getSupportedCurrencies();
                
                if (supportedCurrencies == null) {
                    return Collections.<Currency>emptyList();
                }
                
                ArrayList<Currency> curs = new ArrayList<Currency>(supportedCurrencies.getCurrencies());
                log.debug("got currencies: {}", curs);
                Collections.sort(curs, new Comparator<Currency>() {
                    @Override
                    public int compare(Currency o1, Currency o2) { return o1.getCode().compareTo(o2.getCode()); }
                });
                return curs;
            }
        };
        
        add(new ListView<Currency>("currencies", currencyModel) {
            @Override
            protected void populateItem(ListItem<Currency> item) {
                final Currency c = item.getModelObject();
                item.add(new Label("code", Model.of(c.getCode())));
                item.add(new Label("digits", Model.of(c.getFractionalDigits())));
                
                item.add(new Link<String>("removeLink") {
                    @Override
                    public void onClick() {
                        log.debug("removing currency: {}", c);
                        walletService.removeCurrency(c.getCode());
                    }
                }.add(new ConfirmOnclickAttributeModifier("Really remove this currency?")));
            }
        });
        
        final CompoundPropertyModel<Currency> newCurrencyModel = new CompoundPropertyModel<Currency>(new Currency(null, 2));
        
        Form<Currency> addForm = new Form<Currency>("addForm", newCurrencyModel) {
            @Override
            protected void onSubmit() {
                Currency cur = getModelObject();
                log.debug("submit: {}", cur);
                
                try {
                    walletService.addCurrency(cur);
                } catch (Exception e) {
                    error("Error creating currency: " + e.getMessage());
                    return;
                }
                
                info("Added currency " + cur.getCode() + " with " + cur.getFractionalDigits() + " fractional digits");
                newCurrencyModel.setObject(new Currency(null, 2));
            }  
        };
        
        addForm.add(new RequiredTextField<String>("code", newCurrencyModel.<String>bind("code"))
            .add(StringValidator.exactLength(3)));
        addForm.add(new RequiredTextField<Integer>("digits", newCurrencyModel.<Integer>bind("fractionalDigits"))
            .add(new RangeValidator<Integer>(0, 8)));
        addForm.add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(addForm)));
        addForm.add(new WebMarkupContainer("submitButton").add(new ConfirmOnclickAttributeModifier("Are you sure you want to add this currency?")));
        add(addForm);
    }

    @Override
    public String getPageTitle() {
        return "Edit supported currencies";
    }
}
