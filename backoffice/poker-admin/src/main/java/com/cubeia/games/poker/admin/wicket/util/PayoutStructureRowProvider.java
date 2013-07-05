package com.cubeia.games.poker.admin.wicket.util;

import com.cubeia.games.poker.tournament.configuration.payouts.IntRange;
import com.cubeia.games.poker.tournament.configuration.payouts.Payout;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class PayoutStructureRowProvider implements IDataProvider<List<? extends String>> {

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

    private String rangeToString(IntRange range) {
        if (range.getStart() == range.getStop()) {
            return String.valueOf(range.getStart());
        } else {
            return range.getStart() + "-" + range.getStop();
        }
    }
}