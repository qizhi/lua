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

package com.cubeia.games.poker.admin.wicket.pages.tables;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.network.NetworkClient;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.cubeia.games.poker.admin.wicket.pages.WicketTestHelper.createWicketTester;
import static org.mockito.MockitoAnnotations.initMocks;

public class CreateTableTest {

    @Mock
    private AdminDAO adminDao;

    @Mock
    private NetworkClient networkClient;

    private WicketTester tester;

    @Before
    public void setup() {
        initMocks(this);
        tester = createWicketTester(adminDao, networkClient);
    }

    @Test
    public void testCreateTable() {
        PageParameters pageParameters = new PageParameters();
        tester.startPage(new CreateTable(pageParameters));
        tester.assertRenderedPage(CreateTable.class);
    }
}
