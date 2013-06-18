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

import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.timing.TimingFactory;
import com.google.inject.Guice;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static com.cubeia.poker.variant.PokerVariant.TELESINA;

public class ParticipantFactoryImplTest {


	@Inject
	private ParticipantFactory fact;
	
	@Before
	public void setup() throws Exception {
		Guice.createInjector(new TestActivatorModule()).injectMembers(this);
	}
	
	@Test
	public void testSimpleCreation() {
		TableConfigTemplate templ = createTemplate();
		PokerParticipant part = fact.createParticipantFor(templ);
		Assert.assertNotNull(part);
		Assert.assertNotNull(part.getCashGameBackendService());
		Assert.assertEquals(templ, part.getTemplate());
		Assert.assertEquals(6, part.getSeats());
	}
	
	
	// --- PRIVATE METHODS --- //

	private TableConfigTemplate createTemplate() {
		TableConfigTemplate templ = new TableConfigTemplate();
		templ.setAnte(new BigDecimal(10));
		templ.setSeats(6);
		templ.setTiming(TimingFactory.getRegistry().getTimingProfile("EXPRESS"));
		templ.setVariant(TELESINA);
		return templ;
	}
}
