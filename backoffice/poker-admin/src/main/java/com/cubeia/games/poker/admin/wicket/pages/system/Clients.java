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

package com.cubeia.games.poker.admin.wicket.pages.system;

import com.cubeia.firebase.service.clientreg.state.StateClientRegistryMBean;
import com.cubeia.games.poker.admin.jmx.FirebaseJMXFactory;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class Clients extends BasePage {
    private static final transient Logger log = Logger.getLogger(Clients.class);
    
    private transient StateClientRegistryMBean mbeanProxy;

    public Clients(final PageParameters parameters) {
        super(parameters);
        FirebaseJMXFactory jmxFactory = new FirebaseJMXFactory();
        mbeanProxy = jmxFactory.createClientRegistryProxy();

        add(new FeedbackPanel("feedback"));
        add(new Label("clientsOnline", "" + getClients()));
    }

    private int getClients() {
        int clients  = -1;
        try {
            clients = mbeanProxy.getNumberOfClients();
        } catch (Exception e) {
            log.warn("error calling jmx server", e);
            error("unable to contact jmx server: " + e.getMessage());
        }
        return clients;
    }

    @Override
    public String getPageTitle() {
        return "Live Players";
    }

}
