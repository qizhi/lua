/**
 * Copyright (C) 2012 BetConstruct
 */

package com.cubeia.games.poker.admin.wicket.pages.rakes;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.poker.settings.RakeSettings;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;

public class CreateRake extends BasePage {

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;

    private RakeSettings rake;

    public CreateRake(final PageParameters parameters) {
        super(parameters);
        rake = new RakeSettings();
        Form<RakeSettings> rakeForm = new Form<RakeSettings>("rakeForm", new CompoundPropertyModel<RakeSettings>(rake)) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                RakeSettings object = getModel().getObject();
                adminDAO.persist(object);
                // info("Rake configuration created: " + object);
                setResponsePage(ListRakes.class);
            }
        };

        rakeForm.add(new RequiredTextField<String>("name"));
        rakeForm.add(new RequiredTextField<BigDecimal>("rakeFraction2Plus"));
        rakeForm.add(new RequiredTextField<Long>("rakeLimit2Plus"));
        rakeForm.add(new RequiredTextField<BigDecimal>("rakeFraction3Plus"));
        rakeForm.add(new RequiredTextField<Long>("rakeLimit3Plus"));
        rakeForm.add(new RequiredTextField<BigDecimal>("rakeFraction5Plus"));
        rakeForm.add(new RequiredTextField<Long>("rakeLimit5Plus"));

        add(rakeForm);

        add(new FeedbackPanel("feedback"));

    }

    @Override
    public String getPageTitle() {
        return "Create Rake Configuration";
    }
}
