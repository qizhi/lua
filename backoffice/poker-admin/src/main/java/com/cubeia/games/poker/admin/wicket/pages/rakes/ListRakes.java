/**
 * Copyright (C) 2012 BetConstruct
 */

package com.cubeia.games.poker.admin.wicket.pages.rakes;

import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cubeia.games.poker.admin.wicket.util.ArchiveLinkPanel;
import com.cubeia.poker.settings.RakeSettings;
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
 * Page for listing all rake configurations.
 */
public class ListRakes extends BasePage {

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;


    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters Page parameters
     */
    public ListRakes(final PageParameters parameters) {
        super(parameters);
        SortableDataProviderExtension dataProvider = new SortableDataProviderExtension();
        ArrayList<AbstractColumn<RakeSettings,String>> columns = new ArrayList<AbstractColumn<RakeSettings,String>>();
        columns.add(new AbstractColumn<RakeSettings,String>(new Model<String>("Id")) {

            @Override
            public void populateItem(Item<ICellPopulator<RakeSettings>> item, String componentId, IModel<RakeSettings> model) {
                RakeSettings rake = model.getObject();
                Component panel = new LabelLinkPanel(
                        componentId,
                        "" + rake.getId(),
                        EditRake.class,
                        params("templateId", rake.getId()));
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Name"), "name"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake fraction for 2 players"), "rakeFraction2Plus"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake limit for 2 players"), "rakeLimit2Plus"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake fraction for 3+ players"), "rakeFraction3Plus"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake limit for 3+ players"), "rakeLimit3Plus"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake fraction for 5+ players"), "rakeFraction5Plus"));
        columns.add(new PropertyColumn<RakeSettings,String>(new Model<String>("Rake limit for 5+ players"), "rakeLimit5Plus"));

        columns.add(new AbstractColumn<RakeSettings,String>(new Model<String>("Delete")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<RakeSettings>> item, String componentId, IModel<RakeSettings> model) {
                RakeSettings rake = model.getObject();
                Component panel = new ArchiveLinkPanel(componentId, new RakeArchiver(rake), rake, ListRakes.class);
                item.add(panel);
            }

            @Override
            public boolean isSortable() {
                return false;
            }
        });

        DefaultDataTable userTable = new DefaultDataTable("rakeTable", columns, dataProvider, 20);
        add(userTable);

        add(new FeedbackPanel("feedback"));
    }

    private List<RakeSettings> getRakeSettingsList() {
        return adminDAO.getRakeSettings();
    }

    @Override
    public String getPageTitle() {
        return "Rake Configurations";
    }

    private final class SortableDataProviderExtension extends SortableDataProvider<RakeSettings,String> {

        private static final long serialVersionUID = 1L;

        public SortableDataProviderExtension() {
            setSort("id", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<RakeSettings> iterator(long first, long count) {
            return getRakeSettingsList().subList((int)first, (int)(count + first)).iterator();
        }

        @Override
        public IModel<RakeSettings> model(RakeSettings object) {
            return new Model<RakeSettings>(object);
        }

        @Override
        public long size() {
            return getRakeSettingsList().size();
        }
    }
}
