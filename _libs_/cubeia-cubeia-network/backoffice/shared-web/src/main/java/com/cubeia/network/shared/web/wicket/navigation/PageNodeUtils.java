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

import org.apache.wicket.Page;

import java.util.List;


public class PageNodeUtils {

    public static PageNode node(String title, Class<? extends Page> page, String icon, boolean linkable, PageNode... children) {
        PageNode n = new PageNode(title,page,icon,linkable);
        if(children!=null && children.length>0) {
            for(PageNode c : children) {
                n.addChild(c);
            }
        }
        return n;
    }
    public static PageNode node(String title, Class<? extends Page> page, String icon) {
        return node(title,page,icon,true,null);
    }
    public static PageNode node(String title, Class<? extends Page> page) {
        return node(title,page,null,true,null);
    }
    public static PageNode node(String title, Class<? extends Page> page, boolean linkable) {
        return node(title,page,null,linkable,null);
    }

    public static void add(List<PageNode> pages, String title, Class<? extends Page> page, String icon) {
        pages.add(node(title,page,icon,true,null));
    }
    public static void add(List<PageNode> pages, String title, Class<? extends Page> page, String icon, PageNode... children) {
        pages.add(node(title,page,icon,true,children));
    }

    public static PageNode nodeWithChildren(PageNode node, PageNode ... children) {
        for (PageNode child : children) {
            node.addChild(child);
        }
        return node;
    }

    public static PageNode nodeWithChildren(String name, Class<? extends Page> page, String icon, PageNode ... children) {
        PageNode node = node(name, page, icon);
        return nodeWithChildren(node, children);
    }

}
