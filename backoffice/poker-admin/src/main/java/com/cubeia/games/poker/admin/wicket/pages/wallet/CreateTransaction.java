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

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;

@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class CreateTransaction extends BasePage {
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(CreateTransaction.class);

    @SpringBean(name="walletClient")
    private WalletServiceClient walletService;
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public CreateTransaction(final PageParameters parameters) {
        super(parameters);
//        resetFormData();
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);
        
        
        @SuppressWarnings("unused")
        Form<Void> txForm = new Form<Void>("txForm") {
            private static final long serialVersionUID = 1L;
            
            private Long fromAccountId;
            private Long toAccountId;
            private BigDecimal amount;
            private String comment;
            
            @Override
            protected void onSubmit() {
                Account fromAccount = walletService.getAccountById(fromAccountId);
                Account toAccount = walletService.getAccountById(toAccountId);
                
                if (!fromAccount.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                    feedback.error("Account currencies doesn't match.");
                    return;
                }
                
                String currencyCode = fromAccount.getCurrencyCode();
                Currency currency = walletService.getCurrency(currencyCode);
                
                
                TransactionRequest tx = new TransactionRequest();
                tx.setComment(getComment());
                
                TransactionEntry fromEntry = new TransactionEntry(fromAccountId, new Money(currencyCode, currency.getFractionalDigits(), getAmount().negate()));
                TransactionEntry toEntry = new TransactionEntry(toAccountId, new Money(currencyCode, currency.getFractionalDigits(), getAmount()));
                tx.setEntries(Arrays.asList(fromEntry, toEntry));
                
                log.debug("transaction request: {}", tx);
                
                TransactionResult txResponse = walletService.doTransaction(tx);
                log.debug("created transaction: {}", txResponse);

                feedback.info("created transaction: " + txResponse.getTransactionId());
            }
            
            public Long getFromAccountId() {
                return fromAccountId;
            }
            
            public void setFromAccountId(Long fromAccountId) {
                this.fromAccountId = fromAccountId;
            }
            
            public Long getToAccountId() {
                return toAccountId;
            }
            
            public void setToAccountId(Long toAccountId) {
                this.toAccountId = toAccountId;
            }
            
            public BigDecimal getAmount() {
                return amount;
            }
            
            public void setAmount(BigDecimal amount) {
                this.amount = amount;
            }
            
            public String getComment() {
                return comment;
            }
            
            public void setComment(String comment) {
                this.comment = comment;
            }
        };
        
        CompoundPropertyModel<Form<Void>> txFormModel = new CompoundPropertyModel<Form<Void>>(txForm);
        
        Label fromAccountInfoLabel = new Label("fromAccountInfo", new Model<String>());
        fromAccountInfoLabel.setOutputMarkupId(true);
        txForm.add(fromAccountInfoLabel);
        
        Label toAccountInfoLabel = new Label("toAccountInfo", new Model<String>());
        toAccountInfoLabel.setOutputMarkupId(true);
        txForm.add(toAccountInfoLabel);
        
        RequiredTextField<Long> fromAccountIdField = new RequiredTextField<Long>("fromAccountIdField", txFormModel.<Long>bind("fromAccountId"));
        fromAccountIdField.add(new AccountInfoLoader(fromAccountInfoLabel, fromAccountIdField));
        txForm.add(fromAccountIdField);
        
        RequiredTextField<Long> toAccountIdField = new RequiredTextField<Long>("toAccountIdField", txFormModel.<Long>bind("toAccountId"));
        toAccountIdField.add(new AccountInfoLoader(toAccountInfoLabel, toAccountIdField));
        txForm.add(toAccountIdField);
        
        txForm.add(new RequiredTextField<BigDecimal>("amountField", txFormModel.<BigDecimal>bind("amount")));
        
        txForm.add(new TextField<String>("commentField", txFormModel.<String>bind("comment")));
        
        add(txForm);        
    }
    
    
    @Override
    public String getPageTitle() {
        return "Create transaction";
    }
    
    
    @SuppressWarnings("serial")
    private final class AccountInfoLoader extends OnChangeAjaxBehavior {
        private final Label accountInfoLabel;
        private final RequiredTextField<Long> accountIdField;

        private AccountInfoLoader(Label infoLabel, RequiredTextField<Long> accountIdField) {
            this.accountInfoLabel = infoLabel;
            this.accountIdField = accountIdField;
            //setThrottleDelay(Duration.seconds(0.5));
            getAttributes().setThrottlingSettings(new ThrottlingSettings("test",Duration.seconds(0.5),true));
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            Long id = accountIdField.getModelObject();
            String info = "not found";
            
            if (id != null) {
                try {
                    Account account = walletService.getAccountById(id);
                    info = "id: " + id + ", currency: " + account.getCurrencyCode() + ", user: " + account.getUserId() + ", type: " + account.getType().name() 
                        + ", status: " + account.getStatus().name() + ", name: " + account.getInformation().getName();
                } catch (Exception e) {
                    log.debug("error fetching account {}: {}", id, e.getMessage());
                }
            } 
            
            accountInfoLabel.setDefaultModelObject(info);
            target.add(accountInfoLabel);
        }
    }

    
}
