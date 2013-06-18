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

import java.util.ArrayList;
import java.util.List;

public class PageNode {
    private String title;
    private String icon;
    private Class<? extends Page> pageClass;
    private List<PageNode> children;

    private PageNode parent = null;

    private boolean linkable;

    public PageNode(String title, Class<? extends Page> pageClass, String icon, boolean linkable) {
        this.title = title;
        this.pageClass = pageClass;
        this.children = new ArrayList<PageNode>();
        this.linkable = linkable;
        this.icon = icon;
    }

    public boolean isLinkable() {
        return linkable;
    }

    public PageNode getParent() {
        return parent;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    public boolean hasIcon() {
        return icon!=null;
    }

    public void addChild(PageNode node) {
        node.parent = this;
        this.children.add(node);
    }

    public boolean isDescendantOf(Class<? extends Page> page) {
        if(this.pageClass.equals(page)) {
            return true;
        } else if(this.parent!=null && parent.isDescendantOf(page)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasChildren() {
        return this.children.size()>0;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public Class<? extends Page> getPageClass() {
        return pageClass;
    }
    public List<PageNode> getChildren() {
        return children;
    }

    public boolean isRelatedTo(Class<? extends Page> currentPageClass) {
        if(currentPageClass.equals(this.pageClass)) {
            return true;
        } else if(this.hasChildren()) {
            for(PageNode n : this.children) {
                if(n.isRelatedTo(currentPageClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasLinkableChildren() {
        if(hasChildren()) {
            for(PageNode n : getChildren()) {
                if(n.isLinkable()) {
                    return true;
                }
            }
        }
        return false;
    }
}
