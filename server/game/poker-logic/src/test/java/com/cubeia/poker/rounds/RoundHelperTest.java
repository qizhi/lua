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

package com.cubeia.poker.rounds;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.timing.Periods;
import com.cubeia.poker.timing.TimingProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RoundHelperTest {

    @Mock
    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private TimingProfile timingProfile;

    private RoundHelper roundHelper;

    @Before
    public void setup() {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getTimingProfile()).thenReturn(timingProfile);
        when(context.getTotalPotSize()).thenReturn(new BigDecimal(50));
        when(timingProfile.getTime(Periods.ACTION_TIMEOUT)).thenReturn(10L);
        roundHelper = new RoundHelper(context, serverAdapterHolder);
    }

    @Test
    public void testRequestAction() {
        ActionRequest actionRequest = mock(ActionRequest.class);
        roundHelper.requestAction(actionRequest);

        verify(actionRequest).setTotalPotSize(new BigDecimal(50));
        verify(actionRequest).setTimeToAct(10L);
        verify(serverAdapter).requestAction(actionRequest);
    }

    @Test
    public void requestMultipleActions() {
        ActionRequest actionRequest1 = mock(ActionRequest.class);
        ActionRequest actionRequest2 = mock(ActionRequest.class);

        Collection<ActionRequest> requests = Arrays.asList(actionRequest1, actionRequest2);
        roundHelper.requestMultipleActions(requests);

        verify(actionRequest1).setTimeToAct(10L);
        verify(actionRequest2).setTimeToAct(10L);
        verify(serverAdapter).requestMultipleActions(requests);
    }
}
