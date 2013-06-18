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

package com.cubeia.network.shared.web.wicket.navigation;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.util.List;

public class MenuPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public MenuPanel(String id, List<PageNode> pages, Class<? extends Page> currentPageClass) {
        super(id);
        RepeatingView rv = new RepeatingView("menuList");
        add(rv);
        createMenuItems(pages, currentPageClass, rv);
    }

    private void createMenuItems(List<PageNode> pages, Class<? extends Page> currentPageClass, RepeatingView rv) {
        for(PageNode n : pages) {
            if(!n.isLinkable()) {
                continue;
            }
            AbstractItem item = new AbstractItem(rv.newChildId());
            item.add(createMenuItem(n));
            boolean linkableChildren = n.hasLinkableChildren();
            if(n.isRelatedTo(currentPageClass)) {

                if(linkableChildren) {
                    item.add(AttributeModifier.append("class", "open"));
                }
                if(n.getPageClass()!=null) {
                    item.add(AttributeModifier.append("class", "active"));
                }
            }
            if(linkableChildren) {
                item.add(AttributeModifier.append("class", "submenu"));
                item.add(new MenuPanel("children",n.getChildren(),currentPageClass));
            } else {
                item.add(new WebMarkupContainer("children"));
            }
            rv.add(item);
        }
    }

    public Component createMenuItem(PageNode n) {
        BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("link", n.getPageClass(), null);
        WebMarkupContainer icon = null;
        if(n.hasIcon()) {
            icon = createIcon(n.getIcon());
        } else {
            icon = new WebMarkupContainer("icon");
            icon.add(AttributeModifier.replace("class","hidden"));
        }
        link.add(new Label("title",n.getTitle()));
        link.add(icon);
        return link;
    }

    private WebMarkupContainer createIcon(String iconClass) {
        WebMarkupContainer icon = new WebMarkupContainer("icon");
        icon.add(AttributeModifier.replace("class", "icon " + iconClass));
        return icon;
    }
}
