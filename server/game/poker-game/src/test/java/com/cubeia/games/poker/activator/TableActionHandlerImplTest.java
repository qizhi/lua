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

package com.cubeia.games.poker.activator;

import com.cubeia.backend.cashgame.dto.CloseTableRequest;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.activator.CreationParticipant;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.timing.TimingFactory;
import com.google.inject.Guice;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static com.cubeia.poker.variant.PokerVariant.TELESINA;

public class TableActionHandlerImplTest {

	@Inject
	private TableActionHandler handler;
	
	@Inject
	private ActivatorRouter router;
	
	@Inject
	private TableFactory factory;
	
	@Before
	public void setup() throws Exception {
		Guice.createInjector(new TestActivatorModule() {
			
			@Override
			protected void configure() {
				bind(TableFactory.class).toInstance(Mockito.mock(TableFactory.class));
				bind(ActivatorRouter.class).toInstance(Mockito.mock(ActivatorRouter.class));
				bind(TableActionHandler.class).to(TableActionHandlerImpl.class);
				super.configure();
			}
		}).injectMembers(this);
	}
	
	@Test
	public void testClose() {
		TableModifierAction action = TableModifierAction.close(5);
		handler.handleAction(action);
		ArgumentCaptor<Integer> tableIdCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<GameAction> actionCapture = ArgumentCaptor.forClass(GameAction.class);
		Mockito.verify(router).dispatchToGame(tableIdCapture.capture(), actionCapture.capture());
		int tableId = tableIdCapture.getValue();
		GameObjectAction result = (GameObjectAction) actionCapture.getValue();
		Assert.assertTrue(result.getAttachment() instanceof CloseTableRequest);
		Assert.assertEquals(5, tableId); 
	}
	
	@Test
	public void testCreate() {
		TableConfigTemplate templ = createTemplate();
		TableModifierAction action = TableModifierAction.create(templ);
		handler.handleAction(action);
		ArgumentCaptor<Integer> seatsCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<CreationParticipant> paticipantCapture = ArgumentCaptor.forClass(CreationParticipant.class);
		Mockito.verify(factory).createTable(seatsCapture.capture(), paticipantCapture.capture());
		int seats = seatsCapture.getValue();
		PokerParticipant value = (PokerParticipant) paticipantCapture.getValue();
		Assert.assertNotNull(value);
		Assert.assertEquals(6, seats);
	}
	
	@Test
	public void testDestroy() {
		TableModifierAction action = TableModifierAction.destroy(6);
		handler.handleAction(action);
		ArgumentCaptor<Integer> idCapture = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(factory).destroyTable(idCapture.capture(), Mockito.anyBoolean());
		int tableId = idCapture.getValue();
		Assert.assertEquals(6, tableId);
	}
	
	
	// --- PRIVATE METHODS --- //

	private TableConfigTemplate createTemplate() {
		TableConfigTemplate templ = new TableConfigTemplate();
		templ.setAnte(BigDecimal.TEN);
		templ.setSeats(6);
		templ.setTiming(TimingFactory.getRegistry().getTimingProfile("EXPRESS"));
		templ.setVariant(TELESINA);
		return templ;
	}
}
