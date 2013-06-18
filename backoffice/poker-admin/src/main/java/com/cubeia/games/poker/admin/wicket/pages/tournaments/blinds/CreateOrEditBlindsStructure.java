/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.blinds;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.blinds.Level;
import com.cubeia.network.shared.web.wicket.list.EditableListItem;
import com.cubeia.network.shared.web.wicket.list.ListEditor;
import com.cubeia.network.shared.web.wicket.list.RemoveButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

import java.math.BigDecimal;
import java.util.Iterator;

import static com.cubeia.games.poker.admin.wicket.util.WicketHelpers.isEmpty;

public class CreateOrEditBlindsStructure extends BasePage {

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;

    private final BlindsStructure structure;

    public CreateOrEditBlindsStructure(final PageParameters parameters) {
        super(parameters);
        structure = initBlindsStructure(parameters);
        initPage();
    }

    private void initPage() {
        final Form<BlindsStructure> blindsStructureForm = new Form<BlindsStructure>("blindsStructureForm",
                new CompoundPropertyModel<BlindsStructure>(structure)) {

            @Override
            protected void onSubmit() {
                removeInvalidLevels();
                if (structure.getId() != 0) {
                    adminDAO.save(structure);
                } else {
                    adminDAO.persist(structure);
                }
                setResponsePage(CreateOrEditBlindsStructure.class, new PageParameters().add("structureId", structure.getId()));
            }
        };

        final ListEditor levelListView = new ListEditor<Level>("blindsLevels", new PropertyModel(this, "structure.blindsLevels")) {
            @Override
            protected void onPopulateItem(EditableListItem<Level> item) {
                item.setModel(new CompoundPropertyModel(item.getModel()));
                item.add(new Label("level", new Model<Integer>(item.getIndex() + 1)));
                item.add(new TextField<Integer>("smallBlindAmount"));
                item.add(new TextField<Integer>("bigBlindAmount"));
                item.add(new TextField<Integer>("durationInMinutes"));
                item.add(new CheckBox("break"));

                item.add(new RemoveButton("remove"));
            }
        };

        blindsStructureForm.add(new Button("add") {
            @Override
            public void onSubmit() {
                levelListView.addItem(new Level());
            }
        }.setDefaultFormProcessing(false));


        blindsStructureForm.add(new RequiredTextField<String>("name"));
        blindsStructureForm.add(levelListView);
        add(blindsStructureForm);
        add(new FeedbackPanel("feedback"));
    }

    private void removeInvalidLevels() {
        Iterator<Level> iterator = structure.getBlindsLevels().iterator();
        while (iterator.hasNext()) {
            if (!isValid(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private boolean isValid(Level l) {
        return l.getBigBlindAmount().compareTo(BigDecimal.ZERO) != 0 && l.getSmallBlindAmount().compareTo(BigDecimal.ZERO) != 0 && l.getDurationInMinutes() != 0;
    }

    private BlindsStructure initBlindsStructure(PageParameters parameters) {
        StringValue structureId = parameters.get("structureId");
        if (!isEmpty(structureId)) {
            return adminDAO.getItem(BlindsStructure.class, structureId.toInt());
        }
        return new BlindsStructure();
    }

    @Override
    public String getPageTitle() {
        return "Create Blinds Structure";
    }
}
