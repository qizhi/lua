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

import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_TEMPLATE;

import java.util.Collections;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.games.poker.entity.TableConfigTemplate;
 
public class TemplateLobbyTableFilterTest {

	@Test
	public void simpleFind() {
		TableConfigTemplate templ = Mockito.mock(TableConfigTemplate.class);
		Mockito.when(templ.getId()).thenReturn(1);
		Map<String, AttributeValue> map = Collections.singletonMap(TABLE_TEMPLATE.name(), AttributeValue.wrap(1));
		Assert.assertTrue(new TemplateLobbyTableFilter(templ).accept(map));
	}
	
	@Test
	public void simpleMiss() {
		TableConfigTemplate templ = Mockito.mock(TableConfigTemplate.class);
		Mockito.when(templ.getId()).thenReturn(2); // ID MISS
		Map<String, AttributeValue> map = Collections.singletonMap(TABLE_TEMPLATE.name(), AttributeValue.wrap(1));
		Assert.assertFalse(new TemplateLobbyTableFilter(templ).accept(map));
	}
	
	@Test
	public void typeMiss() {
		TableConfigTemplate templ = Mockito.mock(TableConfigTemplate.class);
		Mockito.when(templ.getId()).thenReturn(1); 
		Map<String, AttributeValue> map = Collections.singletonMap(TABLE_TEMPLATE.name(), AttributeValue.wrap("1")); // Wrong attribute type
		Assert.assertFalse(new TemplateLobbyTableFilter(templ).accept(map));
	}
}
