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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.sitandgo;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.tournaments.configuration.TournamentConfigurationPanel;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.TournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.poker.timing.TimingProfile;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;

public class EditSitAndGo extends BasePage {

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;
    
    private SitAndGoConfiguration tournament;

    public EditSitAndGo(final PageParameters parameters) {
        super(parameters);
        final Integer tournamentId = parameters.get("tournamentId").toInt();
        
        loadFormData(tournamentId);
        
        Form<SitAndGoConfiguration> tournamentForm = new Form<SitAndGoConfiguration>("tournamentForm",
                                                                new CompoundPropertyModel<SitAndGoConfiguration>(tournament)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit() {
                SitAndGoConfiguration object = getModel().getObject();
                adminDAO.save(object);
                info("Tournament updated, id = " + tournamentId);
                setResponsePage(ListSitAndGoTournaments.class);
            }
        };

        tournamentForm.add(new TournamentConfigurationPanel("configuration", tournamentForm,new PropertyModel<TournamentConfiguration>(tournament, "configuration"), false));

        add(tournamentForm);

        add(new FeedbackPanel("feedback"));
    }

    private void loadFormData(final Integer tournamentId) {
        tournament = adminDAO.getItem(SitAndGoConfiguration.class, tournamentId);
    }
    
    @Override
    public String getPageTitle() {
        return "Edit Sit & Go Tournament";
    }

}
