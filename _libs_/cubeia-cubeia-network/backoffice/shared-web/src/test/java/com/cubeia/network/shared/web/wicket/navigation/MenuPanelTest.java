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
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.node;

public class MenuPanelTest {

    @Test
    public void testMenuPanel() {
        List<PageNode> pages = new ArrayList<PageNode>();

        pages.add(
                node("home", Home.class, "home-icon", true,
                        node("news", HomeNews.class, "news-icon")
                )
        );
        pages.add(
                node("tournaments",Tournaments.class,"tournaments-icon",true)
        );
        WicketTester tester = new WicketTester();
        MenuPanel menu = new MenuPanel("menu", pages, Home.class);
        tester.startComponentInPage(menu);

        tester.assertLabel("menu:menuList:1:link:title","home");
        tester.assertLabel("menu:menuList:1:children:menuList:1:link:title","news");
        tester.assertLabel("menu:menuList:2:link:title","tournaments");


    }

    private static class Home extends Page {
        private static final long serialVersionUID = 6782011045404856894L;
    }
    private static class HomeNews extends Page {
        private static final long serialVersionUID = 156196419585426368L;
    }
    private static class Tournaments extends Page {
        private static final long serialVersionUID = 8189885996650094152L;
    }



}
