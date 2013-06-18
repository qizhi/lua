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
import com.cubeia.poker.shutdown.impl.ShutdownServiceMBean;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class SystemManagement extends BasePage {

    @SpringBean
    private FirebaseJMXFactory jmxFactory;

    private transient StateClientRegistryMBean clientRegistryMBean;

    private transient ShutdownServiceMBean shutdownServiceMBean;

    public String prepareMessage = "";

    public String finishMessage = "";

    public SystemManagement(PageParameters p) {
        super(p);
        initJmx();
        initPage();
    }

    private void initJmx() {
        clientRegistryMBean = jmxFactory.createClientRegistryProxy();
        shutdownServiceMBean = jmxFactory.createShutdownServiceProxy();
    }

    private void initPage() {
        Form<SystemManagement> prepareShutdownForm = new Form<SystemManagement>("prepareShutdownForm", new CompoundPropertyModel<SystemManagement>(this));
        Form<SystemManagement> finishShutdownForm = new Form<SystemManagement>("finishShutdownForm", new CompoundPropertyModel<SystemManagement>(this));

        Button prepareSubmit = new Button("submitPrepare") {
            @Override
            public void onSubmit() {
                if (shutdownServiceMBean.prepareShutdown()) {
                    clientRegistryMBean.sendSystemMessage(0, 0, prepareMessage);
                    info("Shutdown prepared.");
                } else {
                    info("Could not prepare shutdown, maybe it has already been done?");
                }
            }
        };

        Button finishSubmit = new Button("submitFinish") {
            @Override
            public void onSubmit() {
                if (shutdownServiceMBean.finishShutdown()) {
                    clientRegistryMBean.sendSystemMessage(0, 0, prepareMessage);
                    info("Shutdown finished.");
                } else {
                    warn("Could not finish shutdown, maybe it has not been prepared yet?");
                }
            }
        };

        prepareShutdownForm.add(new RequiredTextField<String>("prepareMessage"));
        prepareShutdownForm.add(prepareSubmit);
        finishShutdownForm.add(new RequiredTextField<String>("finishMessage"));
        finishShutdownForm.add(finishSubmit);

        add(prepareShutdownForm);
        add(finishShutdownForm);

        add(new FeedbackPanel("feedback"));
    }

    @Override
    public String getPageTitle() {
        return "System Management";
    }
}
