/**
 * Copyright (C) 2012 BetConstruct
 */

package com.cubeia.games.poker.admin.wicket.pages.timings;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cubeia.games.poker.admin.wicket.util.ArchiveLinkPanel;
import com.cubeia.poker.timing.TimingProfile;
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

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.util.DeleteLinkPanel;
import com.cubeia.games.poker.admin.wicket.util.LabelLinkPanel;

/**
 * Page for listing all timing configurations.
 */
public class ListTimings extends BasePage {

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;


    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters Page parameters
     */
    public ListTimings(final PageParameters parameters) {
        super(parameters);
        SortableDataProviderExtension dataProvider = new SortableDataProviderExtension();
        ArrayList<AbstractColumn<TimingProfile,String>> columns = new ArrayList<AbstractColumn<TimingProfile,String>>();
        columns.add(new AbstractColumn<TimingProfile,String>(new Model<String>("Id")) {

            @Override
            public void populateItem(Item<ICellPopulator<TimingProfile>> item, String componentId, IModel<TimingProfile> model) {
                TimingProfile timing = model.getObject();
                Component panel = new LabelLinkPanel(
                        componentId,
                        "" + timing.getId(),
                        EditTiming.class,
                        params("templateId", timing.getId()));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Name"), "name"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Pocket cards time"), "pocketCardsTime"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Flop time"), "flopTime"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Turn time"), "turnTime"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("River time"), "riverTime"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Start new hand time"), "startNewHandTime"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Action timeout"), "actionTimeout"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Automatic blinds posting delay"), "autoPostBlindDelay"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Latency grace period"), "latencyGracePeriod"));
        columns.add(new PropertyColumn<TimingProfile,String>(new Model<String>("Disconnection extra timeout"), "disconnectExtraTime"));

        columns.add(new AbstractColumn<TimingProfile,String>(new Model<String>("Delete")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<TimingProfile>> item, String componentId, IModel<TimingProfile> model) {
                TimingProfile timing = model.getObject();
                Component panel = new ArchiveLinkPanel(componentId, new TimingArchiver(timing), timing, ListTimings.class);
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        DefaultDataTable userTable = new DefaultDataTable("timingTable", columns, dataProvider, 20);
        add(userTable);

        add(new FeedbackPanel("feedback"));
    }

    private List<TimingProfile> getTimingProfileList() {
        return adminDAO.getTimingProfiles();
    }

    @Override
    public String getPageTitle() {
        return "Timing Configurations";
    }

//
//    //  --- PRIVATE CLASSES --- //
//
    private final class SortableDataProviderExtension extends SortableDataProvider<TimingProfile,String> {

        private static final long serialVersionUID = 1L;

        public SortableDataProviderExtension() {
            setSort("id", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<TimingProfile> iterator(long first, long count) {
            return getTimingProfileList().subList((int)first, (int)(count + first)).iterator();
        }

        @Override
        public IModel<TimingProfile> model(TimingProfile object) {
            return new Model<TimingProfile>(object);
        }

        @Override
        public long size() {
            return getTimingProfileList().size();
        }
    }
}
