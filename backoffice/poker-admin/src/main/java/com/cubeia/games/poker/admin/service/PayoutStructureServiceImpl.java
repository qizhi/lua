package com.cubeia.games.poker.admin.service;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.util.PayoutStructureRowProvider;
import com.cubeia.games.poker.tournament.configuration.payouts.IntRange;
import com.cubeia.games.poker.tournament.configuration.payouts.Payout;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class PayoutStructureServiceImpl implements PayoutStructureViewHelper {

    @Inject
    private AdminDAO adminDAO;

    @Transactional
    @Override
    public DataTable getPayoutStructure(int id) {
           return createTable(adminDAO.getItem(PayoutStructure.class,id));
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

    private SortedSet<IntRange> getRanges(PayoutStructure structure) {
        SortedSet<IntRange> ranges = new TreeSet<IntRange>();
        for (Payouts payouts : structure.getPayoutsPerEntryRange()) {
            for (Payout payout :payouts.getPayoutList()) {
                ranges.add(payout.getPositionRange());
            }
        }
        return ranges;
    }
    private String rangeToString(IntRange range) {
        if (range.getStart() == range.getStop()) {
            return String.valueOf(range.getStart());
        } else {
            return range.getStart() + "-" + range.getStop();
        }
    }
}
