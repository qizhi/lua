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

package com.cubeia.games.poker.admin.wicket.pages.history;

import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.poker.handhistory.api.HistoricHand;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;

import static com.cubeia.games.poker.admin.wicket.pages.WicketTestHelper.createMockHand;
import static com.cubeia.games.poker.admin.wicket.pages.WicketTestHelper.createWicketTester;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HandHistoryTest {

    @Mock
    private HistoryService historyService;

    private WicketTester tester;

    @Before
    public void setup() {
        initMocks(this);
        tester = createWicketTester(historyService);
    }

    @Test
    public void testHandSearch() {
        HistoricHand hand = createMockHand();
        when(historyService.findHandHistory(1, null, null, null)).thenReturn(Collections.singletonList(hand));
        PageParameters pageParameters = new PageParameters();
        tester.startPage(new HandHistory(pageParameters));
        tester.assertRenderedPage(HandHistory.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("playerId", "1");
        form.submit();
        verify(historyService).findHandHistory(Mockito.eq(1), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(Date.class));
    }

}
