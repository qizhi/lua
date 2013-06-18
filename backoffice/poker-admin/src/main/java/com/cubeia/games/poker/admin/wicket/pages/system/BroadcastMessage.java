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

package com.cubeia.games.poker.admin.wicket.pages.system;

import com.cubeia.firebase.service.clientreg.state.StateClientRegistryMBean;
import com.cubeia.games.poker.admin.jmx.FirebaseJMXFactory;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class BroadcastMessage extends BasePage {

    private transient StateClientRegistryMBean mbeanProxy;

    String message;

    public BroadcastMessage(PageParameters p) {
        super(p);
        FirebaseJMXFactory jmxFactory = new FirebaseJMXFactory();
        mbeanProxy = jmxFactory.createClientRegistryProxy();
        // Send message form
        Form broadcastForm = new Form("broadcastForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                sendSystemMessage(message);
                info("Message \"" + message + "\" sent.");
            }
        };

        broadcastForm.add(new RequiredTextField<String>("message", new PropertyModel<String>(this, "message")));
        add(broadcastForm);
        add(new FeedbackPanel("feedback"));
    }

    private void sendSystemMessage(String message) {
        mbeanProxy.sendSystemMessage(0, 0, message);
    }

    @Override
    public String getPageTitle() {
        return "Broadcast Message";
    }
}
