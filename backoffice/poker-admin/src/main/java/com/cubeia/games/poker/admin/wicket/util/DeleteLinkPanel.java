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

package com.cubeia.games.poker.admin.wicket.util;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class DeleteLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "adminDAO")
    private AdminDAO adminDAO;

    public DeleteLinkPanel(String id, final Class<?> entityClass, final int entityId, final Class<? extends BasePage> responsePage) {
        super(id);
        Link<String> delete = new Link<String>("link", Model.of("delete")) {

            @Override
            public void onClick() {
                try {
                    adminDAO.removeItem(entityClass, entityId);
                    setResponsePage(responsePage);
                }
                catch (Exception ex) {
                    this.getPage().error("Could not delete: " + ex.getMessage());
                }
            }
        };
        delete.add(new AttributeAppender("class","btn btn-danger btn-mini"));
        add(delete);
    }
}
