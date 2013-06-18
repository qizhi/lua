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

package com.cubeia.games.poker.admin.wicket.pages.tables;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.util.DeleteLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;

/**
 * Page for listing all tournaments. Currently lists sit&go tournaments.
 */
public class ListTables extends BasePage {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;


    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters Page parameters
     */
    public ListTables(final PageParameters parameters) {
        super(parameters);
        SortableDataProviderExtension dataProvider = new SortableDataProviderExtension();
        ArrayList<AbstractColumn<TableConfigTemplate,String>> columns = new ArrayList<AbstractColumn<TableConfigTemplate,String>>();
        columns.add(new AbstractColumn<TableConfigTemplate,String>(new Model<String>("Id")) {

            @Override
            public void populateItem(Item<ICellPopulator<TableConfigTemplate>> item, String componentId, IModel<TableConfigTemplate> model) {
                TableConfigTemplate table = model.getObject();
                Component panel = new LabelLinkPanel(
                        componentId,
                        "" + table.getId(),
                        EditTable.class,
                        params("templateId", table.getId()));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Name"), "name"));
        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Seats"), "seats"));
        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Ante"), "ante"));
        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Variant"), "variant"));
        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Timing"), "timing.name"));
        columns.add(new PropertyColumn<TableConfigTemplate,String>(new Model<String>("Rake system"), "rakeSettings.name"));

        columns.add(new AbstractColumn<TableConfigTemplate,String>(new Model<String>("Delete")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void populateItem(Item<ICellPopulator<TableConfigTemplate>> item, String componentId, IModel<TableConfigTemplate> model) {
                TableConfigTemplate table = model.getObject();
                Component panel = new DeleteLinkPanel(componentId, TableConfigTemplate.class, table.getId(), ListTables.class);
                item.add(panel);
            }
            @Override
            public boolean isSortable() {
                return false;
            }
        });

        DefaultDataTable table = new DefaultDataTable("tableTable", columns, dataProvider, 20);
        add(table);

        add(new FeedbackPanel("feedback"));
    }

    private List<TableConfigTemplate> getTableTemplateList() {
        return adminDAO.getTableConfigTemplates();
    }

    @Override
    public String getPageTitle() {
        return "Table Templates";
    }
    
    
    //  --- PRIVATE CLASSES --- //
    
    private final class SortableDataProviderExtension extends SortableDataProvider<TableConfigTemplate,String> {

        private static final long serialVersionUID = 1L;

        public SortableDataProviderExtension() {
            setSort("id", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<TableConfigTemplate> iterator(long first, long count) {
            return getTableTemplateList().subList((int)first, (int)(count + first)).iterator();
        }

        @Override
        public IModel<TableConfigTemplate> model(TableConfigTemplate object) {
            return new Model<TableConfigTemplate>(object);
        }

        @Override
        public long size() {
            return getTableTemplateList().size();
        }
    }
}
