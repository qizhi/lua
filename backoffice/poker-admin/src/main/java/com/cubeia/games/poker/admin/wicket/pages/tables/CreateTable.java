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
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.TimingFactory;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;

import static com.cubeia.poker.variant.PokerVariant.TEXAS_HOLDEM;

public class CreateTable extends BasePage {

    private static final long serialVersionUID = 6896786450236805072L;

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;

    private TableConfigTemplate table;

    public CreateTable(final PageParameters parameters) {
        super(parameters);
        table = new TableConfigTemplate();
        table.setVariant(TEXAS_HOLDEM);
        table.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        table.setRakeSettings(new RakeSettings());
        table.setSeats(10);
        table.setMinTables(1);
        table.setMinEmptyTables(1);
        table.setBetStrategy(BetStrategyType.NO_LIMIT);
        table.setAnte(BigDecimal.ZERO);

        TableForm tableForm = new TableForm("tableForm", table) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(TableConfigTemplate config) {
                adminDAO.save(config);
                setResponsePage(ListTables.class);
            }
        };
        add(tableForm);
        add(new FeedbackPanel("feedback"));
    }

    @Override
    public String getPageTitle() {
        return "Create Table";
    }
}