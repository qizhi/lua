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

package com.cubeia.games.poker.admin.wicket.pages.history;

import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.util.DatePanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.ParamBuilder;
import com.cubeia.poker.handhistory.api.HistoricHand;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.IClusterable;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.cubeia.games.poker.admin.wicket.util.WicketHelpers.*;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Page for searching for and viewing hand histories.
 */
public class HandHistory extends BasePage {

    private static final Logger log = Logger.getLogger(HandHistory.class);

    private static final long serialVersionUID = 1L;

    @SpringBean
    private HistoryService historyService;

    private final HandHistory.HandProvider handProvider;

    public HandHistory(PageParameters parameters) {
        super(parameters);
        log.debug("Params: " + parameters);
        handProvider = new HandProvider(parameters);
        addForms();
        addResultsTable();
        add(new FeedbackPanel("feedback"));
    }

    private void addResultsTable() {
        List<IColumn<HistoricHand,String>> columns = createColumns();
        add(new AjaxFallbackDefaultDataTable<HistoricHand,String>("hands", columns, handProvider, 8));
    }

    private List<IColumn<HistoricHand,String>> createColumns() {
        List<IColumn<HistoricHand,String>> columns = new ArrayList<IColumn<HistoricHand,String>>();

        // Add column with clickable hand ids.
        columns.add(new AbstractColumn<HistoricHand,String>(new Model<String>("Hand id")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<HistoricHand>> item, String componentId, IModel<HistoricHand> model) {
                HistoricHand hand = model.getObject();
                String handId = hand.getId();
                Component panel = new LabelLinkPanel(componentId, "" + handId, ShowHand.class, ParamBuilder.params("handId", handId));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }

        });
        // columns.add(new PropertyColumn<HistoricHand>(Model.of("Hand id"), "handId.handId"));
        columns.add(new PropertyColumn<HistoricHand,String>(Model.of("Table id"), "table.tableId"));
        columns.add(new PropertyColumn<HistoricHand,String>(Model.of("Table integration id"), "table.tableIntegrationId"));
        columns.add(new PropertyColumn<HistoricHand,String>(Model.of("Table name"), "table.tableName"));
        columns.add(new AbstractColumn<HistoricHand,String>(new Model<String>("Start date")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<HistoricHand>> item, String componentId, IModel<HistoricHand> model) {
                HistoricHand hand = model.getObject();
                item.add(new DatePanel(componentId, hand.getStartTime()));
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });
        columns.add(new AbstractColumn<HistoricHand,String>(new Model<String>("End date")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<HistoricHand>> item, String componentId, IModel<HistoricHand> model) {
                HistoricHand hand = model.getObject();
                item.add(new DatePanel(componentId, hand.getEndTime()));
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });
        columns.add(new PropertyColumn<HistoricHand,String>(Model.of("Total rake"), "results.totalRake"));

        return columns;
    }

    private void addForms() {
        Form<HandHistorySearch> form = new Form<HandHistorySearch>("form",  new CompoundPropertyModel<HandHistorySearch>(new HandHistorySearch())) {
            @Override
            protected void onSubmit() {
            	HandHistorySearch hs = getModel().getObject();
                if(hs.isEmpty()) {
                	error("Please provide at least one field to search on.");
                } else {
                	handProvider.search(hs);
                }
            }
        };
        form.add(new TextField<Integer>("playerId").setRequired(false));
        form.add(new TextField<Integer>("tableId"));
        form.add(new DateField("fromDate"));
        form.add(new DateField("toDate"));
        add(form);
        Form<HandLookup> form2 = new Form<HandLookup>("lookup", new CompoundPropertyModel<HandLookup>(new HandLookup())) {

			private static final long serialVersionUID = 3724547810754260078L;
        	
			@Override
			protected void onSubmit() {
				HandLookup str = getModel().getObject();
				setResponsePage(ShowHand.class, ParamBuilder.params("handId", str.handId));
			}
        };
        form2.add(new TextField<String>("handId").setRequired(true));
        add(form2);
    }

    @Override
    public String getPageTitle() {
        return "Hand History";
    }
    
    private class HandLookup implements IClusterable {
    	private static final long serialVersionUID = -7138295119718739577L;
		String handId;
    }

    private class HandProvider extends SortableDataProvider<HistoricHand,String> {

        private List<HistoricHand> hands;

        private HandProvider(PageParameters parameters) {
            if (parameters.isEmpty()) {
                hands = newArrayList();
            } else {
                HandHistorySearch search = new HandHistorySearch();
                search.playerId = toIntOrNull(parameters.get("playerId"));
                search.tableId = toStringOrNull(parameters.get("tableId"));
                search.fromDate = toDateOrNull(parameters.get("startDate"));
                search.toDate = toDateOrNull(parameters.get("toDate"));
                if(search.isEmpty()) {
                	search.resetDates();
                }
                search(search);
            }
        }

        @Override
        public Iterator<? extends HistoricHand> iterator(long first, long count) {
            return hands.subList((int)first, (int)(first + count)).iterator();
        }

        @Override
        public long size() {
            return hands.size();
        }

        @Override
        public IModel<HistoricHand> model(HistoricHand historicHand) {
            return Model.of(historicHand);
        }

        public void search(HandHistorySearch params) {
            hands = historyService.findHandHistory(params.playerId, params.tableId, params.fromDate, params.toDate);
        }
    }

    private static class HandHistorySearch implements IClusterable {
        private static final long serialVersionUID = 7373912948187005605L;
		Integer playerId;
        Date fromDate;
        Date toDate;
        String tableId;
        public HandHistorySearch() {
        	resetDates();
        }
		public boolean isEmpty() {
			return playerId == null && fromDate == null && toDate == null && tableId == null;
		}
		private void resetDates() {
			fromDate = new DateTime().minusDays(7).toDate();
	        toDate = new DateTime().plusDays(1).toDate();
		}
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
    }
}
