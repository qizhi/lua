package com.cubeia.games.poker.admin.wicket.pages.tables;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.Collections;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.network.NetworkClient;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.TimingProfile;


public abstract class TableForm extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private AdminDAO adminDAO;

    @SpringBean
    private NetworkClient networkClient;

    private static final Logger logger = LoggerFactory.getLogger(TableForm.class);

    public TableForm(String id, TableConfigTemplate tableTemplate) {
        super(id);
        Form<TableConfigTemplate> tableForm = new Form<TableConfigTemplate>("tableForm",
                new CompoundPropertyModel<TableConfigTemplate>(tableTemplate)){
            @Override
            protected void onSubmit() {
                TableConfigTemplate config = getModelObject();
                TableForm.this.onSubmit(config);
            }
        };
        final RequiredTextField<BigDecimal> anteField = new RequiredTextField<BigDecimal>("ante");
        final RequiredTextField<BigDecimal> smallBlindField = new RequiredTextField<BigDecimal>("smallBlind");
        final RequiredTextField<BigDecimal> bigBlindField = new RequiredTextField<BigDecimal>("bigBlind");
        final DropDownChoice<BetStrategyType> betStrategy = new DropDownChoice<BetStrategyType>("betStrategy", asList(BetStrategyType.values()), choiceRenderer());
        final FormComponent<BigDecimal> minBuyIn = new RequiredTextField<BigDecimal>("minBuyIn").setRequired(true);
        final FormComponent<BigDecimal> maxBuyIn = new RequiredTextField<BigDecimal>("maxBuyIn").setRequired(true);

        tableForm.add(new RequiredTextField<String>("name"));
        tableForm.add(anteField);
        tableForm.add(smallBlindField);
        tableForm.add(bigBlindField);
        tableForm.add(minBuyIn);
        tableForm.add(maxBuyIn);
        tableForm.add(new RequiredTextField<Integer>("seats"));
        tableForm.add(new TextField<Integer>("minTables"));
        tableForm.add(new TextField<Integer>("minEmptyTables"));
        tableForm.add(new DropDownChoice<String>("currency", networkClient.getCurrencies(), new ChoiceRenderer<String>()).setRequired(true));
        tableForm.add(betStrategy);
        tableForm.add(new DropDownChoice<TimingProfile>("timing", adminDAO.getTimingProfiles(), choiceRenderer()));
        tableForm.add(new DropDownChoice<RakeSettings>("rakeSettings", adminDAO.getRakeSettings(), choiceRenderer()));
        tableForm.add(new Button("submitButton", new Model<String>(getActionLabel())));
        tableForm.add(new AbstractFormValidator() {

            @Override
            public FormComponent<?>[] getDependentFormComponents() {
                return new FormComponent[] { }; //anteField, smallBlindField, bigBlindField, minBuyIn, maxBuyIn};
            }

            @Override
            public void validate(Form<?> form) {
                validateAnteOrBlinds(form);
                validateBuyIns(form);
            }

            private void validateAnteOrBlinds(Form<?> form) {
            	BigDecimal ante = anteField.getConvertedInput();
            	BigDecimal smallBlind = smallBlindField.getConvertedInput();
            	BigDecimal bigBlind = bigBlindField.getConvertedInput();
            	
            	if (!form.hasError()) {
	            	logger.debug("smallBlind(String) = " + anteField.getInput() + " bigBlind(String) = " + bigBlindField.getInput());
	                logger.debug("smallBlind = " + smallBlind + " bigBlind = " + bigBlind);
	
	                if (ante.compareTo(BigDecimal.ZERO) == 0) {
	                    if (smallBlind.compareTo(BigDecimal.ZERO) == 0 || bigBlind.compareTo(BigDecimal.ZERO) == 0) {
	                        form.error("Blinds must be defined if ante is 0.", Collections.<String, Object>emptyMap());
	                    }
	                }
	                if (bigBlind.compareTo(smallBlind) < 0) {
	                    form.error("Big blind must not be less than small blind.", Collections.<String, Object>emptyMap());
	                }
            	}
            }


            private void validateBuyIns(Form<?> form) {
            	BigDecimal minBuyInValue = minBuyIn.getConvertedInput();
            	BigDecimal maxBuyInValue = maxBuyIn.getConvertedInput();
                logger.debug("min: " + minBuyInValue + " max : " + maxBuyInValue);
                
                if (!form.hasError()) {
	                if (maxBuyInValue.compareTo(minBuyInValue) < 0) {
	                    form.error("Max Buy-in must not be less than Min Buy-in.", Collections.<String, Object>emptyMap());
	                }
                }
            }
        });
        add(tableForm);
    }

    protected <T>ChoiceRenderer<T> choiceRenderer() {
        return new ChoiceRenderer<T>("name");
    }

    public String getActionLabel() {
        return "Create";
    }
    
    protected abstract void onSubmit(TableConfigTemplate config);

}
