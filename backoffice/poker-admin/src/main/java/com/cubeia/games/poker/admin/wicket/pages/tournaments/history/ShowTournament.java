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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.history;

import com.cubeia.games.poker.admin.Configuration;
import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.pages.history.HandHistory;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.ParamBuilder;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.cubeia.poker.tournament.history.api.PlayerPosition;
import com.cubeia.poker.tournament.history.api.TournamentEvent;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.cubeia.games.poker.admin.wicket.util.ExternalLinkPanel.externalLinkForPlayer;

public class ShowTournament extends BasePage {

    private static final Logger log = Logger.getLogger(ShowTournament.class);

    @SpringBean
    private HistoryService historyService;

    @SpringBean(name = "webConfig")
    private Configuration config;

    public ShowTournament(PageParameters parameters) {
        super(parameters);
        String historicTournamentId = parameters.get("historicTournamentId").toString();
        log.debug("Tourn id " + historicTournamentId);
        HistoricTournament tournament = historyService.findTournamentByHistoricId(historicTournamentId);
        addSummary(tournament);
        addResultList(tournament);
        addEvents(tournament);
        addTables(tournament);
    }

    private void addResultList(final HistoricTournament tournament) {
        DataView<PlayerPosition> players = new DataView<PlayerPosition>("players", new ListDataProvider<PlayerPosition>(tournament.getPositions())) {

            private static final long serialVersionUID = 1908334758912501993L;

            @Override
            protected void populateItem(Item<PlayerPosition> item) {
                PlayerPosition position = item.getModelObject();
                item.add(externalLinkForPlayer(position.getPlayerId(), config.getNetworkUrl()));
                item.add(new Label("position", String.valueOf(position.getPosition())));
                item.add(new Label("payout",position.getPayout().toPlainString()));
            }
        };

        add(players);
    }

    private void addSummary(HistoricTournament tournament) {
        log.debug("Tournament: " + tournament);
        add(new Label("historicTournamentId", tournament.getId()));
        add(new Label("tournamentId", String.valueOf(tournament.getTournamentId())));
        add(new Label("tournamentTemplateId", String.valueOf(tournament.getTournamentTemplateId())));
        add(new Label("name", tournament.getTournamentName()));
        add(new Label("startTime", new Date(tournament.getStartTime()).toString()));
        add(new Label("endTime", new Date(tournament.getEndTime()).toString()));
    }

    private void addEvents(final HistoricTournament tournament) {
        DataView<TournamentEvent> events = new DataView<TournamentEvent>("events", new ListDataProvider<TournamentEvent>(tournament.getEvents())) {
            @Override
            protected void populateItem(Item<TournamentEvent> item) {
                TournamentEvent event = item.getModelObject();

                item.add(new Label("event", event.getEvent()));
                item.add(new Label("details", event.getDetails()));
                item.add(new Label("time", new Date(event.getTimestamp()).toString()));
            }
        };
        add(events);
    }

    private void addTables(final HistoricTournament tournament) {
        List<IColumn<String,String>> columns = new ArrayList<IColumn<String,String>>();
        columns.add(new AbstractColumn<String,String>(new Model<String>("tableId")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<String>> item, String componentId, IModel<String> model) {
                String tableId = model.getObject();
                Component panel = new LabelLinkPanel(componentId, tableId, HandHistory.class, ParamBuilder.params("tableId", tableId));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }

        });
        add(new AjaxFallbackDefaultDataTable<String,String>("tables", columns, new TableProvider(tournament.getTables()), 8));
    }

    @Override
    public String getPageTitle() {
        return "Tournament history";
    }

    private static class TableProvider extends SortableDataProvider<String,String> {

        private List<String> items;

        private TableProvider(List<String> items) {
            this.items = items;
        }

        @Override
        public Iterator<? extends String> iterator(long first, long count) {
            return items.iterator();
        }

        @Override
        public long size() {
            return items.size();
        }

        @Override
        public IModel<String> model(String string) {
            return Model.of(string);
        }
    }
}
