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
import com.cubeia.games.poker.admin.wicket.util.DeleteLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.ParamBuilder;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListBlindsStructures extends BasePage {

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;

    @Override
    public String getPageTitle() {
        return "Blinds Structures";
    }

    public ListBlindsStructures(final PageParameters parameters) {
        super(parameters);
        SortableDataProviderExtension dataProvider = new SortableDataProviderExtension();
        ArrayList<AbstractColumn> columns = new ArrayList<AbstractColumn>();

        columns.add(new AbstractColumn<BlindsStructure,String>(new Model<String>("Id")) {
            @Override
            public void populateItem(Item<ICellPopulator<BlindsStructure>> item, String componentId, IModel<BlindsStructure> model) {
                BlindsStructure blindsStructure = model.getObject();
                Component panel = new LabelLinkPanel(
                    componentId,
                    "" + blindsStructure.getId(),
                    CreateOrEditBlindsStructure.class,
                    ParamBuilder.params("structureId", blindsStructure.getId()));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        columns.add(new PropertyColumn(new Model<String>("Name"), "name"));
        columns.add(new AbstractColumn<BlindsStructure,String>(new Model<String>("Delete")) {

            @Override
            public void populateItem(Item<ICellPopulator<BlindsStructure>> item, String componentId, IModel<BlindsStructure> model) {
                BlindsStructure table = model.getObject();
                Component panel = new DeleteLinkPanel(componentId, BlindsStructure.class, table.getId(), ListBlindsStructures.class);
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        DefaultDataTable<BlindsStructure,String> table = new DefaultDataTable("blindsStructureTable", columns, dataProvider, 20);
        add(table);
    }

    private List<BlindsStructure> getTournamentList() {
        return adminDAO.getBlindsStructures();
    }

    private final class SortableDataProviderExtension extends SortableDataProvider<BlindsStructure,String> {
        private static final long serialVersionUID = 1L;

        public SortableDataProviderExtension() {
            setSort("id", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<BlindsStructure> iterator(long first, long count) {
            return getTournamentList().subList((int)first, (int)(count + first)).iterator();
        }

        @Override
        public IModel<BlindsStructure> model(BlindsStructure object) {
            return new Model(object);
        }

        @Override
        public long size() {
            return getTournamentList().size();
        }
    }
}
