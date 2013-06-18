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
import com.cubeia.games.poker.admin.wicket.pages.tournaments.configuration.TournamentArchiver;
import com.cubeia.games.poker.admin.wicket.util.ArchiveLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.DeleteLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.ParamBuilder;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
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

/**
 * Page for listing all tournaments. Currently lists sit&go tournaments.
 */
public class ListSitAndGoTournaments extends BasePage {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;


    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters Page parameters
     */
    public ListSitAndGoTournaments(final PageParameters parameters) {
        super(parameters);
        SortableDataProviderExtension dataProvider = new SortableDataProviderExtension();
        ArrayList<AbstractColumn> columns = new ArrayList<AbstractColumn>();

        columns.add(new AbstractColumn<SitAndGoConfiguration,String>(new Model<String>("Id")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<SitAndGoConfiguration>> item, String componentId, IModel<SitAndGoConfiguration> model) {
                SitAndGoConfiguration tournament = model.getObject();
                Component panel = new LabelLinkPanel(
                    componentId,
                    "" + tournament.getId(),
                    EditSitAndGo.class,
                    ParamBuilder.params("tournamentId", tournament.getId()));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        columns.add(new PropertyColumn(new Model("Name"), "configuration.name"));
        columns.add(new PropertyColumn(new Model("Seats"), "configuration.seatsPerTable"));
        columns.add(new PropertyColumn(new Model("Min"), "configuration.minPlayers"));
        columns.add(new PropertyColumn(new Model("Max"), "configuration.maxPlayers"));
        columns.add(new PropertyColumn(new Model<String>("Buy-in"), "configuration.buyIn"));
        columns.add(new PropertyColumn(new Model<String>("Fee"), "configuration.fee"));
        
        columns.add(new AbstractColumn<SitAndGoConfiguration,String>(new Model<String>("Delete")) {

            @Override
            public void populateItem(Item<ICellPopulator<SitAndGoConfiguration>> item, String componentId, IModel<SitAndGoConfiguration> model) {
                SitAndGoConfiguration sitAndGo = model.getObject();
                Component panel = new ArchiveLinkPanel(componentId, new TournamentArchiver(sitAndGo.getConfiguration()), sitAndGo, ListSitAndGoTournaments.class);
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        DefaultDataTable userTable = new DefaultDataTable("tournamentTable", columns, dataProvider, 20);
        add(userTable);
    }

    private List<SitAndGoConfiguration> getTournamentList() {
        return adminDAO.getSitAndGoConfigurations();
    }

    private final class SortableDataProviderExtension extends SortableDataProvider<SitAndGoConfiguration,String> {
        private static final long serialVersionUID = 1L;

        public SortableDataProviderExtension() {
            setSort("id", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<SitAndGoConfiguration> iterator(long first, long count) {
            return getTournamentList().subList((int)first, (int)(count + first)).iterator();
        }

        @Override
        public IModel<SitAndGoConfiguration> model(SitAndGoConfiguration object) {
            return new Model(object);
        }

        @Override
        public long size() {
            return getTournamentList().size();
        }
    }

    @Override
    public String getPageTitle() {
        return "Sit & Go Tournaments";
    }
}
