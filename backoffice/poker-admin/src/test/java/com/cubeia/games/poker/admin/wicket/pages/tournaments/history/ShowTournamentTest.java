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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.history;

import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ShowTournamentTest {

    @Mock
    private HistoryService historyService;
    private ApplicationContextMock context = new ApplicationContextMock();

    @Before
    public void setup() {
        initMocks(this);
        context = new ApplicationContextMock();
        context.putBean("historyService", historyService);
    }

    @Test
    public void testShowTournament() {
        WicketTester tester = new WicketTester();
        tester.getApplication().getComponentInstantiationListeners().add(new SpringComponentInjector(tester.getApplication(), context));
        PageParameters pageParameters = new PageParameters();
        pageParameters.set("historicTournamentId", "someId");
        HistoricTournament tournament = new HistoricTournament();
        tournament.setId(new ObjectId());
        when(historyService.findTournamentByHistoricId("someId")).thenReturn(tournament);
        tester.startPage(new ShowTournament(pageParameters));
        tester.assertRenderedPage(ShowTournament.class);
    }
}
