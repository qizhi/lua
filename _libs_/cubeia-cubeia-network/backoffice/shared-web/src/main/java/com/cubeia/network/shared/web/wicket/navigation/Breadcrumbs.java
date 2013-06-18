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
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.util.List;

public class Breadcrumbs extends Panel {

    public Breadcrumbs(String id, List<PageNode> pages, Class<? extends Page> currentPageClass) {
        super(id);
        PageNode node = findPage(pages,currentPageClass);
        if(node!=null) {
            RepeatingView rv = new RepeatingView("breadcrumb");
            add(rv);
            addBreadCrumb(node,rv);
        } else {
            WebMarkupContainer empty = new WebMarkupContainer("breadcrumb");
            add(empty);
            WebMarkupContainer link = new WebMarkupContainer("link");
            empty.add(link);
            link.add(new WebMarkupContainer("icon"));
            link.add(new WebMarkupContainer("title"));
        }
    }

    private void addBreadCrumb(PageNode node, RepeatingView rv) {
        if(node.getParent()!=null) {
            addBreadCrumb(node.getParent(),rv);
        }
        AbstractItem ai = new AbstractItem(rv.newChildId());
        WebMarkupContainer link = null;
        if(node.isLinkable()) {
            link = new BookmarkablePageLink("link",node.getPageClass());
        } else {
            link = new WebMarkupContainer("link");
        }
        ai.add(link);
        link.add(new Label("title",node.getTitle()));
        WebMarkupContainer icon = new WebMarkupContainer("icon");
        if(node.hasIcon()) {
            icon.add(AttributeModifier.replace("class",node.getIcon()));
        } else {
            icon.add(AttributeModifier.replace("class","hide"));
        }
        link.add(icon);
        rv.add(ai);
    }

    private PageNode findPage(List<PageNode> pages, Class<? extends Page> currentPageClass) {
        for(PageNode pn : pages) {
            if(pn.hasChildren()) {
                PageNode n =  findPage(pn.getChildren(),currentPageClass);
                if(n!=null) {
                    return n;
                }
            }
            if(currentPageClass.equals(pn.getPageClass())) {
                return pn;
            }
        }
        return null;
    }

}
