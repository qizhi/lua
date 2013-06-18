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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LabelLinkPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public LabelLinkPanel(String id, String text, Class<? extends Page> page, PageParameters params) {
        this(id, text, null, page, params);
    }
    
    public LabelLinkPanel(String id, String text, String toolTip, Class<? extends Page> page, PageParameters params) {
        super(id);
        BookmarkablePageLink<?> link = new BookmarkablePageLink<Void>("link", page, params);
        link.add(new Label("label", text));
        
        if (toolTip != null) {
            link.add(new AttributeModifier("title", Model.of(toolTip)));
        }
        
        add(link);
    }
}
