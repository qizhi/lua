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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.payouts;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.tournament.configuration.payouts.IntRange;
import com.cubeia.games.poker.tournament.configuration.payouts.Payout;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newArrayList;

public class ViewPayoutStructure extends BasePage {

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;

    public ViewPayoutStructure(PageParameters p) {
        super(p);
        PayoutStructure structure = adminDAO.getItem(PayoutStructure.class, p.get("structureId").toInt());
        DataTable table = createTable(structure);
        add(table);
    }

    private DataTable createTable(PayoutStructure structure) {
        SortedSet<IntRange> ranges = getRanges(structure);
        List<IColumn> columns = createColumns(ranges);
        DataTable table = new DataTable("payoutStructure", columns, new PayoutStructureRowProvider(structure.getPayoutsPerEntryRange(), columns.size()), 100);
        table.addTopToolbar(new HeadersToolbar(table, null));
        return table;
    }

    private List<IColumn> createColumns(Set<IntRange> ranges) {
        List<IColumn> columns = newArrayList();
        columns.add(new PropertyColumn(new Model<String>("Players"), "0"));
        int index = 1;
        for (IntRange range : ranges) {
            columns.add(new PropertyColumn(new Model<String>(rangeToString(range)), "" + index++));
        }
        return columns;
    }

    private String rangeToString(IntRange range) {
        if (range.getStart() == range.getStop()) {
            return String.valueOf(range.getStart());
        } else {
            return range.getStart() + "-" + range.getStop();
        }
    }

    private SortedSet<IntRange> getRanges(PayoutStructure structure) {
        SortedSet<IntRange> ranges = new TreeSet<IntRange>();
        for (Payouts payouts : structure.getPayoutsPerEntryRange()) {
            for (Payout payout :payouts.getPayoutList()) {
                ranges.add(payout.getPositionRange());
            }
        }
        return ranges;
    }

    @Override
    public String getPageTitle() {
        return "Viewing Payout Structure";
    }

    private class PayoutStructureRowProvider implements IDataProvider<List<? extends String>> {

        private List<List<String>> rows = newArrayList();

        public PayoutStructureRowProvider(List<Payouts> payoutList, int rangeCount) {
            for (Payouts payouts : payoutList) {
                rows.add(rowFor(payouts, rangeCount));
            }
        }

        private List<String> rowFor(Payouts payouts, int rangeCount) {
            List<String> row = newArrayList();
            row.add(rangeToString(payouts.getEntrantsRange()));
            for (Payout payout : payouts.getPayoutList()) {
                row.add(payout.getPercentage().toString() + "%");
            }
            // Fill the rest of the row with empty strings.
            for (int i = row.size(); i < rangeCount; i++) {
                row.add("");
            }
            return row;
        }

        @Override
        public Iterator<? extends List<String>> iterator(long first, long count) {
            // We're not going to paginate, so we're ignoring first and count.
            return rows.iterator();
        }

        @Override
        public long size() {
            return rows.size();
        }

        @Override
        public IModel<List<? extends String>> model(List<? extends String> object) {
            return Model.<String>ofList(object);
        }

        @Override
        public void detach() {
            // Ignoring for now.
        }
    }
}
