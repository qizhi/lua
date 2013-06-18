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
import org.junit.Test;

import static com.cubeia.network.shared.web.wicket.navigation.PageNodeUtils.node;
import static org.junit.Assert.*;

public class PageNodeTest {

    @Test
    public void testChildren() {

        PageNode pn =
                PageNodeUtils.node("home", HomePage.class, "home-icon", true,
                        node("home child1", Level2Child1.class, null, true,
                                node("home child2", Level3Child1.class, null, false)
                        ),
                        node("home child1 child1", Level2Child2.class, null, true)
                );

        assertTrue(pn.hasChildren());
        assertNull(pn.getParent());

        PageNode child1 = pn.getChildren().get(0);
        PageNode child2 = pn.getChildren().get(1);

        assertEquals(child1.getPageClass(),Level2Child1.class);
        assertEquals(child1.getParent(),pn);

        assertTrue(pn.isRelatedTo(Level3Child1.class));
        assertTrue(pn.isRelatedTo(Level2Child1.class));
        assertTrue(child1.isRelatedTo(Level3Child1.class));
        assertFalse(child2.isRelatedTo(Level3Child1.class));

    }

    private static class HomePage extends Page {

    }
    private static class Level2Child1 extends Page {
        private static final long serialVersionUID = -4073406728522203466L;
    }
    private static class Level3Child1 extends Page {
        private static final long serialVersionUID = -822587640282456547L;
    }
    private static class Level2Child2 extends Page {
        private static final long serialVersionUID = 5209182193204816795L;
    }
}
