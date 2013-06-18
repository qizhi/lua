/**
 * Copyright (C) 2012 BetConstruct
 */

package com.cubeia.games.poker.admin.wicket.pages.timings;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.poker.timing.TimingProfile;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class EditTiming extends BasePage {

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;

    private TimingProfile timing;

    public EditTiming(final PageParameters parameters) {
        super(parameters);
        final Integer templateId = parameters.get("templateId").toInt();
        loadFormData(templateId);
        Form<TimingProfile> timingForm = new Form<TimingProfile>("timingForm", new CompoundPropertyModel<TimingProfile>(timing)) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                TimingProfile object = getModel().getObject();
                adminDAO.save(object);
                // info("Timing configuration updated, id = " + templateId);
                setResponsePage(ListTimings.class);
            }
        };

        timingForm.add(new RequiredTextField<String>("name", new PropertyModel<String>(this, "timing.name")));
        timingForm.add(new RequiredTextField<Long>("pocketCardsTime", new PropertyModel<Long>(this, "timing.pocketCardsTime")));
        timingForm.add(new RequiredTextField<Long>("flopTime", new PropertyModel<Long>(this, "timing.flopTime")));
        timingForm.add(new RequiredTextField<Long>("turnTime", new PropertyModel<Long>(this, "timing.turnTime")));
        timingForm.add(new RequiredTextField<Long>("riverTime", new PropertyModel<Long>(this, "timing.riverTime")));
        timingForm.add(new RequiredTextField<Long>("startNewHandTime", new PropertyModel<Long>(this, "timing.startNewHandTime")));
        timingForm.add(new RequiredTextField<Long>("actionTimeout", new PropertyModel<Long>(this, "timing.actionTimeout")));
        timingForm.add(new RequiredTextField<Long>("autoPostBlindDelay", new PropertyModel<Long>(this, "timing.autoPostBlindDelay")));
        timingForm.add(new RequiredTextField<Long>("latencyGracePeriod", new PropertyModel<Long>(this, "timing.latencyGracePeriod")));
        timingForm.add(new RequiredTextField<Long>("disconnectExtraTime", new PropertyModel<Long>(this, "timing.disconnectExtraTime")));

        add(timingForm);

        add(new FeedbackPanel("feedback"));

    }

    private void loadFormData(final Integer timingId) {
        timing = adminDAO.getItem(TimingProfile.class, timingId);
    }

    @Override
    public String getPageTitle() {
        return "Edit Timing Configuration";
    }
}
